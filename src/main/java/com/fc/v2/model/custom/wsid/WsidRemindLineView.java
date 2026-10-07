package com.fc.v2.model.custom.wsid;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * 本轮里一行落定的回面：够日子、缺栏、静默、回落第几式，逐行答得上。
 *
 * @author fuce
 * @date 2026-10-07
 */
@ApiModel(value = "WsidRemindLineView", description = "本轮逐行回面")
public class WsidRemindLineView {

    @ApiModelProperty(value = "条目主键")
    private Long itemId;

    @ApiModelProperty(value = "条目代号")
    private String itemNo;

    @ApiModelProperty(value = "一处主体：繁育主体册代号（缺栏时为空）")
    private String holderNo;

    @ApiModelProperty(value = "一桩事由 1年度核查 2换证")
    private Integer matterKind;

    @ApiModelProperty(value = "层别名目（缺栏时为空）")
    private String layerName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "应交日／届满日（缺栏时为空）")
    private java.util.Date dueAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "够上条件的那一瞬（撞静默也记这一瞬，不记时段过后）")
    private java.util.Date qualifiedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "实际起话那一刻（静默搁起时为空）")
    private java.util.Date dispatchAt;

    @ApiModelProperty(value = "本行情形 0搁着(静默) 1已发出 2发不成 3缺栏按下不发")
    private Integer status;

    @ApiModelProperty(value = "走通的是第几式 1站内 2短信息 3上门告知（未走通为空）")
    private Integer reachedLevel;

    @ApiModelProperty(value = "回落几行记数用：本行试了不止一式即为回落行")
    private boolean fallback;

    @ApiModelProperty(value = "缺着的栏（原样报出：主体代号/层别名目/该发的那一天）；不缺为空")
    private List<String> missingFields = new ArrayList<String>();

    @ApiModelProperty(value = "本式各档回执，试过的每一式各一档，旧档不擦")
    private List<WsidRemindReceiptView> receipts = new ArrayList<WsidRemindReceiptView>();

    @ApiModelProperty(value = "行面一记（缺栏/发不成/静默缘由）")
    private String remark;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemNo() {
        return itemNo;
    }

    public void setItemNo(String itemNo) {
        this.itemNo = itemNo;
    }

    public String getHolderNo() {
        return holderNo;
    }

    public void setHolderNo(String holderNo) {
        this.holderNo = holderNo;
    }

    public Integer getMatterKind() {
        return matterKind;
    }

    public void setMatterKind(Integer matterKind) {
        this.matterKind = matterKind;
    }

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public java.util.Date getDueAt() {
        return dueAt;
    }

    public void setDueAt(java.util.Date dueAt) {
        this.dueAt = dueAt;
    }

    public java.util.Date getQualifiedAt() {
        return qualifiedAt;
    }

    public void setQualifiedAt(java.util.Date qualifiedAt) {
        this.qualifiedAt = qualifiedAt;
    }

    public java.util.Date getDispatchAt() {
        return dispatchAt;
    }

    public void setDispatchAt(java.util.Date dispatchAt) {
        this.dispatchAt = dispatchAt;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getReachedLevel() {
        return reachedLevel;
    }

    public void setReachedLevel(Integer reachedLevel) {
        this.reachedLevel = reachedLevel;
    }

    public boolean isFallback() {
        return fallback;
    }

    public void setFallback(boolean fallback) {
        this.fallback = fallback;
    }

    public List<String> getMissingFields() {
        return missingFields;
    }

    public void setMissingFields(List<String> missingFields) {
        this.missingFields = missingFields;
    }

    public List<WsidRemindReceiptView> getReceipts() {
        return receipts;
    }

    public void setReceipts(List<WsidRemindReceiptView> receipts) {
        this.receipts = receipts;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
