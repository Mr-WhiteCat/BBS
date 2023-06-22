package com.GPbbs.service;

import java.util.List;

import com.GPbbs.entity.dto.UserMessageCountDto;
import com.GPbbs.entity.query.UserMessageQuery;
import com.GPbbs.entity.po.UserMessage;
import com.GPbbs.entity.vo.PaginationResultVO;


/**
 * 
 * 用户消息 业务接口
 * 
 */
public interface UserMessageService {

	/**
	 * 根据条件查询列表
	 */
	List<UserMessage> findListByParam(UserMessageQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(UserMessageQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<UserMessage> findListByPage(UserMessageQuery param);

	/**
	 * 新增
	 */
	Integer add(UserMessage bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<UserMessage> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<UserMessage> listBean);

	/**
	 * 根据MessageId查询对象
	 */
	UserMessage getUserMessageByMessageId(Integer messageId);


	/**
	 * 根据MessageId修改
	 */
	Integer updateUserMessageByMessageId(UserMessage bean,Integer messageId);


	/**
	 * 根据MessageId删除
	 */
	Integer deleteUserMessageByMessageId(Integer messageId);


	/**
	 * 根据ArticleIdAndCommentIdAndSendUserIdAndMessageType查询对象
	 */
	UserMessage getUserMessageByArticleIdAndCommentIdAndSendUserIdAndMessageType(String articleId,Integer commentId,String sendUserId,Integer messageType);


	/**
	 * 根据ArticleIdAndCommentIdAndSendUserIdAndMessageType修改
	 */
	Integer updateUserMessageByArticleIdAndCommentIdAndSendUserIdAndMessageType(UserMessage bean,String articleId,Integer commentId,String sendUserId,Integer messageType);


	/**
	 * 根据ArticleIdAndCommentIdAndSendUserIdAndMessageType删除
	 */
	Integer deleteUserMessageByArticleIdAndCommentIdAndSendUserIdAndMessageType(String articleId,Integer commentId,String sendUserId,Integer messageType);

	/**
	 * 返回用户信息数
	 */
	UserMessageCountDto getUserMessageCount(String userId);

	/**
	 * 标记消息已读
	 */
	void readMessageByType(String recivedUserId, Integer messageType);



}