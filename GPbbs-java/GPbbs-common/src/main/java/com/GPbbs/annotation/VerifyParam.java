package com.GPbbs.annotation;


import com.GPbbs.entity.enums.VerifyRegexEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD})
public @interface VerifyParam {
    boolean required() default false;

    /**
     * 最大长度
     *
     * @return
     */
    int max() default -1;

    /**
     * 最小长度
     *
     * @return
     */
    int min() default -1;

    /**
     * 校验正则
     *
     * @return
     */
    VerifyRegexEnum regex() default VerifyRegexEnum.NO;
}
