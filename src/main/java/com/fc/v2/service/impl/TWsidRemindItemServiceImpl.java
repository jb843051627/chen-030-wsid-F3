package com.fc.v2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.mapper.auto.TWsidRemindDispatchMapper;
import com.fc.v2.mapper.auto.TWsidRemindItemMapper;
import com.fc.v2.model.auto.TWsidRemindDispatch;
import com.fc.v2.model.auto.TWsidRemindItem;
import com.fc.v2.model.custom.wsid.WsidRemindConsts;
import com.fc.v2.model.custom.wsid.WsidRemindLineView;
import com.fc.v2.model.custom.wsid.WsidRemindProperties;
import com.fc.v2.model.custom.wsid.WsidRemindReceiptView;
import com.fc.v2.model.custom.wsid.WsidRemindScanCmd;
import com.fc.v2.model.custom.wsid.WsidRemindScanResult;
import com.fc.v2.service.ITWsidRemindItemService;
import com.fc.v2.util.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 年度核查与换证提醒条目 Service 实现——按日照跑，六问只此一条扫描方法开口。
 *
 * <p>六条按次序落，一条不并进另一条：</p>
 * <ol>
 *   <li><b>挑行</b>：一轮只认开跑那一刻 {@code at}。存量条目与今年新立同走一个挑法：
 *       amount 空着／取负／大过定数上限 90，本轮不动也不借别行的数；
 *       文面齐的行还要落在窗口内（核查往应交之前数、换证往届满之前数，0..amount 日）。</li>
 *   <li><b>回落</b>：通道取用式三式挨个退，一式只走一次，试一档落一档（前档不擦）；
 *       退到第几式走通，行面记第几式；连最后一式也落不了地，沉「发不成」。</li>
 *   <li><b>静默</b>：开不开窗只认 {@link WsidRemindProperties} 两栏，程序不自掐时辰；
 *       窗内够日子的行搁着不起，qualified_at 仍记开跑那一刻，窗走完后的趟次才落 dispatch_at。</li>
 *   <li><b>单行缺字</b>：少主体代号／少层别名目／少该发的那一天，只停本行：不发不代填，
 *       缺哪几栏原样报上；其余各行照发，整轮不卡。发成发不成都回写行面。</li>
 *   <li><b>同一天只一回</b>：去重键＝一处主体＋一桩事由＋一日；同日再扫只对照不重发，
 *       隔日重新累计。同一刻两趟分开排队（去重键上锁先后进），答复逐行相同。</li>
 *   <li><b>交数</b>：捞着／走通／回落三笔与逐行记号同一次算定，不另拉一轮清点；
 *       一行没捞着只落「本轮空跑」，不牵首行、不抄上轮。</li>
 * </ol>
 *
 * <p>并发排队在逐行事务之外：后进趟次拿到锁时，先进趟次的回执档早已提交，
 * 只对照即可，两趟答复出自同一份当日账，天然相同。</p>
 *
 * @author fuce
 * @date 2026-10-07
 */
@Service
public class TWsidRemindItemServiceImpl implements ITWsidRemindItemService {

    /** 墙钟口径统一 GMT+8，避开 JDBC serverTimezone 与本机不对称的整体偏移。 */
    private static final ZoneId ZONE = ZoneId.of("GMT+8");
    private static final String TS_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DAY_PATTERN = "yyyy-MM-dd";

    /** 每个去重键一把锁：同一刻两趟在这把锁上排队，先进的发、后进的只对照。 */
    private final ConcurrentHashMap<String, ReentrantLock> keyLocks = new ConcurrentHashMap<String, ReentrantLock>();

    @Resource
    private TWsidRemindItemMapper remindItemMapper;

    @Resource
    private TWsidRemindDispatchMapper dispatchMapper;

    @Resource
    private WsidRemindRowWriter rowWriter;

    @Resource
    private WsidRemindProperties properties;

