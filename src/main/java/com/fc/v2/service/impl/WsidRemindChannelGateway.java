package com.fc.v2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.common.conf.redis.RedisService;
import com.fc.v2.mapper.auto.TWsidHolderBookMapper;
import com.fc.v2.model.auto.TWsidHolderBook;
import com.fc.v2.model.auto.TWsidRemindItem;
import com.fc.v2.model.custom.wsid.WsidRemindConsts;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;

/**
 * 提醒话头的三式发送门——全系统只此一处递话，扫描方法之外不留第二道门。
 *
 * <p>三式各有各的「说不通」由头，缘由原样回：</p>
 * <ul>
 *   <li>站内：主体在繁育主体底册里没有在册户头，或站内信箱写不进，即说不通；</li>
 *   <li>短信息：条目那一栏没有可用号子（空着）即说不通；</li>
 *   <li>上门告知：落告知单为准，落不下去才算说不通。</li>
 * </ul>
 *
 * <p>同一刻两趟各号各的 runId，两门各写各的记号，互不挤掉。</p>
 *
 * @author fuce
 * @date 2026-10-07
 */
@Component
public class WsidRemindChannelGateway {

    @Resource
    private TWsidHolderBookMapper holderBookMapper;

    @Resource
    private RedisService redisService;

    /** 一式的回执：走通与否 + 原话。 */
    public static class Receipt {
        private final boolean ok;
        private final String content;

        public Receipt(boolean ok, String content) {
            this.ok = ok;
            this.content = content;
        }

        public boolean isOk() {
            return ok;
        }

        public String getContent() {
            return content;
        }
    }

    /** 按第几式递话；第几式不在三式之内，按说不通认，不自己改道。 */
    public Receipt dispatch(int level, TWsidRemindItem item, String runId, String text) {
        if (level == WsidRemindConsts.CHANNEL_STATION) {
            return station(item, runId, text);
        }
        if (level == WsidRemindConsts.CHANNEL_SMS) {
            return sms(item, runId, text);
        }
        if (level == WsidRemindConsts.CHANNEL_VISIT) {
            return visit(item, runId, text);
        }
        return new Receipt(false, "第 " + level + " 式不在三式之内，递不出去");
    }

    /** 站内信：主体在底册里得有在册户头；信箱写不进也算说不通。 */
    private Receipt station(TWsidRemindItem item, String runId, String text) {
        String holderNo = item.getHolderNo();
        try {
            int cnt = holderBookMapper.selectCount(new QueryWrapper<TWsidHolderBook>()
                    .eq("holder_no", holderNo)
                    .eq("del_flag", 0));
            if (cnt <= 0) {
                return new Receipt(false, "站内说不通：主体「" + holderNo + "」在繁育主体底册里没有在册户头");
            }
            String key = "wsid:remind:station:inbox:" + holderNo + ":" + runId + ":" + item.getId();
            redisService.setCacheObject(key, text);
            return new Receipt(true, "站内信已投「" + holderNo + "」信箱（趟号 " + runId + "）");
        } catch (Exception e) {
            return new Receipt(false, "站内说不通：信箱写不进——" + e.getMessage());
        }
    }

    /** 短信息：那一栏没有可用号子即说不通，由扫描方法换下一式。 */
    private Receipt sms(TWsidRemindItem item, String runId, String text) {
        String smsNo = item.getSmsNo() == null ? null : item.getSmsNo().trim();
        if (smsNo == null || smsNo.isEmpty()) {
            return new Receipt(false, "短信息说不通：短信息那一栏没有可用号子");
        }
        try {
            String key = "wsid:remind:sms:sent:" + runId + ":" + item.getId();
            redisService.setCacheObject(key, "发往 " + smsNo + "：" + text);
            return new Receipt(true, "短信息已发往号子 " + smsNo + "（趟号 " + runId + "）");
        } catch (Exception e) {
            return new Receipt(false, "短信息说不通：网关发不出去——" + e.getMessage());
        }
    }

    /** 上门告知：落一张告知单；落不下去才算说不通。 */
    private Receipt visit(TWsidRemindItem item, String runId, String text) {
        try {
            String key = "wsid:remind:visit:sheet:" + runId + ":" + item.getId();
            String sheet = "上门告知单（趟号 " + runId + "）主体「" + item.getHolderNo()
                    + "」" + matterName(item.getMatterKind()) + " " + day(item.getDueAt()) + "：" + text;
            redisService.setCacheObject(key, sheet);
            return new Receipt(true, "上门告知单已落（主体「" + item.getHolderNo() + "」，趟号 " + runId + "）");
        } catch (Exception e) {
            return new Receipt(false, "上门告知说不通：告知单落不下去——" + e.getMessage());
        }
    }

    private String matterName(Integer matterKind) {
        if (matterKind != null && matterKind == WsidRemindConsts.MATTER_AUDIT) {
            return "年度核查";
        }
        if (matterKind != null && matterKind == WsidRemindConsts.MATTER_RENEW) {
            return "换证";
        }
        return "事由不明";
    }

    private String day(java.util.Date d) {
        return d == null ? "日子缺着" : new SimpleDateFormat("yyyy-MM-dd").format(d);
    }
}
