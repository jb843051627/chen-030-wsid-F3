package com.fc.v2.model.custom.wsid;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 一趟里一式通道的回执面（一档一式，前一式的旧记号不擦）。
 *
 * @author fuce
 * @date 2026-10-07
 */
@ApiModel(value = "WsidRemindReceiptView", description = "一式通道的回执")
public class WsidRemindReceiptView {

    @ApiModelProperty(value = "第几式 1站内 2短信息 3上门告知")
    private Integer channelLevel;

    @ApiModelProperty(value = "本式结果 0说不通 1走通")
    private Integer reachStatus;

    @ApiModelProperty(value = "本式回执原话（说不通的缘由也在这里）")
    private String content;

    public WsidRemindReceiptView() {
    }

    public WsidRemindReceiptView(Integer channelLevel, Integer reachStatus, String content) {
        this.channelLevel = channelLevel;
        this.reachStatus = reachStatus;
        this.content = content;
    }

    public Integer getChannelLevel() {
        return channelLevel;
    }

    public void setChannelLevel(Integer channelLevel) {
        this.channelLevel = channelLevel;
    }

    public Integer getReachStatus() {
        return reachStatus;
    }

    public void setReachStatus(Integer reachStatus) {
        this.reachStatus = reachStatus;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
