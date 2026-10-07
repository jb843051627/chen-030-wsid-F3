package com.fc.v2.model.custom.wsid;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 提醒日照的册上配置（前缀 {@code wsid.remind}）。
 *
 * <p>静默那一截开不开窗，只由这两栏说了算：{@code silence-enabled} 不开，起止写着也不认；
 * 开关开着而起止空着／写得不成时刻，与没开窗等价。程序不许自己掐时辰。</p>
 *
 * <p>提醒撞上静默取哪一条写死在册上：静默赢——够日子也不当日起话，等这一截走完再起，
 * 起话时刻仍记当初够上条件的那一瞬。</p>
 *
 * @author fuce
 * @date 2026-10-07
 */
@Component
@ConfigurationProperties(prefix = "wsid.remind")
public class WsidRemindProperties {

    private static final String TS_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /** 静默开窗开关：县里开会务期不对外发话；默认关，程序不自己掐时辰 */
    private boolean silenceEnabled = false;

    /** 静默起（含），yyyy-MM-dd HH:mm:ss 字串 */
    private String silenceFrom;

    /** 静默止（不含）：这一刻走完才许起话 */
    private String silenceTo;

    public boolean isSilenceEnabled() {
        return silenceEnabled;
    }

    public void setSilenceEnabled(boolean silenceEnabled) {
        this.silenceEnabled = silenceEnabled;
    }

    public String getSilenceFrom() {
        return silenceFrom;
    }

    public void setSilenceFrom(String silenceFrom) {
        this.silenceFrom = silenceFrom;
    }

    public String getSilenceTo() {
        return silenceTo;
    }

    public void setSilenceTo(String silenceTo) {
        this.silenceTo = silenceTo;
    }

    /**
     * 给一个开跑时刻，撞上静默就还静默走完那一刻（止，不含）；没开窗或没撞上给 null。
     * 开窗但起止残缺／写得不成时刻／起不先于止，按没开窗认——程序不替配置兜底掐时辰。
     */
    public Date silenceHoldUntil(Date at) {
        if (!silenceEnabled || at == null) {
            return null;
        }
        Date from = parse(silenceFrom);
        Date to = parse(silenceTo);
        if (from == null || to == null || !from.before(to)) {
            return null;
        }
        // [from, to)：at 落在这一截里，搁到 to
        if (!at.before(from) && at.before(to)) {
            return to;
        }
        return null;
    }

    private static Date parse(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return new SimpleDateFormat(TS_PATTERN).parse(text.trim());
        } catch (ParseException e) {
            throw new IllegalArgumentException("wsid.remind 静默时刻写得不成「" + TS_PATTERN + "」：" + text, e);
        }
    }
}
