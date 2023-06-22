package com.GPbbs.controller.api;

import com.GPbbs.annotation.GlobalInterceptor;
import com.GPbbs.annotation.VerifyParam;
import com.GPbbs.controller.base.ABaseController;
import com.GPbbs.entity.config.WebConfig;
import com.GPbbs.entity.enums.ResponseCodeEnum;
import com.GPbbs.entity.vo.ResponseVO;
import com.GPbbs.exception.BusinessException;
import com.GPbbs.service.SysSettingService;
import com.GPbbs.utils.StringTools;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/innerApi")
public class InnnerApiController extends ABaseController {

    @Resource
    private WebConfig webConfig;

    @Resource
    private SysSettingService sysSettingService;

    @RequestMapping("/refresSysSetting")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO refresSysSetting(@VerifyParam(required = true) String appKey,
                                       @VerifyParam(required = true) Long timestamp,
                                       @VerifyParam(required = true) String sign) {
        if (!webConfig.getInnerApiAppKey().equals(appKey)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        if (System.currentTimeMillis() - timestamp > 1000 * 10) {
            // 调用超时
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        String mySign = StringTools.encodeByMD5(appKey + timestamp + webConfig.getInnerApiAppSecret());
        if (!mySign.equals(sign)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        sysSettingService.refreshCache();
        return getSuccessResponseVO(null);
    }
}
