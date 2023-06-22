package com.GPbbs.service;

import java.util.List;

import com.GPbbs.entity.query.ForumCommentQuery;
import com.GPbbs.entity.po.ForumComment;
import com.GPbbs.entity.vo.PaginationResultVO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


/**
 * 
 * 评论 业务接口
 * 
 */
public interface ForumCommentService {

	/**
	 * 根据条件查询列表
	 */
	List<ForumComment> findListByParam(ForumCommentQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(ForumCommentQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<ForumComment> findListByPage(ForumCommentQuery param);

	/**
	 * 新增
	 */
	Integer add(ForumComment bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<ForumComment> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<ForumComment> listBean);

	/**
	 * 根据CommentId查询对象
	 */
	ForumComment getForumCommentByCommentId(Integer commentId);


	/**
	 * 根据CommentId修改
	 */
	Integer updateForumCommentByCommentId(ForumComment bean,Integer commentId);


	/**
	 * 根据CommentId删除
	 */
	Integer deleteForumCommentByCommentId(Integer commentId);

	/**
	 * 评论置顶与取消
	 */
	void changeTopType(String userId, Integer commentId, Integer topType);

	/**
	 * 发布评论
	 */
	void postComment(ForumComment comment, MultipartFile image);

	/**
	 * 删除评论
	 */
	void delComment(String commentIds);

	/**
	 * 删除单个评论
	 */
	void delCommentSingle(Integer commentId);


	/**
	 * 审核评论
	 */
	void auditComment(String commentIds);

	/**
	 * 审核单个评论
	 */
	void auditCommentSingle(Integer commentId);



}