package com.GPbbs.entity.dto;

import com.GPbbs.annotation.VerifyParam;


/**
 * 发帖设置
 */
public class SysSetting4PostDto {
    /**
     * 发帖积分
     */
    @VerifyParam(required = true)
    private Integer postPoints;

    /**
     * 一天发帖数量
     */
    @VerifyParam(required = true)
    private Integer postDayCountThreshold;

    /**
     * 每天上传图片数量
     */
    @VerifyParam(required = true)
    private Integer dayImageUploadCount;

    /**
     * 附件大小 单位 mb
     */
    @VerifyParam(required = true)
    private Integer attachmentSize;

    public Integer getPostPoints() {
        return postPoints;
    }

    public void setPostPoints(Integer postPoints) {
        this.postPoints = postPoints;
    }

    public Integer getPostDayCountThreshold() {
        return postDayCountThreshold;
    }

    public void setPostDayCountThreshold(Integer postDayCountThreshold) {
        this.postDayCountThreshold = postDayCountThreshold;
    }

    public Integer getDayImageUploadCount() {
        return dayImageUploadCount;
    }

    public void setDayImageUploadCount(Integer dayImageUploadCount) {
        this.dayImageUploadCount = dayImageUploadCount;
    }

    public Integer getAttachmentSize() {
        return attachmentSize;
    }

    public void setAttachmentSize(Integer attachmentSize) {
        this.attachmentSize = attachmentSize;
    }

}
