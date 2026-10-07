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
import java.util.Date;

/**
 * 提醒条目通道回落回执档对象 t_wsid_remind_dispatch
 *
 * <p>一趟里一个条目每试一式落一档：前一式说不通也留着，不许为省一行擦掉旧记号；
 * 落到第几式走通，行面就记第几式。</p>
 *
 * @author fuce
 * @date 2026-10-07
 */
@TableName("t_wsid_remind_dispatch")
@ApiModel(value = "TWsidRemindDispatch", description = "提醒条目通道回落回执档")
public class TWsidRemindDispatch implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 本轮扫描趟号（同一刻开的两趟各号各的） */
    @TableField("run_id")
    @ApiModelProperty(value = "本轮扫描趟号（同一刻开的两趟各号各的）")
    private String runId;

    /** 所对提醒条目 */
    @TableField("item_id")
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "所对提醒条目")
    private Long itemId;

    /** 一处主体（冗余自条目，对流水用） */
    @TableField("holder_no")
    @ApiModelProperty(value = "一处主体（冗余自条目，对流水用）")
    private String holderNo;

    /** 一桩事由 1年度核查 2换证 */
    @TableField("matter_kind")
    @ApiModelProperty(value = "一桩事由 1年度核查 2换证")
    private Integer matterKind;

    /** 够上条件的那一瞬（抄自条目） */
    @TableField("qualified_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "够上条件的那一瞬（抄自条目）")
    private Date qualifiedAt;

    /** 落到第几式 1站内 2短信息 3上门告知 */
    @TableField("channel_level")
    @ApiModelProperty(value = "落到第几式 1站内 2短信息 3上门告知")
    private Integer channelLevel;

    /** 本式结果 0说不通 1走通 */
    @TableField("reach_status")
    @ApiModelProperty(value = "本式结果 0说不通 1走通")
    private Integer reachStatus;

    /** 本式回执原话（失败缘由也落这里） */
    @TableField("content")
    @ApiModelProperty(value = "本式回执原话（失败缘由也落这里）")
    private String content;

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

    /** 备注 */
    @TableField("remark")
    @ApiModelProperty(value = "备注")
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
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

    public Date getQualifiedAt() {
        return qualifiedAt;
    }

    public void setQualifiedAt(Date qualifiedAt) {
        this.qualifiedAt = qualifiedAt;
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
