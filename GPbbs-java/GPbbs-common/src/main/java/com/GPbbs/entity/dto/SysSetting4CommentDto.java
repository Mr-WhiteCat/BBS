package com.GPbbs.entity.dto;

import com.GPbbs.annotation.VerifyParam;

/**
 * 评论设置
 */

public class SysSetting4CommentDto {
    /**
     * 评论积分
     */
    @VerifyParam(required = true)
    private Integer commentPoints;

    /**
     * 评论数量阈值
     */
    @VerifyParam(required = true)
    private Integer commentDayCountThreshold;

    /**
     * 评论是否展开
     */
    @VerifyParam(required = true)
    private Boolean commentOpen;

    public Boolean getCommentOpen() {
        return commentOpen;
    }

    public void setCommentOpen(Boolean commentOpen) {
        this.commentOpen = commentOpen;
    }

    public Integer getCommentPoints() {
        return commentPoints;
    }

    public void setCommentPoints(Integer commentPoints) {
        this.commentPoints = commentPoints;
    }

    public Integer getCommentDayCountThreshold() {
        return commentDayCountThreshold;
    }

    public void setCommentDayCountThreshold(Integer commentDayCountThreshold) {
        this.commentDayCountThreshold = commentDayCountThreshold;
    }
}
