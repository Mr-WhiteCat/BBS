package com.GPbbs.entity.dto;

import com.GPbbs.annotation.VerifyParam;

public class SysSetting4LikeDto {
    /**
     * 点赞数量阈值
     */
    @VerifyParam(required = true)
    private Integer likeDayCountThreshold;

    public Integer getLikeDayCountThreshold() {
        return likeDayCountThreshold;
    }

    public void setLikeDayCountThreshold(Integer likeDayCountThreshold) {
        this.likeDayCountThreshold = likeDayCountThreshold;
    }
}
