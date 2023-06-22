package com.GPbbs.aspect;

import com.GPbbs.annotation.GlobalInterceptor;
import com.GPbbs.annotation.VerifyParam;
import com.GPbbs.entity.config.WebConfig;
import com.GPbbs.entity.constants.Constants;
import com.GPbbs.entity.dto.SessionWebUserDto;
import com.GPbbs.entity.dto.SysSettingDto;
import com.GPbbs.entity.enums.DateTimePatternEnum;
import com.GPbbs.entity.enums.ResponseCodeEnum;
import com.GPbbs.entity.enums.UserOperFrequencyTypeEnum;
import com.GPbbs.entity.enums.UserStatusEnum;
import com.GPbbs.entity.po.UserInfo;
import com.GPbbs.entity.query.*;
import com.GPbbs.entity.vo.ResponseVO;
import com.GPbbs.exception.BusinessException;
import com.GPbbs.service.*;
import com.GPbbs.utils.DateUtil;
import com.GPbbs.utils.StringTools;
import com.GPbbs.utils.SysCacheUtils;
import com.GPbbs.utils.VerifyUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Date;
import java.util.List;

@Component
@Aspect
public class OperationAspect {

    private static Logger logger = LoggerFactory.getLogger(OperationAspect.class);



//    private static final String TYPE_STRING = "java.lang.String";

    private static final String[] TYPE_BASE = {"java.lang.String", "java.lang.Integer", "java.lang.Long"};

//    private static final String TYPE_INTEGER = "java.lang.Integer";
//
//    private static final String TYPE_LONG = "java.lang.Long";


    @Resource
    private WebConfig webConfig;

    @Resource
    private ForumArticleService forumArticleService;

    @Resource
    private ForumCommentService forumCommentService;

    @Resource
    private LikeRecordService likeRecordService;

    @Resource
    private UserInfoService userInfoService;

    @Pointcut("@annotation(com.GPbbs.annotation.GlobalInterceptor)")
    private void requestInterceptor() {
    }

    @Around("requestInterceptor()")
    public Object interceptorDo(ProceedingJoinPoint point) throws BusinessException {
        try {
            // 拿到具体目标
            Object target = point.getTarget();
            // 拿到参数
            Object[] arguments = point.getArgs();
            // 拿到方法名
            String methodName = point.getSignature().getName();
            // 拿到参数类型
            Class<?>[] parameterTypes = ((MethodSignature) point.getSignature()).getMethod().getParameterTypes();
            Method method = target.getClass().getMethod(methodName, parameterTypes);
            // 拿到全局拦截器
            GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);

            // 如果没有拦截器
            if (null == interceptor) {
                return null;
            }

            /**
             * 校验登录
             */
            if (interceptor.checkLogin()) {
                checkLogin();
            }
            /**
             * 校验参数
             */
            if (interceptor.checkParams()) {
                validateParams(method, arguments);
            }
            /**
             * 校验频次
             */
            this.checkFrequency(interceptor.frequencyType());
            /**
             * 执行操作
             */
            Object pointResult = point.proceed();

            /**
             * 增加频次限制
             */
            if (pointResult instanceof ResponseVO) {
                ResponseVO responseVO = (ResponseVO) pointResult;
                if (Constants.STATUC_SUCCESS.equals(responseVO.getStatus())) {
                    addOpCount(interceptor.frequencyType());
                }
            }
            return pointResult;
        } catch (BusinessException e) {
            logger.error("全局拦截器异常", e);
            throw e;
        } catch (Exception e) {
            logger.error("全局拦截器异常", e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        } catch (Throwable e) {
            logger.error("全局拦截器异常", e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }
    }

    private void checkFrequency(UserOperFrequencyTypeEnum typeEnum) {
        if (typeEnum == null || typeEnum == UserOperFrequencyTypeEnum.NO_CHECK) {
            // 如果不校验
            return;
        }
         // 拿到sessinon
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();

        SessionWebUserDto webUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);

        String curDate = DateUtil.format(new Date(), DateTimePatternEnum.YYYY_MM_DD.getPattern());
        String sessionKey = Constants.SESSION_KEY_FREQUENCY + curDate + typeEnum;
        // 拿到用户某一天 某一项操作数量
        Integer count = (Integer) session.getAttribute(sessionKey);
        SysSettingDto sysSettingDto = SysCacheUtils.getSysSetting();
        switch (typeEnum) {
            case POST_ARTICLE:
                if (count == null) {
                    // 如果该次数没有
                    // 查询今天次数
                    ForumArticleQuery forumArticleQuery = new ForumArticleQuery();
                    forumArticleQuery.setUserId(webUserDto.getUserId());
                    forumArticleQuery.setPostTimeStart(curDate);
                    forumArticleQuery.setPostTimeEnd(curDate);
                    count = forumArticleService.findCountByParam(forumArticleQuery);
                }
                if (count >= sysSettingDto.getPostSetting().getPostDayCountThreshold()) {
                    throw new BusinessException(ResponseCodeEnum.CODE_602);
                }
                break;
            case POST_COMMENT:
                if (count == null) {
                    ForumCommentQuery forumCommentQuery = new ForumCommentQuery();
                    forumCommentQuery.setUserId(webUserDto.getUserId());
                    forumCommentQuery.setPostTimeStart(curDate);
                    forumCommentQuery.setPostTimeEnd(curDate);
                    count = forumCommentService.findCountByParam(forumCommentQuery);
                }

                if (count >= sysSettingDto.getCommentSetting().getCommentDayCountThreshold()) {
                    throw new BusinessException(ResponseCodeEnum.CODE_602);
                }
                break;
            case DO_LIKE:
                if (count == null) {
                    LikeRecordQuery recordQuery = new LikeRecordQuery();
                    recordQuery.setUserId(webUserDto.getUserId());
                    recordQuery.setCreateTimeStart(curDate);
                    recordQuery.setCreateTimeEnd(curDate);
                    count = likeRecordService.findCountByParam(recordQuery);

                }
                if (count >= sysSettingDto.getLikeSetting().getLikeDayCountThreshold()) {
                    throw new BusinessException(ResponseCodeEnum.CODE_602);
                }
                break;
            case IMAGE_UPLAOD:
                if (count == null) {
                    count = 0;
                }
                if (count >= sysSettingDto.getPostSetting().getDayImageUploadCount()) {
                    throw new BusinessException(ResponseCodeEnum.CODE_602);
                }
                break;
        }
        session.setAttribute(sessionKey, count);
    }

