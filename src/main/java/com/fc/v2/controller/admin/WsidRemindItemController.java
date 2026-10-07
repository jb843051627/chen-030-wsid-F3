package com.fc.v2.controller.admin;

import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.log.Log;
import com.fc.v2.model.custom.wsid.WsidRemindScanCmd;
import com.fc.v2.service.ITWsidRemindItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 年度核查与换证提醒 Controller（scheduling-job 形状：按日照跑）。
 *
 * <p>这轮界面不改，这里只出接口。六问只许服务层那一条扫描方法开口：挑行、回落、
 * 静默、缺栏、同日去重、三笔交数全在 scan 里一次算定。页面只拿 scan 回来的一份
 * 摆数，不许先亮数字再回头找依据，旁处不留第二道门。定时任务直接调
 * {@code tWsidRemindItemServiceImpl.scan}，同一刻手按两回也是各跑各的趟、答复相同。</p>
 *
 * @author fuce
 * @date 2026-10-07
 */
@Api(value = "年度核查与换证提醒")
@Controller
@RequestMapping("/wsidRemindItem")
public class WsidRemindItemController extends BaseController {

    @Autowired
    private ITWsidRemindItemService wsidRemindItemService;

    @Log(title = "年度核查与换证提醒·按日照跑", action = "scan")
    @ApiOperation(value = "按日照跑一轮：挑行→回落→静默→缺栏停单行→同日只一回→交三笔数",
            notes = "六问只此一口；空跑只落「本轮空跑」")
    @PostMapping("/scan")
    @RequiresPermissions("wsid:wsidRemind:scan")
    @ResponseBody
    public AjaxResult scan(@RequestBody(required = false) WsidRemindScanCmd cmd) {
        return AjaxResult.success(200, "本轮已照跑", wsidRemindItemService.scan(cmd));
    }

    @ApiOperation(value = "按主键回查条目（只读账，不挑行不点数）", notes = "回查条目")
    @GetMapping("/{id}")
    @RequiresPermissions("wsid:wsidRemind:scan")
    @ResponseBody
    public AjaxResult get(@PathVariable("id") Long id) {
        Object item = wsidRemindItemService.selectTWsidRemindItemById(id);
        return item == null ? AjaxResult.error("条目查不着")
                : AjaxResult.success(200, "条目在此", item);
    }
}
