package com.GPbbs.service.impl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.List;

import javax.annotation.Resource;

import com.GPbbs.entity.dto.SysSetting4AuditDto;
import com.GPbbs.entity.dto.SysSettingDto;
import com.GPbbs.entity.enums.SysSettingCodeEnum;
import com.GPbbs.exception.BusinessException;
import com.GPbbs.utils.JsonUtils;
import com.GPbbs.utils.StringTools;
import com.GPbbs.utils.SysCacheUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.GPbbs.entity.enums.PageSize;
import com.GPbbs.entity.query.SysSettingQuery;
import com.GPbbs.entity.po.SysSetting;
import com.GPbbs.entity.vo.PaginationResultVO;
import com.GPbbs.entity.query.SimplePage;
import com.GPbbs.mappers.SysSettingMapper;
import com.GPbbs.service.SysSettingService;
import org.springframework.transaction.annotation.Transactional;


/**
 * 
 * 系统设置信息 业务接口实现
 * 
 */
@Service("sysSettingService")
public class SysSettingServiceImpl implements SysSettingService {

	private static final Logger logger = LoggerFactory.getLogger(SysSettingServiceImpl.class);


	@Resource
	private SysSettingMapper<SysSetting,SysSettingQuery> sysSettingMapper;



	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<SysSetting> findListByParam(SysSettingQuery param) {
		return this.sysSettingMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(SysSettingQuery param) {
		return this.sysSettingMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<SysSetting> findListByPage(SysSettingQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<SysSetting> list = this.findListByParam(param);
		PaginationResultVO<SysSetting> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(),page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(SysSetting bean){
		return this.sysSettingMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<SysSetting> listBean){
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.sysSettingMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<SysSetting> listBean){
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.sysSettingMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 根据Code获取对象
	 */
	@Override
	public SysSetting getSysSettingByCode(String code){
		return this.sysSettingMapper.selectByCode(code);
	}

	/**
	 * 根据Code修改
	 */
	@Override
	public Integer updateSysSettingByCode(SysSetting bean,String code){
		return this.sysSettingMapper.updateByCode(bean,code);
	}

	/**
	 * 根据Code删除
	 */
	@Override
	public Integer deleteSysSettingByCode(String code){
		return this.sysSettingMapper.deleteByCode(code);
	}

	// 刷新缓存
	@Override
	public SysSettingDto refreshCache() {

//		System.out.printf("XXXX");
		try {
			SysSettingDto sysSettingDto = new SysSettingDto();
			List<SysSetting> list = this.sysSettingMapper.selectList(new SysSettingQuery());
			Class classz = SysSettingDto.class;
			// 将list转换为sysSettingDto对象
			for (SysSetting sysSetting : list) {

				// 利用封装阿里fastjson
				String jsonContent = sysSetting.getJsonContent();
				if (StringTools.isEmpty(jsonContent)) {
					continue;
				}
				String code = sysSetting.getCode();
				// 拿到枚举
				SysSettingCodeEnum codeEnum = SysSettingCodeEnum.getByCode(code);
				// 反射
				PropertyDescriptor pd = new PropertyDescriptor(codeEnum.getPropName(), classz);
				Method method = pd.getWriteMethod();
				// 通过枚举实例化对象
				Class subClassZ = Class.forName(codeEnum.getClassZ());
				method.invoke(sysSettingDto, JsonUtils.convertJson2Obj(jsonContent, subClassZ));
			}
			SysCacheUtils.refresh(sysSettingDto);
			return sysSettingDto;
		} catch (Exception e) {
			logger.error("刷新缓存失败", e);
			throw new BusinessException("刷新缓存失败");
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveSetting(SysSettingDto sysSettingDto) {
		try {

			// 反射
			Class classz = SysSettingDto.class;
			for (SysSettingCodeEnum codeEnum : SysSettingCodeEnum.values()) {
				PropertyDescriptor pd = new PropertyDescriptor(codeEnum.getPropName(), classz);
				Method method = pd.getReadMethod();
				Object obj = method.invoke(sysSettingDto);
				SysSetting setting = new SysSetting();
				setting.setCode(codeEnum.getCode());
				setting.setJsonContent(JsonUtils.convertObj2Json(obj));
				this.sysSettingMapper.insertOrUpdate(setting);
			}
		} catch (Exception e) {
			logger.error("保存设置失败", e);
			throw new BusinessException("保存设置失败");
		}
	}
}