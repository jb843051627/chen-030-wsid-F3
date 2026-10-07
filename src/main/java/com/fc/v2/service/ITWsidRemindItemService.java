package com.fc.v2.service;

import com.fc.v2.model.auto.TWsidRemindItem;
import com.fc.v2.model.custom.wsid.WsidRemindScanCmd;
import com.fc.v2.model.custom.wsid.WsidRemindScanResult;

/**
 * 年度核查与换证提醒条目 Service 接口。
 *
 * <p>六问只许 {@link #scan} 这一条服务层扫描方法开口，入参出参就此钉住：</p>
 * <ol>
 *   <li>本轮捞得着哪几行（够不够日子只在这里定夺，空/负/越上限的提前量本轮不动）；</li>
 *   <li>话从哪一路递出去（三式挨个退，退到第几式记第几式，前档不擦）；</li>
 *   <li>静默那一截算不算当日起话（册上配置说了算，起话仍记当初够上条件那一瞬）；</li>
 *   <li>隔日再扫认不认作同一桩（去重键＝一处主体＋一桩事由＋一日）；</li>
 *   <li>行上文面短了栏走哪条道（本行按下不发、不代填，缺栏原样报上，发成发不成都沉库底）；</li>
 *   <li>末了三笔数打哪儿来（捞着／走通／回落，与逐行记号同一次算定，空跑只落「本轮空跑」）。</li>
 * </ol>
 *
 * <p>页面只准先拿依据再摆数，旁处不留第二道门；日后要把一趟圈在单一物种底下，
 * 另立一条并列的名目去接，不许在这条本体上动手术、不改签名。</p>
 *
 * @author fuce
 * @date 2026-09-14
 */
public interface ITWsidRemindItemService {

    /** 按主键回查条目（只读账，不定够不够日子、不点数） */
    TWsidRemindItem selectTWsidRemindItemById(Long id);

    /**
     * 按日照跑一轮：挑行 → 回落 → 静默 → 缺栏停单行 → 同日去重 → 交三笔数。
     * 六问只此一口；同一刻两趟分开排队、各留各的先后，两趟答复相同。
     */
    WsidRemindScanResult scan(WsidRemindScanCmd cmd);
}