    @Override
    public TWsidRemindItem selectTWsidRemindItemById(Long id) {
        return this.remindItemMapper.selectById(id);
    }

    // ============================================================ 唯一的扫描方法

    @Override
    public WsidRemindScanResult scan(WsidRemindScanCmd cmd) {
        Date at = cmd == null || cmd.getAt() == null ? new Date() : cmd.getAt();
        String runId = StringUtils.isNotEmpty(cmd == null ? null : cmd.getRunId())
                ? cmd.getRunId().trim()
                : "R" + stamp(at) + "-" + UUID.randomUUID().toString().substring(0, 8);
        String runDay = day(at);

        // 三笔与逐行记号在同一趟循环里算定，全程不另拉一轮逐行清点
        int picked = 0;
        int sent = 0;
        int fallback = 0;
        int fail = 0;
        int incomplete = 0;
        int silentHold = 0;
        List<WsidRemindLineView> lines = new ArrayList<WsidRemindLineView>();

        List<TWsidRemindItem> all = remindItemMapper.selectList(new QueryWrapper<TWsidRemindItem>()
                .eq("del_flag", 0)
                .orderByAsc("id"));

        for (TWsidRemindItem item : all) {
            // —— 第一条·头一道：amount 空着/取负/大过定数上限，或事由不在两桩之内，本轮不动 ——
            if (!passesLeadGate(item)) {
                continue;
            }

            // 同一刻两趟分开排队、各留各的先后；锁在逐行事务外，后进读得到先进已提交的账。
            // 去重键钉在「一处主体＋一桩事由＋一日（开跑当日）」三样上。
            String dedupKey = item.getHolderNo() + "|" + item.getMatterKind() + "|" + runDay;
            ReentrantLock lock = keyLocks.computeIfAbsent(dedupKey, k -> new ReentrantLock());
            lock.lock();
            try {
                // 拿锁后重读行面：开跑时读的那份可能已被先进一趟落定，按旧账会重发
                TWsidRemindItem fresh = remindItemMapper.selectById(item.getId());
                if (fresh != null) {
                    item = fresh;
                }
                boolean heldOver = isHeldOverSilence(item, at);

                // —— 第五条·隔日重新累计：已落定（发出/发不成/缺栏）的行只在当日只对照；
                // 隔日它不再是本轮的事——新一桩提醒另立条目，不把旧行天天重发
                Integer st = item.getStatus();
                boolean terminal = st != null
                        && (st == WsidRemindConsts.STATUS_SENT
                        || st == WsidRemindConsts.STATUS_FAIL
                        || st == WsidRemindConsts.STATUS_INCOMPLETE);
                if (terminal && !heldOver) {
                    // 「一日」钉在够上条件那一瞬当日（与回执账同口径）：静默跨夜醒来的行
                    // qualified_at 记的是昨天那一瞬，醒来当天再扫仍照昨天那本账对照；
                    // 隔日两样都不沾，跳过（新一桩提醒另立条目，不把旧行天天重发）
                    String qDay = item.getQualifiedAt() == null ? runDay : day(item.getQualifiedAt());
                    List<TWsidRemindDispatch> todayLedger = listLedgerOfDay(item, qDay);
                    if (!runDay.equals(qDay) && todayLedger.isEmpty()) {
                        continue;
                    }
                    picked++;
                    if (st == WsidRemindConsts.STATUS_INCOMPLETE) {
                        incomplete++;
                        lines.add(viewOf(item, null, WsidRemindConsts.STATUS_INCOMPLETE,
                                false, new ArrayList<TWsidRemindDispatch>(), item.getRemark()));
                        continue;
                    }
                    boolean fb = todayLedger.size() > 1;
                    if (fb) {
                        fallback++;
                    }
                    if (st == WsidRemindConsts.STATUS_SENT) {
                        sent++;
                        TWsidRemindDispatch reached = firstReached(todayLedger);
                        Integer level = reached == null ? null : reached.getChannelLevel();
                        lines.add(viewOfLedger(item, level, WsidRemindConsts.STATUS_SENT,
                                fb, todayLedger, "同日已走过，本趟只对照不重发"));
                    } else {
                        fail++;
                        lines.add(viewOfLedger(item, null, WsidRemindConsts.STATUS_FAIL,
                                fb, todayLedger,
                                "同日已发不成，本趟只对照" + (item.getRemark() == null ? "" : "：" + item.getRemark())));
                    }
                    continue;
                }

                // —— 第四条：文面缺栏只停本行，缺哪几栏原样报出；其余各行照发 ——
                List<String> missing = missingFields(item);
                if (!missing.isEmpty()) {
                    picked++;
                    incomplete++;
                    if (!heldOver && !runDay.equals(day(item.getQualifiedAt()))) {
                        item.setQualifiedAt(at);
                    }
                    rowWriter.markIncomplete(item, missing);
                    lines.add(viewOf(item, null, WsidRemindConsts.STATUS_INCOMPLETE,
                            false, new ArrayList<TWsidRemindDispatch>(),
                            "文面缺栏，本行按下不发，也不代填：缺" + joinFields(missing)));
                    continue;
                }

                // —— 第一条·第二道：够不够日子只在这里定夺，外头另算的一概不算 ——
                Long days = daysBetween(at, item.getDueAt());
                if (days == null || days < 0
                        || new BigDecimal(days.toString()).compareTo(item.getAmount()) > 0) {
                    continue; // 没进窗口（或已过了该发的那一天），本轮不捞，不借别行的数顶
                }

                picked++;
                // 静默搁过的行醒来（哪怕窗走完时已过夜）：保住当初够上条件那一瞬不改记
                if (!heldOver && !runDay.equals(day(item.getQualifiedAt()))) {
                    item.setQualifiedAt(at);
                }

                WsidRemindLineView view;
                if (heldOver) {
                    // 静默窗已走完：当初够上条件那一瞬还在账上，此刻起话、回落照常
                    List<TWsidRemindDispatch> attempts = rowWriter.cascade(item, runId);
                    TWsidRemindDispatch ok = firstReached(attempts);
                    boolean fb = attempts.size() > 1;
                    if (fb) {
                        fallback++;
                    }
                    if (ok != null) {
                        sent++;
                        view = viewOf(item, ok.getChannelLevel(), WsidRemindConsts.STATUS_SENT,
                                fb, attempts, "静默走完后起话，起话仍认当初够上条件那一瞬；"
                                        + attempts.get(attempts.size() - 1).getContent());
                    } else {
                        fail++;
                        String why = attempts.isEmpty() ? "通道取用式不在三式之内"
                                : attempts.get(attempts.size() - 1).getContent();
                        view = viewOf(item, null, WsidRemindConsts.STATUS_FAIL,
                                fb, attempts, "发不成：" + why);
                    }
                } else {
                    // —— 第三条：静默开窗由配置两栏说了算，程序不自己掐时辰 ——
                    Date holdUntil = properties.silenceHoldUntil(at);
                    if (holdUntil != null) {
                        silentHold++;
                        rowWriter.markSilentHold(item, holdUntil);
                        view = viewOf(item, null, WsidRemindConsts.STATUS_WAIT,
                                false, new ArrayList<TWsidRemindDispatch>(),
                                "撞上静默时段，够日子也不当日起话，搁至 " + ts(holdUntil)
                                        + " 再起；起话仍记够上条件那一瞬 " + ts(item.getQualifiedAt()));
                    } else {
                        // —— 第二条：三式挨个退 ——
                        List<TWsidRemindDispatch> attempts = rowWriter.cascade(item, runId);
                        TWsidRemindDispatch ok = firstReached(attempts);
                        boolean fb = attempts.size() > 1;
                        if (fb) {
                            fallback++;
                        }
                        if (ok != null) {
                            sent++;
                            view = viewOf(item, ok.getChannelLevel(), WsidRemindConsts.STATUS_SENT,
                                    fb, attempts, attempts.get(attempts.size() - 1).getContent());
                        } else {
                            fail++;
                            String why = attempts.isEmpty() ? "通道取用式不在三式之内"
                                    : attempts.get(attempts.size() - 1).getContent();
                            view = viewOf(item, null, WsidRemindConsts.STATUS_FAIL,
                                    fb, attempts, "发不成：" + why);
                        }
                    }
                }
                lines.add(view);
            } finally {
                lock.unlock();
            }
        }

        // —— 第六条：交数。三笔与逐行记号同一次算定；空跑是合法结果，不牵首行、不抄上轮 ——
        WsidRemindScanResult result = new WsidRemindScanResult();
        result.setRunId(runId);
        result.setAt(at);
        result.setPickedNum(picked);
        result.setSentNum(sent);
        result.setFallbackNum(fallback);
        result.setFailNum(fail);
        result.setIncompleteNum(incomplete);
        result.setSilentHoldNum(silentHold);
        result.setLines(lines);
        if (picked == 0) {
            result.setEmptyRun(true);
            result.setContent("本轮空跑");
        } else {
            result.setEmptyRun(false);
            result.setContent("本轮捞着 " + picked + " 行、走通 " + sent + " 行、回落 " + fallback
                    + " 行（发不成 " + fail + "、缺栏 " + incomplete + "、静默搁起 " + silentHold + "）");
        }
        return result;
    }

