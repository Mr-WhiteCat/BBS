package com.GPbbs.service;

import java.util.List;

import com.GPbbs.entity.dto.SysSettingDto;
import com.GPbbs.entity.query.SysSettingQuery;
import com.GPbbs.entity.po.SysSetting;
import com.GPbbs.entity.vo.PaginationResultVO;


/**
 * 
 * 系统设置信息 业务接口
 * 
 */
public interface SysSettingService {

	/**
	 * 根据条件查询列表
	 */
	List<SysSetting> findListByParam(SysSettingQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(SysSettingQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<SysSetting> findListByPage(SysSettingQuery param);

	/**
	 * 新增
	 */
	Integer add(SysSetting bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<SysSetting> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<SysSetting> listBean);

	/**
	 * 根据Code查询对象
	 */
	SysSetting getSysSettingByCode(String code);


	/**
	 * 根据Code修改
	 */
	Integer updateSysSettingByCode(SysSetting bean,String code);


	/**
	 * 根据Code删除
	 */
	Integer deleteSysSettingByCode(String code);

	/**
	 * 刷新缓存
	 */
	SysSettingDto refreshCache();

	/**
	 * 保存设置配置
	 */
	void saveSetting(SysSettingDto sysSettingDto);

}