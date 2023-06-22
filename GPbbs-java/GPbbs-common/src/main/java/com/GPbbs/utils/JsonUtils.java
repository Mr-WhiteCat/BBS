package com.GPbbs.utils;

import com.GPbbs.exception.BusinessException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.GPbbs.entity.enums.ResponseCodeEnum;
import com.GPbbs.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class JsonUtils {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    /**
     * 数组 集合 bean对象
     * 对象转json
     * @param obj
     * @return
     */
    public static String convertObj2Json(Object obj) {
        return JSON.toJSONString(obj);
    }

    /**
     * json转字符串
     * @param json
     * @param classz
     * @return
     * @param <T>
     * @throws BusinessException
     */
    public static <T> T convertJson2Obj(String json, Class<T> classz) throws BusinessException {
        try {
            return JSONObject.parseObject(json, classz);
        } catch (Exception e) {
            logger.error("convertJson2Obj异常，json:{}", json);
            return null;
//            throw new BusinessException(ResponseCodeEnum.CODE_601);
        }
    }

    /**
     * 字符串数组转集合对象
     * @param json
     * @param classz
     * @return
     * @param <T>
     * @throws BusinessException
     */
    public static <T> List<T> convertJsonArray2List(String json, Class<T> classz) throws BusinessException {
        try {
            return JSONArray.parseArray(json, classz);
        } catch (Exception e) {
            logger.error("convertJsonArray2List,json:{}", json, e);
//            throw new BusinessException(ResponseCodeEnum.CODE_601);
            return null;
        }
    }

    public static void main(String[] args) {
    }
}