    // ============================================================ 挑行口径

    /** 头一道只认 amount 与事由：空/负/越上限不动；事由不在两桩之内本轮不动，也不借别行的数。 */
    private boolean passesLeadGate(TWsidRemindItem item) {
        BigDecimal amount = item.getAmount();
        if (amount == null || amount.signum() < 0
                || amount.compareTo(new BigDecimal(WsidRemindConsts.MAX_LEAD_DAYS)) > 0) {
            return false;
        }
        Integer matter = item.getMatterKind();
        return matter != null
                && (matter == WsidRemindConsts.MATTER_AUDIT || matter == WsidRemindConsts.MATTER_RENEW);
    }

    /** 文面三栏：主体代号、层别名目、该发的那一天；少哪几栏由本轮原样报出，谁也不替它补。 */
    private List<String> missingFields(TWsidRemindItem item) {
        List<String> missing = new ArrayList<String>();
        if (StringUtils.isEmpty(item.getHolderNo())) {
            missing.add("主体代号");
        }
        if (StringUtils.isEmpty(item.getLayerName())) {
            missing.add("层别名目");
        }
        if (item.getDueAt() == null) {
            missing.add("该发的那一天");
        }
        return missing;
    }

    /**
     * 上一趟静默搁起、此刻窗已走完：行面仍搁着且记着静默走完那一刻、那一刻已到。
     * 哪怕窗走完时已过夜，qualified_at 仍认当初那一瞬；当日没有回执账，首趟醒来照发不误。
     */
    private boolean isHeldOverSilence(TWsidRemindItem item, Date at) {
        return item.getStatus() != null && item.getStatus() == WsidRemindConsts.STATUS_WAIT
                && item.getSilentToAt() != null
                && !at.before(item.getSilentToAt())
                && item.getQualifiedAt() != null;
    }