    // 统计已经统计数据 如果操作成功，则频次加一
    private void addOpCount(UserOperFrequencyTypeEnum typeEnum) {
        if (typeEnum == null || typeEnum == UserOperFrequencyTypeEnum.NO_CHECK) {
            return;
        }
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();
        String curDate = DateUtil.format(new Date(), DateTimePatternEnum.YYYY_MM_DD.getPattern());
        String sessionKey = Constants.SESSION_KEY_FREQUENCY + curDate + typeEnum;
        Integer count = (Integer) session.getAttribute(sessionKey);
        session.setAttribute(sessionKey, count + 1);
    }

    // 校验登录
    private void checkLogin() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();
        SessionWebUserDto sessionUser = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        if (sessionUser == null && webConfig.getDev()) {
            List<UserInfo> userInfoList = userInfoService.findListByParam(new UserInfoQuery());
            if (!userInfoList.isEmpty()) {
                UserInfo userInfo = userInfoList.get(0);
                sessionUser = new SessionWebUserDto();
                sessionUser.setUserId(userInfo.getUserId());
                sessionUser.setNickName(userInfo.getNickName());
                sessionUser.setProvince("中国");
                sessionUser.setAdmin(true);
                session.setAttribute(Constants.SESSION_KEY, sessionUser);
            }

        }
        // 登录超时
        if (null == sessionUser) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
    }

    /**
     * @Description: 参数校验，后端参数校验 防小人不妨君子，前端已经做了参数校验，绕过前端，后端校验，一律返回 请求参数不正确
     * @param: [m, arguments]
     * @return: void
     */
    private void validateParams(Method m, Object[] arguments) throws BusinessException {
        Parameter[] parameters = m.getParameters();
        // 拿到参数进行for循环
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            // 拿到参数值
            Object value = arguments[i];
            // 拿到参数注解
            VerifyParam verifyParam = parameter.getAnnotation(VerifyParam.class);
            if (verifyParam == null) {
                continue;
            }
            // 基本数据类型
//            if (TYPE_STRING.equals(parameter.getParameterizedType().getTypeName()) || TYPE_LONG.equals(parameter.getParameterizedType().getTypeName()) || TYPE_INTEGER.equals(parameter.getParameterizedType().getTypeName())) {
            if(ArrayUtils.contains(TYPE_BASE, parameter.getParameterizedType().getTypeName())){
                checkValue(value, verifyParam);
                //如果传递的是对象
            } else {
                checkObjValue(parameter, value);
            }
        }
    }

    private void checkObjValue(Parameter parameter, Object value) {
        try {
            String typeName = parameter.getParameterizedType().getTypeName();
            Class classz = Class.forName(typeName);
            Field[] fields = classz.getDeclaredFields();
            for (Field field : fields) {
                VerifyParam fieldVerifyParam = field.getAnnotation(VerifyParam.class);
                if (fieldVerifyParam == null) {
                    continue;
                }
                field.setAccessible(true);
                Object resultValue = field.get(value);
                checkValue(resultValue, fieldVerifyParam);
            }
        } catch (BusinessException e) {
            logger.error("校验参数失败", e);
            throw e;
        } catch (Exception e) {
            logger.error("校验参数失败", e);
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }

    /**
     * 校验参数
     *
     * @param value
     * @param verifyParam
     * @throws BusinessException
     */
    private void checkValue(Object value, VerifyParam verifyParam) throws BusinessException {
        Boolean isEmpty = value == null || StringTools.isEmpty(value.toString());
        Integer length = value == null ? 0 : value.toString().length();

        /**
         * 校验空 如果必填，并且没有填参数
         */
        if (isEmpty && verifyParam.required()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        /**
         * 校验长度
         */
        if (!isEmpty && (verifyParam.max() != -1 && verifyParam.max() < length || verifyParam.min() != -1 && verifyParam.min() > length)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        /**
         * 校验正则 非空 && 正则非空 && 校验不过
         */
        if (!isEmpty && !StringTools.isEmpty(verifyParam.regex().getRegex()) && !VerifyUtils.verify(verifyParam.regex(), String.valueOf(value))) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }
}