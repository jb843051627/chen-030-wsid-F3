package com.fc.v2.model.auto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 年度核查与换证提醒条目对象 t_wsid_remind_item
 *
 * @author fuce
 * @date 2026-09-12
 */
@TableName("t_wsid_remind_item")
@ApiModel(value = "TWsidRemindItem", description = "年度核查与换证提醒条目")
public class TWsidRemindItem implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 年度核查与换证提醒条目代号 */
    @TableField("item_no")
    @ApiModelProperty(value = "年度核查与换证提醒条目代号")
    private String itemNo;

    /** 一处主体：繁育主体册代号 */
    @TableField("holder_no")
    @ApiModelProperty(value = "一处主体：繁育主体册代号")
    private String holderNo;

    /** 一桩事由 1年度核查 2换证 */
    @TableField("matter_kind")
    @ApiModelProperty(value = "一桩事由 1年度核查 2换证")
    private Integer matterKind;

    /** 层别名目（甲层/乙层/丙层） */
    @TableField("layer_name")
    @ApiModelProperty(value = "层别名目（甲层/乙层/丙层）")
    private String layerName;

    /** 应交日（核查）／卡管届满日（换证） */
    @TableField("due_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "应交日（核查）／卡管届满日（换证）")
    private Date dueAt;

    /** 够上条件的那一瞬（本轮认定、年底对流水只认它，不因静默改记） */
    @TableField("qualified_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "够上条件的那一瞬（本轮认定、年底对流水只认它，不因静默改记）")
    private Date qualifiedAt;

    /** 实际起话那一刻（静默走完再起时晚于 qualified_at） */
    @TableField("dispatch_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "实际起话那一刻（静默走完再起时晚于 qualified_at）")
    private Date dispatchAt;

    /** 本行撞上的静默走完那一刻（没撞为空） */
    @TableField("silent_to_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "本行撞上的静默走完那一刻（没撞为空）")
    private Date silentToAt;

    /** 至多许提前几日（空着/负数/大过定数上限的行本轮不捞） */
    @TableField("amount")
    @ApiModelProperty(value = "至多许提前几日（空着/负数/大过定数上限的行本轮不捞）")
    private BigDecimal amount;

    /** 通道取用式 0只走站内 1先站内后短信息 2三式挨个退 */
    @TableField("channel_kind")
    @ApiModelProperty(value = "通道取用式 0只走站内 1先站内后短信息 2三式挨个退")
    private Integer channelKind;

    /** 事由与所对主体的记要 */
    @TableField("content")
    @ApiModelProperty(value = "事由与所对主体的记要")
    private String content;

    /** 短信息可用号子（空着则短信息一式走不通） */
    @TableField("sms_no")
    @ApiModelProperty(value = "短信息可用号子（空着则短信息一式走不通）")
    private String smsNo;

    /** 条目情形 0搁着 1已发出 2发不成 3文面缺栏按下不发 */
    @TableField("status")
    @ApiModelProperty(value = "条目情形 0搁着 1已发出 2发不成 3文面缺栏按下不发")
    private Integer status;

    /** 删除标记 0正常 1删除 */
    @TableField("del_flag")
    @ApiModelProperty(value = "删除标记 0正常 1删除")
    private Integer delFlag;

    /** 创建者 */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建者")
    private String createBy;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /** 更新者 */
    @TableField(value = "update_by", fill = FieldFill.UPDATE)
    @ApiModelProperty(value = "更新者")
    private String updateBy;

    /** 更新时间 */
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    /** 备注（缺哪几栏/发不成的原样一记） */
    @TableField("remark")
    @ApiModelProperty(value = "备注（缺哪几栏/发不成的原样一记）")
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Date getDueAt() {
        return dueAt;
    }

    public void setDueAt(Date dueAt) {
        this.dueAt = dueAt;
    }

    public Date getQualifiedAt() {
        return qualifiedAt;
    }

    public void setQualifiedAt(Date qualifiedAt) {
        this.qualifiedAt = qualifiedAt;
    }

    public Date getDispatchAt() {
        return dispatchAt;
    }

    public void setDispatchAt(Date dispatchAt) {
        this.dispatchAt = dispatchAt;
    }

    public Date getSilentToAt() {
        return silentToAt;
    }

    public void setSilentToAt(Date silentToAt) {
        this.silentToAt = silentToAt;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getChannelKind() {
        return channelKind;
    }

    public void setChannelKind(Integer channelKind) {
        this.channelKind = channelKind;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSmsNo() {
        return smsNo;
    }

    public void setSmsNo(String smsNo) {
        this.smsNo = smsNo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
