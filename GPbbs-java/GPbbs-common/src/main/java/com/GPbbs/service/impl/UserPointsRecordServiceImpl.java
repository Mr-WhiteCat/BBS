package com.GPbbs.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.GPbbs.entity.enums.PageSize;
import com.GPbbs.entity.query.UserPointsRecordQuery;
import com.GPbbs.entity.po.UserPointsRecord;
import com.GPbbs.entity.vo.PaginationResultVO;
import com.GPbbs.entity.query.SimplePage;
import com.GPbbs.mappers.UserPointsRecordMapper;
import com.GPbbs.service.UserPointsRecordService;


/**
 * 
 * 用户积分记录表 业务接口实现
 * 
 */
@Service("userPointsRecordService")
public class UserPointsRecordServiceImpl implements UserPointsRecordService {

	@Resource
	private UserPointsRecordMapper<UserPointsRecord,UserPointsRecordQuery> userPointsRecordMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<UserPointsRecord> findListByParam(UserPointsRecordQuery param) {
		return this.userPointsRecordMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(UserPointsRecordQuery param) {
		return this.userPointsRecordMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<UserPointsRecord> findListByPage(UserPointsRecordQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserPointsRecord> list = this.findListByParam(param);
		PaginationResultVO<UserPointsRecord> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(),page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(UserPointsRecord bean){
		return this.userPointsRecordMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<UserPointsRecord> listBean){
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userPointsRecordMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserPointsRecord> listBean){
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userPointsRecordMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 根据RecordId获取对象
	 */
	@Override
	public UserPointsRecord getUserPointsRecordByRecordId(Integer recordId){
		return this.userPointsRecordMapper.selectByRecordId(recordId);
	}

	/**
	 * 根据RecordId修改
	 */
	@Override
	public Integer updateUserPointsRecordByRecordId(UserPointsRecord bean,Integer recordId){
		return this.userPointsRecordMapper.updateByRecordId(bean,recordId);
	}

	/**
	 * 根据RecordId删除
	 */
	@Override
	public Integer deleteUserPointsRecordByRecordId(Integer recordId){
		return this.userPointsRecordMapper.deleteByRecordId(recordId);
	}
}