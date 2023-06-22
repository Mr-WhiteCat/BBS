package com.GPbbs.service;

import java.util.List;

import com.GPbbs.entity.enums.UserPointsOperTypeEnum;
import com.GPbbs.entity.query.UserPointsRecordQuery;
import com.GPbbs.entity.po.UserPointsRecord;
import com.GPbbs.entity.vo.PaginationResultVO;


/**
 * 
 * 用户积分记录表 业务接口
 * 
 */
public interface UserPointsRecordService {

	/**
	 * 根据条件查询列表
	 */
	List<UserPointsRecord> findListByParam(UserPointsRecordQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(UserPointsRecordQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<UserPointsRecord> findListByPage(UserPointsRecordQuery param);

	/**
	 * 新增
	 */
	Integer add(UserPointsRecord bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<UserPointsRecord> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<UserPointsRecord> listBean);

	/**
	 * 根据RecordId查询对象
	 */
	UserPointsRecord getUserPointsRecordByRecordId(Integer recordId);


	/**
	 * 根据RecordId修改
	 */
	Integer updateUserPointsRecordByRecordId(UserPointsRecord bean,Integer recordId);


	/**
	 * 根据RecordId删除
	 */
	Integer deleteUserPointsRecordByRecordId(Integer recordId);



}