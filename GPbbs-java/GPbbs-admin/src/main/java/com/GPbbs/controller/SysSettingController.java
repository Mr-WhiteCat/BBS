package com.GPbbs.controller;

import com.GPbbs.annotation.GlobalInterceptor;
import com.GPbbs.annotation.VerifyParam;
import com.GPbbs.controller.base.ABaseController;
import com.GPbbs.entity.config.AdminConfig;
import com.GPbbs.entity.dto.*;
import com.GPbbs.entity.vo.ResponseVO;
import com.GPbbs.exception.BusinessException;
import com.GPbbs.service.SysSettingService;
import com.GPbbs.utils.JsonUtils;
import com.GPbbs.utils.OKHttpUtils;
import com.GPbbs.utils.StringTools;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/setting")
public class SysSettingController extends ABaseController {

    @Resource
    private SysSettingService sysSettingService;

    @Resource
    private AdminConfig adminConfig;

    @RequestMapping("getSetting")
    public ResponseVO getSetting() {
        return getSuccessResponseVO(sysSettingService.refreshCache());
    }

    @RequestMapping("saveSetting")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO saveSetting(@VerifyParam SysSetting4AuditDto auditDto,
                                  @VerifyParam SysSetting4CommentDto commentDto,
                                  @VerifyParam SysSetting4PostDto postDto,
                                  @VerifyParam SysSetting4LikeDto likeDto,
                                  @VerifyParam SysSetting4RegisterDto registerDto,
                                  @VerifyParam SysSetting4EmailDto emailDto) {
        SysSettingDto sysSettingDto = new SysSettingDto();
        sysSettingDto.setAuditStting(auditDto);
        sysSettingDto.setCommentSetting(commentDto);
        sysSettingDto.setPostSetting(postDto);
        sysSettingDto.setLikeSetting(likeDto);
        sysSettingDto.setEmailSetting(emailDto);
        sysSettingDto.setRegisterSetting(registerDto);
        sysSettingService.saveSetting(sysSettingDto);
        sendWebRequest();
        return getSuccessResponseVO(null);
    }

    /**
     * 刷新缓存
     */
    private void sendWebRequest() {
        String appKey = adminConfig.getInnerApiAppKey();
        String appSecret = adminConfig.getInnerApiAppSecret();
        Long timestamp = System.currentTimeMillis();
        String sign = StringTools.encodeByMD5(appKey + timestamp + appSecret);
        String url = adminConfig.getWebApiUrl() + "?appKey=" + appKey + "&timestamp=" + timestamp + "&sign=" + sign;
        String responseJson = OKHttpUtils.getRequest(url);
        ResponseVO responseVO = JsonUtils.convertJson2Obj(responseJson, ResponseVO.class);
        if (!STATUC_SUCCESS.equals(responseVO.getStatus())) {
            throw new BusinessException("刷新访客端缓存失败");
        }
    }

}
