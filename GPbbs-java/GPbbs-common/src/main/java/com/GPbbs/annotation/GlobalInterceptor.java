package com.GPbbs.annotation;

//import com.GPbbs.entity.enums.UserOperFrequencyTypeEnum;
import com.GPbbs.entity.enums.UserOperFrequencyTypeEnum;
import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Mapping
@Inherited
public @interface GlobalInterceptor {
    /**
     * 是否校验登录
     */
    boolean checkLogin() default false;

    /**
     * 校验参数
     *
     * @return
     */
    boolean checkParams() default false;

    /**
     * 校验频次
     */
    UserOperFrequencyTypeEnum frequencyType() default UserOperFrequencyTypeEnum.NO_CHECK;
}
