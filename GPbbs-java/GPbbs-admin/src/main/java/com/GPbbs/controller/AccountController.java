package com.GPbbs.controller;


import com.GPbbs.annotation.GlobalInterceptor;
import com.GPbbs.annotation.VerifyParam;
import com.GPbbs.controller.base.ABaseController;
import com.GPbbs.entity.config.AdminConfig;
import com.GPbbs.entity.constants.Constants;
import com.GPbbs.entity.dto.CreateImageCode;
import com.GPbbs.entity.dto.SessionAdminUserDto;
import com.GPbbs.entity.dto.SessionWebUserDto;
import com.GPbbs.entity.dto.SysSetting4CommentDto;
import com.GPbbs.entity.enums.VerifyRegexEnum;
import com.GPbbs.entity.vo.ResponseVO;
import com.GPbbs.exception.BusinessException;
import com.GPbbs.service.EmailCodeService;
import com.GPbbs.service.UserInfoService;
import com.GPbbs.utils.StringTools;
import com.GPbbs.utils.SysCacheUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@RestController
public class AccountController extends ABaseController {

    @Resource
    private EmailCodeService emailCodeService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private AdminConfig adminConfig;


    /**
     * 图片验证码
     */
    @RequestMapping(value = "/verificationCode")
    public void checkCode(HttpServletResponse response, HttpSession session) throws IOException {

        // 设置图片高度、宽度、验证码字母个数、以及干扰线条数
        CreateImageCode vCode = new CreateImageCode(130, 38, 5, 10);
        // 返回流的设置
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        // 失效时间 0
        response.setDateHeader("Expires", 0);
        // 设置为图片格式---识别为图片
        response.setContentType("image/jpeg");
        // 拿到验证字母
        String code = vCode.getCode();
        // 登录图片验证码
        session.setAttribute(Constants.VERIFICATION_CODE_KEY, code);
        vCode.write(response.getOutputStream());
    }

    /**
     * 登录
     *
     */
    @RequestMapping("/login")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO login(HttpSession session,
                            HttpServletRequest request,
                            @VerifyParam(required = true) String account,
                            @VerifyParam(required = true) String password,
                            @VerifyParam(required = true) String verificationCode) {
        try {
            if (!verificationCode.equalsIgnoreCase((String) session.getAttribute(Constants.VERIFICATION_CODE_KEY))) {
                throw new BusinessException("图片验证码不正确");
            }

            if (!adminConfig.getAdminAccount().equals(account) || !StringTools.encodeByMD5(adminConfig.getAdminPassword()).equals(password)) {
                throw new BusinessException("账号或密码错误");
            }
            SessionAdminUserDto sessionAdminUserDto = new SessionAdminUserDto();
            sessionAdminUserDto.setAccount(account);
            session.setAttribute(Constants.SESSION_KEY, sessionAdminUserDto);
            return getSuccessResponseVO(sessionAdminUserDto);
        } finally {
            session.removeAttribute(Constants.VERIFICATION_CODE_KEY);
        }
    }

    /**
     * 退出登录
     * @param session
     * @return
     */
    @RequestMapping("/logout")
    public ResponseVO logout(HttpSession session) {
        session.invalidate();
        return getSuccessResponseVO(null);
    }



}
