package com.fc.v2.model.custom.wsid;

/**
 * 年度核查与换证提醒一摊的定数。
 *
 * <p>提醒时点由册上口径定：核查往应交之前数、换证往届满之前数；系统不自己拍日子。
 * 通道三式、条目去处在册上写死，第四样存不进表里。</p>
 *
 * @author fuce
 * @date 2026-10-07
 */
public final class WsidRemindConsts {

    private WsidRemindConsts() {
    }

    /** 条目情形：搁着（够上条件但静默没走完，不起话） */
    public static final int STATUS_WAIT = 0;

    /** 条目情形：已发出（三式里走通了一式，行面记走通那一档） */
    public static final int STATUS_SENT = 1;

    /** 条目情形：发不成（连最后一式也落不了地，沉进行面不许只闪屏） */
    public static final int STATUS_FAIL = 2;

    /** 条目情形：文面缺栏按下不发（不代填，缺哪几栏原样报上） */
    public static final int STATUS_INCOMPLETE = 3;

    /** 一桩事由：年度核查（往应交之前数日） */
    public static final int MATTER_AUDIT = 1;

    /** 一桩事由：换证（往卡管届满之前数日） */
    public static final int MATTER_RENEW = 2;

    /** 通道第一式：只走站内（站内信） */
    public static final int CHANNEL_STATION = 1;

    /** 通道第二式：短信息（那一栏没有可用号子即说不通） */
    public static final int CHANNEL_SMS = 2;

    /** 通道第三式：上门告知 */
    public static final int CHANNEL_VISIT = 3;

    /** 本式回执：说不通（回落的由头，旧档不擦） */
    public static final int REACH_FAIL = 0;

    /** 本式回执：走通（一趟里一个条目只准走通一式） */
    public static final int REACH_OK = 1;

    /** 通道取用式：只走站内 */
    public static final int CHANNEL_KIND_ONLY_STATION = 0;

    /** 通道取用式：先站内后短信息 */
    public static final int CHANNEL_KIND_STATION_SMS = 1;

    /** 通道取用式：三式挨个退（站内→短信息→上门告知） */
    public static final int CHANNEL_KIND_CASCADE = 2;

    /**
     * 「至多许提前几日」的定数上限：栏里写的数大过它，这一行本轮不动。
     * 县里口径：再长的提前量也不许越过一个节令，90 日为册上定死的上沿。
     */
    public static final int MAX_LEAD_DAYS = 90;
}