    // ============================================================ 去重账

    /** 本键某一日的回执档（一处主体＋一桩事由＋一日，日钉在够上条件那一瞬当日）；按式次、先后升序。 */
    private List<TWsidRemindDispatch> listLedgerOfDay(TWsidRemindItem item, String whichDay) {
        List<TWsidRemindDispatch> ledger = dispatchMapper.selectList(new QueryWrapper<TWsidRemindDispatch>()
                .eq("holder_no", item.getHolderNo())
                .eq("matter_kind", item.getMatterKind())
                .eq("del_flag", 0)
                .orderByAsc("channel_level")
                .orderByAsc("id"));
        List<TWsidRemindDispatch> today = new ArrayList<TWsidRemindDispatch>();
        for (TWsidRemindDispatch d : ledger) {
            if (whichDay.equals(day(d.getQualifiedAt()))) {
                today.add(d);
            }
        }
        return today;
    }

    private TWsidRemindDispatch firstReached(List<TWsidRemindDispatch> attempts) {
        for (TWsidRemindDispatch d : attempts) {
            if (d.getReachStatus() != null && d.getReachStatus() == WsidRemindConsts.REACH_OK) {
                return d;
            }
        }
        return null;
    }

    // ============================================================ 回面装配

    private WsidRemindLineView viewOf(TWsidRemindItem item, Integer reachedLevel, int status, boolean fallback,
                                      List<TWsidRemindDispatch> ledger, String remark) {
        WsidRemindLineView v = baseView(item, reachedLevel, status, fallback, remark);
        attachReceipts(v, ledger);
        return v;
    }

