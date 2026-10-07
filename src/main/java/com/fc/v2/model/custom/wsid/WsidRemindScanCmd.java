package com.fc.v2.model.custom.wsid;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

/**
 * 一趟日照扫描的入参。
 *
 * <p>一轮只认开跑那一刻：{@code at} 空着就取系统当下；{@code runId} 空着由本轮另号一趟，
 * 同一刻手按两回也各号各的，不并成一趟。</p>
 *
 * @author fuce
 * @date 2026-10-07
 */
@ApiModel(value = "WsidRemindScanCmd", description = "年度核查与换证提醒·一轮扫描入参")
public class WsidRemindScanCmd {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "开跑那一刻（空着取当下；一轮够不够日子只认这一刻）")
    private Date at;

    @ApiModelProperty(value = "本轮趟号（空着系统另号；同一刻两趟各传各的号，不许并趟）")
    private String runId;

    public Date getAt() {
        return at;
    }

    public void setAt(Date at) {
        this.at = at;
    }

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }
}
