package com.GPbbs.service;

import java.util.List;

import com.GPbbs.entity.query.ForumBoardQuery;
import com.GPbbs.entity.po.ForumBoard;
import com.GPbbs.entity.vo.PaginationResultVO;


/**
 * 
 * 文章板块信息 业务接口
 * 
 */
public interface ForumBoardService {

	/**
	 * 根据条件查询列表
	 */
	List<ForumBoard> findListByParam(ForumBoardQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(ForumBoardQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<ForumBoard> findListByPage(ForumBoardQuery param);

	/**
	 * 新增
	 */
	Integer add(ForumBoard bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<ForumBoard> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<ForumBoard> listBean);

	/**
	 * 根据BoardId查询对象
	 */
	ForumBoard getForumBoardByBoardId(Integer boardId);


	/**
	 * 根据BoardId修改
	 */
	Integer updateForumBoardByBoardId(ForumBoard bean,Integer boardId);


	/**
	 * 根据BoardId删除
	 */
	Integer deleteForumBoardByBoardId(Integer boardId);

	/**
	 * 获取板块信息
	 */
	List<ForumBoard> getBoardTree(Integer postType);

	/**
	 * 保存板块
	 * @param forumBoard
	 */
	void saveForumBoard(ForumBoard forumBoard);

	/**
	 * 板块上下移动
	 */
	void  changeSort(String boardIds);

}