    /** 只对照的趟次：够上条件那一瞬以当日回执账为准，两趟答复才能逐行相同。 */
    private WsidRemindLineView viewOfLedger(TWsidRemindItem item, Integer reachedLevel, int status,
                                            boolean fallback, List<TWsidRemindDispatch> ledger, String remark) {
        WsidRemindLineView v = baseView(item, reachedLevel, status, fallback, remark);
        if (!ledger.isEmpty()) {
            v.setQualifiedAt(ledger.get(0).getQualifiedAt());
        }
        attachReceipts(v, ledger);
        return v;
    }

    private WsidRemindLineView baseView(TWsidRemindItem item, Integer reachedLevel, int status,
                                        boolean fallback, String remark) {
        WsidRemindLineView v = new WsidRemindLineView();
        v.setItemId(item.getId());
        v.setItemNo(item.getItemNo());
        v.setHolderNo(item.getHolderNo());
        v.setMatterKind(item.getMatterKind());
        v.setLayerName(item.getLayerName());
        v.setDueAt(item.getDueAt());
        v.setQualifiedAt(item.getQualifiedAt());
        v.setDispatchAt(item.getDispatchAt());
        v.setStatus(status);
        v.setReachedLevel(reachedLevel);
        v.setFallback(fallback);
        v.setRemark(remark);
        return v;
    }

    private void attachReceipts(WsidRemindLineView v, List<TWsidRemindDispatch> ledger) {
        List<WsidRemindReceiptView> receipts = new ArrayList<WsidRemindReceiptView>();
        for (TWsidRemindDispatch d : ledger) {
            receipts.add(new WsidRemindReceiptView(d.getChannelLevel(), d.getReachStatus(), d.getContent()));
        }
        v.setReceipts(receipts);
    }

    // ============================================================ 时日小工具

    /** 两日期间的整日差（from→to，GMT+8 墙钟）；该发的那一天缺着交不出来。 */
    private Long daysBetween(Date from, Date to) {
        if (from == null || to == null) {
            return null;
        }
        LocalDate a = from.toInstant().atZone(ZONE).toLocalDate();
        LocalDate b = to.toInstant().atZone(ZONE).toLocalDate();
        return ChronoUnit.DAYS.between(a, b);
    }

    private String stamp(Date d) {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(d);
    }

    private String ts(Date d) {
        return d == null ? null : new SimpleDateFormat(TS_PATTERN).format(d);
    }

    private String day(Date d) {
        return d == null ? null : new SimpleDateFormat(DAY_PATTERN).format(d);
    }

    private String joinFields(List<String> fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) {
                sb.append("、");
            }
            sb.append(fields.get(i));
        }
        return sb.toString();
    }
}
