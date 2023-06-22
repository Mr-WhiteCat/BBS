package com.GPbbs.utils;

import com.GPbbs.entity.dto.SysSettingDto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SysCacheUtils {

    private static final String KEY_SYS = "sys_setting";

    // 全局对象
    private static final Map<String, SysSettingDto> CACHE_DATA = new ConcurrentHashMap<>();

    public static SysSettingDto getSysSetting() {
        return CACHE_DATA.get(KEY_SYS);
    }

    public static void refresh(SysSettingDto dto) {
        CACHE_DATA.put(KEY_SYS, dto);
    }
}
