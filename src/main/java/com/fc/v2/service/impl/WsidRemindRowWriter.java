package com.fc.v2.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fc.v2.mapper.auto.TWsidRemindDispatchMapper;
import com.fc.v2.mapper.auto.TWsidRemindItemMapper;
import com.fc.v2.model.auto.TWsidRemindDispatch;
import com.fc.v2.model.auto.TWsidRemindItem;
import com.fc.v2.model.custom.wsid.WsidRemindConsts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 提醒条目逐行落账件：一趟里一行的各档回执与行面状态在同一事务里一起落，
 * 行面与库底只落一处，不许只在屏上闪一下。
 *
 * <p>独立成件是为让事务边界落在去重锁之内：先进的趟次在锁内提交，后到的趟次
 * 拿得到锁时一定读得到当日回执档，只对照不重发，两趟答复才合得拢。</p>
 *
 * @author fuce
 * @date 2026-10-07
 */
@Component
public class WsidRemindRowWriter {

    private static final String TS_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Resource
    private TWsidRemindItemMapper remindItemMapper;

    @Resource
    private TWsidRemindDispatchMapper dispatchMapper;

    @Resource
    private WsidRemindChannelGateway channelGateway;

    /** 缺栏：本行按下不发、不代填，缺哪几栏原样沉进行面。 */
    @Transactional(rollbackFor = Exception.class)
    public void markIncomplete(TWsidRemindItem item, List<String> missing) {
        item.setStatus(WsidRemindConsts.STATUS_INCOMPLETE);
        item.setRemark("文面缺栏，本行按下不发，也不代填：缺" + joinFields(missing));
        updateRow(item);
    }

    /** 静默搁起：不起话，行面只记够上条件那一瞬与窗走完那一刻。 */
    @Transactional(rollbackFor = Exception.class)
    public void markSilentHold(TWsidRemindItem item, Date holdUntil) {
        item.setStatus(WsidRemindConsts.STATUS_WAIT);
        item.setSilentToAt(holdUntil);
        item.setDispatchAt(null);
        item.setRemark("静默时段不起话；起话时刻仍以够上条件那一瞬 "
                + ts(item.getQualifiedAt()) + " 为准");
        updateRow(item);
    }

    /**
     * 按通道取用式挨个退：每试一式落一档（说不通也留档，前档不擦），走通一式即收手；
     * 连最后一式也落不了地，行面沉「发不成」。各档与行面同一事务落库。
     */
    @Transactional(rollbackFor = Exception.class)
    public List<TWsidRemindDispatch> cascade(TWsidRemindItem item, String runId) {
        List<TWsidRemindDispatch> attempts = new ArrayList<TWsidRemindDispatch>();
        int[] levels;
        Integer kind = item.getChannelKind();
        if (kind != null && kind == WsidRemindConsts.CHANNEL_KIND_ONLY_STATION) {
            levels = new int[]{WsidRemindConsts.CHANNEL_STATION};
        } else if (kind != null && kind == WsidRemindConsts.CHANNEL_KIND_STATION_SMS) {
            levels = new int[]{WsidRemindConsts.CHANNEL_STATION, WsidRemindConsts.CHANNEL_SMS};
        } else if (kind != null && kind == WsidRemindConsts.CHANNEL_KIND_CASCADE) {
            levels = new int[]{WsidRemindConsts.CHANNEL_STATION, WsidRemindConsts.CHANNEL_SMS,
                    WsidRemindConsts.CHANNEL_VISIT};
        } else {
            item.setStatus(WsidRemindConsts.STATUS_FAIL);
            item.setDispatchAt(null);
            item.setSilentToAt(null);
            item.setRemark("发不成：通道取用式不在三式之内");
            updateRow(item);
            return attempts;
        }

        String text = buildText(item);
        for (int level : levels) {
            WsidRemindChannelGateway.Receipt receipt = channelGateway.dispatch(level, item, runId, text);
            TWsidRemindDispatch d = newDispatch(item, runId, level,
                    receipt.isOk() ? WsidRemindConsts.REACH_OK : WsidRemindConsts.REACH_FAIL,
                    receipt.getContent());
            dispatchMapper.insert(d); // 旧记号留着，不为省一行擦掉前一档
            attempts.add(d);
            if (receipt.isOk()) {
                item.setStatus(WsidRemindConsts.STATUS_SENT);
                item.setDispatchAt(new Date());
                item.setSilentToAt(null);
                item.setRemark(null);
                updateRow(item);
                return attempts; // 一趟里一个条目只准走通一式
            }
        }
        item.setStatus(WsidRemindConsts.STATUS_FAIL);
        item.setDispatchAt(null);
        item.setSilentToAt(null);
        item.setRemark("发不成：" + attempts.get(attempts.size() - 1).getContent());
        updateRow(item);
        return attempts;
    }

    private TWsidRemindDispatch newDispatch(TWsidRemindItem item, String runId, int level, int reach, String content) {
        TWsidRemindDispatch d = new TWsidRemindDispatch();
        d.setRunId(runId);
        d.setItemId(item.getId());
        d.setHolderNo(item.getHolderNo());
        d.setMatterKind(item.getMatterKind());
        d.setQualifiedAt(item.getQualifiedAt());
        d.setChannelLevel(level);
        d.setReachStatus(reach);
        d.setContent(content);
        d.setDelFlag(0);
        return d;
    }

    private void updateRow(TWsidRemindItem item) {
        UpdateWrapper<TWsidRemindItem> uw = new UpdateWrapper<TWsidRemindItem>()
                .eq("id", item.getId())
                .eq("del_flag", 0);
        uw.set("status", item.getStatus());
        uw.set("qualified_at", item.getQualifiedAt() == null ? null : ts(item.getQualifiedAt()));
        uw.set("dispatch_at", item.getDispatchAt() == null ? null : ts(item.getDispatchAt()));
        uw.set("silent_to_at", item.getSilentToAt() == null ? null : ts(item.getSilentToAt()));
        uw.set("remark", item.getRemark());
        uw.set("update_time", ts(new Date()));
        remindItemMapper.update(null, uw);
    }

    private String buildText(TWsidRemindItem item) {
        String matter = item.getMatterKind() == WsidRemindConsts.MATTER_AUDIT ? "年度核查应交" : "卡管届满换证";
        String layer = item.getLayerName() == null ? "" : item.getLayerName();
        String day = item.getDueAt() == null ? "日子缺着"
                : new SimpleDateFormat("yyyy-MM-dd").format(item.getDueAt());
        return matter + " " + day + "，请按期办理（" + layer + "）。"
                + (item.getContent() == null ? "" : item.getContent());
    }

    private String ts(Date d) {
        return new SimpleDateFormat(TS_PATTERN).format(d);
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
