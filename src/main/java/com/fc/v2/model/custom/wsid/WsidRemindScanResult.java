package com.fc.v2.model.custom.wsid;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 一轮日照扫描的出参：三笔数与逐行记号出自同一次算定，只信本轮返回。
 *
 * <p>一行没捞着是合法结果，只落「本轮空跑」，不牵首行填数、不抄上一轮。</p>
 *
 * @author fuce
 * @date 2026-10-07
 */
@ApiModel(value = "WsidRemindScanResult", description = "年度核查与换证提醒·一轮扫描回面")
public class WsidRemindScanResult {

    @ApiModelProperty(value = "本轮趟号")
    private String runId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "本轮开跑那一刻")
    private Date at;

    @ApiModelProperty(value = "本轮捞着几行")
    private int pickedNum;

    @ApiModelProperty(value = "走通几行（三式里落了一式）")
    private int sentNum;

    @ApiModelProperty(value = "回落几行（试过不止一式；含回落走通与回落到底发不成）")
    private int fallbackNum;

    @ApiModelProperty(value = "发不成几行（连最后一式也落不了地）")
    private int failNum;

    @ApiModelProperty(value = "缺栏按下不发几行")
    private int incompleteNum;

    @ApiModelProperty(value = "静默搁起几行（够日子但不当日起话）")
    private int silentHoldNum;

    @ApiModelProperty(value = "本轮是否空跑（一行没捞着）")
    private boolean emptyRun;

    @ApiModelProperty(value = "屏面一句话：空跑只落「本轮空跑」")
    private String content;

    @ApiModelProperty(value = "逐行记号（与三笔数同一次算定）")
    private List<WsidRemindLineView> lines = new ArrayList<WsidRemindLineView>();

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public Date getAt() {
        return at;
    }

    public void setAt(Date at) {
        this.at = at;
    }

    public int getPickedNum() {
        return pickedNum;
    }

    public void setPickedNum(int pickedNum) {
        this.pickedNum = pickedNum;
    }

    public int getSentNum() {
        return sentNum;
    }

    public void setSentNum(int sentNum) {
        this.sentNum = sentNum;
    }

    public int getFallbackNum() {
        return fallbackNum;
    }

    public void setFallbackNum(int fallbackNum) {
        this.fallbackNum = fallbackNum;
    }

    public int getFailNum() {
        return failNum;
    }

    public void setFailNum(int failNum) {
        this.failNum = failNum;
    }

    public int getIncompleteNum() {
        return incompleteNum;
    }

    public void setIncompleteNum(int incompleteNum) {
        this.incompleteNum = incompleteNum;
    }

    public int getSilentHoldNum() {
        return silentHoldNum;
    }

    public void setSilentHoldNum(int silentHoldNum) {
        this.silentHoldNum = silentHoldNum;
    }

    public boolean isEmptyRun() {
        return emptyRun;
    }

    public void setEmptyRun(boolean emptyRun) {
        this.emptyRun = emptyRun;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<WsidRemindLineView> getLines() {
        return lines;
    }

    public void setLines(List<WsidRemindLineView> lines) {
        this.lines = lines;
    }
}
