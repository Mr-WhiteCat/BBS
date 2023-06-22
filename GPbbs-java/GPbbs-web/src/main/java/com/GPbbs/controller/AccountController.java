package com.GPbbs.controller;


import com.GPbbs.annotation.GlobalInterceptor;
import com.GPbbs.annotation.VerifyParam;
import com.GPbbs.controller.base.ABaseController;
import com.GPbbs.entity.constants.Constants;
import com.GPbbs.entity.dto.CreateImageCode;
import com.GPbbs.entity.dto.SessionWebUserDto;
import com.GPbbs.entity.dto.SysSetting4CommentDto;
import com.GPbbs.entity.dto.SysSettingDto;
import com.GPbbs.entity.enums.ResponseCodeEnum;
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


    /**
     * 图片验证码
     */
    @RequestMapping(value = "/verificationCode")
    public void checkCode(HttpServletResponse response, HttpSession session, Integer type) throws IOException {

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
        // 登录注册验证码图片
        if (type == null || type == 0) {
            // 登录/注册图片验证码
            session.setAttribute(Constants.VERIFICATION_CODE_KEY, code);
        } else {
            // 获取图片验证码图片
            session.setAttribute(Constants.VERIFICATION_CODE_KEY_EMAIL, code);
        }
        vCode.write(response.getOutputStream());
    }

    /**
     * 发送邮箱验证码
     */
    @RequestMapping("/sendEmailCode")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO sendEmailCode(HttpSession session,
                                    @VerifyParam(required = true) String email,
                                    @VerifyParam(required = true) String verificationCode,
                                    @VerifyParam(required = true) Integer type) {



       try {
           /*if(StringTools.isEmpty(email)||StringTools.isEmpty(verificationCode)|| type == null ){
               throw new BusinessException(ResponseCodeEnum.CODE_600);
           }*/
           if (!verificationCode.equalsIgnoreCase((String) session.getAttribute(Constants.VERIFICATION_CODE_KEY_EMAIL))) {
                throw new BusinessException("图片验证码不正确");
           }
           emailCodeService.sendEmailCode(email, type);
           return getSuccessResponseVO(null);
        } finally {
           // 清除已经使用过的验证码
           session.removeAttribute(Constants.VERIFICATION_CODE_KEY_EMAIL);
        }
    }

    /**
     * 注册
     * @param session
     * @param email
     * @param emailCode
     * @param nickName
     * @param password
     * @param verificationCode
     * @return
     */
    @RequestMapping("/register")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO register(HttpSession session,
                               @VerifyParam(required = true, regex = VerifyRegexEnum.EMAIL, max = 150) String email,
                               @VerifyParam(required = true) String emailCode,
                               @VerifyParam(required = true, max = 20) String nickName,
                               @VerifyParam(required = true, regex = VerifyRegexEnum.PASSWORD, min = 8, max = 18) String password,
                               @VerifyParam(required = true) String verificationCode){

        /*if (verificationCode == null || "".equalsIgnoreCase(verificationCode)){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        session.getAttribute(Constants.VERIFICATION_CODE_KEY).equals(verificationCode);

        // 忽略大小写
        String sessionCode = (String)session.getAttribute(Constants.VERIFICATION_CODE_KEY);

        if(sessionCode.equalsIgnoreCase(verificationCode)){
            return getSuccessResponseVO("验证成功");
        } else {
            throw new BusinessException("验证失败");

        }*/

        try {

            /*if(StringTools.isEmpty(email)||StringTools.isEmpty(emailCode)||StringTools.isEmpty(nickName)||StringTools.isEmpty(password)||StringTools.isEmpty(verificationCode)){
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }*/
            if(!verificationCode.equalsIgnoreCase((String)session.getAttribute(Constants.VERIFICATION_CODE_KEY))){
                throw new BusinessException("图片验证码不正确");
            }

            // 注册用户
            userInfoService.register(email, emailCode, nickName, password);
            return getSuccessResponseVO(null);

        } finally {
            // 移除图片验证码
            session.removeAttribute(Constants.VERIFICATION_CODE_KEY);
        }
    }


    /**
     * 登录
     *
     */
    @RequestMapping("/login")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO login(HttpSession session,
                            HttpServletRequest request,
                            @VerifyParam(required = true) String email,
                            @VerifyParam(required = true) String password,
                            @VerifyParam(required = true) String verificationCode) {
        try {
            if (!verificationCode.equalsIgnoreCase((String) session.getAttribute(Constants.VERIFICATION_CODE_KEY))) {
                throw new BusinessException("图片验证码不正确");
            }
            SessionWebUserDto sessionWebUserDto = userInfoService.login(email, password, getIpAddr(request));
            session.setAttribute(Constants.SESSION_KEY, sessionWebUserDto);
            return getSuccessResponseVO(sessionWebUserDto);
        } finally {
            session.removeAttribute(Constants.VERIFICATION_CODE_KEY);
        }
    }

    /**
     * 获取用户信息 未登录返回null
     * @param session
     * @return
     */
    @RequestMapping("/getUserInfo")
    public ResponseVO login(HttpSession session) {
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        return getSuccessResponseVO(sessionWebUserDto);
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
    /**
     * 获取系统设置
     * @return
     */
    @RequestMapping("/getSysSetting")
    public ResponseVO getSysSetting() {

        // 获取评论是否打开
        SysSetting4CommentDto commentDto = SysCacheUtils.getSysSetting().getCommentSetting();
        Map<String, Object> result = new HashMap<>();
        result.put("commentOpen",commentDto.getCommentOpen());

        return getSuccessResponseVO(result);
    }
    /**
     * 重置密码
     * @param session
     * @return
     */
    @RequestMapping("/resetPwd")
    public ResponseVO resetPwd(HttpSession session,
                               @VerifyParam(required = true) String email,
                               @VerifyParam(required = true, regex = VerifyRegexEnum.PASSWORD, min = 8, max = 18) String password,
                               @VerifyParam(required = true) String verificationCode,
                               @VerifyParam(required = true) String emailCode) {
        try {
            if (!verificationCode.equalsIgnoreCase((String) session.getAttribute(Constants.VERIFICATION_CODE_KEY))) {
                throw new BusinessException("图片验证码不正确");
            }
            userInfoService.resetPwd(email, password, emailCode);
            return getSuccessResponseVO(null);
        } finally {
            session.removeAttribute(Constants.VERIFICATION_CODE_KEY);
        }
    }

    /**
     *
     */



}
