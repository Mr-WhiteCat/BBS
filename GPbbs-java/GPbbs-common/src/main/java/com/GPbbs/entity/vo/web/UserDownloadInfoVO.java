package com.GPbbs.entity.vo.web;

public class UserDownloadInfoVO {
    private Integer userPoints;
    private boolean haveDownload;

    public Integer getUserPoints() {
        return userPoints;
    }

    public void setUserPoints(Integer userPoints) {
        this.userPoints = userPoints;
    }

    public boolean isHaveDownload() {
        return haveDownload;
    }

    public void setHaveDownload(boolean haveDownload) {
        this.haveDownload = haveDownload;
    }
}
