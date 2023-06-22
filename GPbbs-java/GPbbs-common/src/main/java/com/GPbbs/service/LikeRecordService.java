package com.GPbbs.service;

import java.util.List;

import com.GPbbs.entity.enums.OperRecordOpTypeEnum;
import com.GPbbs.entity.query.LikeRecordQuery;
import com.GPbbs.entity.po.LikeRecord;
import com.GPbbs.entity.vo.PaginationResultVO;


/**
 * 
 * 点赞记录 业务接口
 * 
 */
public interface LikeRecordService {

	/**
	 * 根据条件查询列表
	 */
	List<LikeRecord> findListByParam(LikeRecordQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(LikeRecordQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<LikeRecord> findListByPage(LikeRecordQuery param);

	/**
	 * 新增
	 */
	Integer add(LikeRecord bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<LikeRecord> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<LikeRecord> listBean);

	/**
	 * 根据OpId查询对象
	 */
	LikeRecord getLikeRecordByOpId(Integer opId);


	/**
	 * 根据OpId修改
	 */
	Integer updateLikeRecordByOpId(LikeRecord bean,Integer opId);


	/**
	 * 根据OpId删除
	 */
	Integer deleteLikeRecordByOpId(Integer opId);


	/**
	 * 根据ObjectIdAndUserIdAndOpType查询对象
	 */
	LikeRecord getLikeRecordByObjectIdAndUserIdAndOpType(String objectId,String userId,Integer opType);


	/**
	 * 根据ObjectIdAndUserIdAndOpType修改
	 */
	Integer updateLikeRecordByObjectIdAndUserIdAndOpType(LikeRecord bean,String objectId,String userId,Integer opType);


	/**
	 * 根据ObjectIdAndUserIdAndOpType删除
	 */
	Integer deleteLikeRecordByObjectIdAndUserIdAndOpType(String objectId,String userId,Integer opType);

	/**
	 *添加点赞记录
	 */
	void doLike(String objectId, String userId, String nickName, OperRecordOpTypeEnum opTypeEnum);

}