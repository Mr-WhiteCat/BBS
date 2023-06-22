package com.GPbbs.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.GPbbs.entity.enums.*;
import com.GPbbs.entity.po.ForumArticle;
import com.GPbbs.entity.po.ForumComment;
import com.GPbbs.entity.po.UserMessage;
import com.GPbbs.entity.query.*;
import com.GPbbs.exception.BusinessException;
import com.GPbbs.mappers.ForumArticleMapper;
import com.GPbbs.mappers.ForumCommentMapper;
import com.GPbbs.mappers.UserMessageMapper;
import org.springframework.stereotype.Service;

import com.GPbbs.entity.po.LikeRecord;
import com.GPbbs.entity.vo.PaginationResultVO;
import com.GPbbs.mappers.LikeRecordMapper;
import com.GPbbs.service.LikeRecordService;
import org.springframework.transaction.annotation.Transactional;


/**
 * 
 * 点赞记录 业务接口实现
 * 
 */
@Service("likeRecordService")
public class LikeRecordServiceImpl implements LikeRecordService {

	@Resource
	private LikeRecordMapper<LikeRecord,LikeRecordQuery> likeRecordMapper;

	@Resource
	private UserMessageMapper<UserMessage, UserMessageQuery> userMessageMapper;

	@Resource
	private ForumArticleMapper<ForumArticle, ForumArticleQuery> forumArticleMapper;

	@Resource
	private ForumCommentMapper<ForumComment, ForumCommentQuery> forumCommentMapper;


	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<LikeRecord> findListByParam(LikeRecordQuery param) {
		return this.likeRecordMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(LikeRecordQuery param) {
		return this.likeRecordMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<LikeRecord> findListByPage(LikeRecordQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<LikeRecord> list = this.findListByParam(param);
		PaginationResultVO<LikeRecord> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(),page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(LikeRecord bean){
		return this.likeRecordMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<LikeRecord> listBean){
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.likeRecordMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<LikeRecord> listBean){
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.likeRecordMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 根据OpId获取对象
	 */
	@Override
	public LikeRecord getLikeRecordByOpId(Integer opId){
		return this.likeRecordMapper.selectByOpId(opId);
	}

	/**
	 * 根据OpId修改
	 */
	@Override
	public Integer updateLikeRecordByOpId(LikeRecord bean,Integer opId){
		return this.likeRecordMapper.updateByOpId(bean,opId);
	}

	/**
	 * 根据OpId删除
	 */
	@Override
	public Integer deleteLikeRecordByOpId(Integer opId){
		return this.likeRecordMapper.deleteByOpId(opId);
	}

	/**
	 * 根据ObjectIdAndUserIdAndOpType获取对象
	 */
	@Override
	public LikeRecord getLikeRecordByObjectIdAndUserIdAndOpType(String objectId,String userId,Integer opType){
		return this.likeRecordMapper.selectByObjectIdAndUserIdAndOpType(objectId,userId,opType);
	}

	/**
	 * 根据ObjectIdAndUserIdAndOpType修改
	 */
	@Override
	public Integer updateLikeRecordByObjectIdAndUserIdAndOpType(LikeRecord bean,String objectId,String userId,Integer opType){
		return this.likeRecordMapper.updateByObjectIdAndUserIdAndOpType(bean,objectId,userId,opType);
	}

	/**
	 * 根据ObjectIdAndUserIdAndOpType删除
	 */
	@Override
	public Integer deleteLikeRecordByObjectIdAndUserIdAndOpType(String objectId,String userId,Integer opType){
		return this.likeRecordMapper.deleteByObjectIdAndUserIdAndOpType(objectId,userId,opType);
	}

	/**
	 *
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void doLike(String objectId, String userId, String nickName, OperRecordOpTypeEnum opTypeEnum) {
		UserMessage userMessage = new UserMessage();
		userMessage.setCreateTime(new Date());
		LikeRecord likeRecord = null;

		switch (opTypeEnum) {
			case ARTICLE_LIKE:

				ForumArticle forumArticle = forumArticleMapper.selectByArticleId(objectId);
				if (null == forumArticle) {
					throw new BusinessException("您所找的内容不存在");
				}

				articleLike(objectId, forumArticle, userId, opTypeEnum);

				userMessage.setArticleId(objectId);
				userMessage.setArticleTitle(forumArticle.getTitle());
				userMessage.setMessageType(MessageTypeEnum.ARTICLE_LIKE.getType());
				userMessage.setCommentId(0);
				userMessage.setReceivedUserId(forumArticle.getUserId());
				break;
			case COMMENT_LIKE:
				ForumComment forumComment = forumCommentMapper.selectByCommentId(Integer.parseInt(objectId));
				if (null == forumComment) {
					throw new BusinessException("评论不存在");
				}
				commentLike(objectId, forumComment, userId, opTypeEnum);

				ForumArticle commentArticle = forumArticleMapper.selectByArticleId(forumComment.getArticleId());
				userMessage.setArticleId(commentArticle.getArticleId());
				userMessage.setArticleTitle(commentArticle.getTitle());
				userMessage.setMessageType(MessageTypeEnum.COMMENT_LIKE.getType());
				userMessage.setCommentId(Integer.parseInt(objectId));
				userMessage.setReceivedUserId(forumComment.getUserId());
				userMessage.setMessageContent(forumComment.getContent());
				break;
		}

		// 插入数据库
		userMessage.setSendUserId(userId);
		userMessage.setSendNickName(nickName);
		userMessage.setStatus(MessageStatusEnum.NO_READ.getStatus());

		if (!userId.equals(userMessage.getReceivedUserId())) {
			// 如果没点赞 才插入数据库 并且点赞不是自己 发送消息
			UserMessage dbInfo =userMessageMapper.selectByArticleIdAndCommentIdAndSendUserIdAndMessageType(userMessage.getArticleId(),userMessage.getCommentId(),userMessage.getSendUserId(),userMessage.getMessageType());
			if(dbInfo == null) {
				// 如果message表中没有对应记录，这插入数据
				userMessageMapper.insert(userMessage);
			}
		}
	}

	/**
	 * 文章点赞，取消点赞
	 *
	 * @param objectId
	 * @param userId
	 * @param opTypeEnum
	 */
	public void articleLike(String objectId, ForumArticle forumArticle, String userId, OperRecordOpTypeEnum opTypeEnum) {
		LikeRecord record = this.likeRecordMapper.selectByObjectIdAndUserIdAndOpType(objectId, userId, opTypeEnum.getType());
		Integer changeCount = 0;
		if (record != null) {
			// 点过赞 取消点赞
			this.likeRecordMapper.deleteByObjectIdAndUserIdAndOpType(objectId, userId, opTypeEnum.getType());
			changeCount = -1;
		} else {
			// 没有点赞
			LikeRecord likeRecord = new LikeRecord();
			likeRecord.setObjectId(objectId);
			likeRecord.setUserId(userId);
			likeRecord.setOpType(opTypeEnum.getType());
			likeRecord.setCreateTime(new Date());
			likeRecord.setAuthorUserId(forumArticle.getUserId());
			// 插入数据库（点赞）
//			System.out.printf("99999999999999999999"+opTypeEnum+"99999999999999999999"+likeRecord+"999999999999999999999999999999999999");
			this.likeRecordMapper.insert(likeRecord);
			// 更新文章内容表中的点赞数
			changeCount = 1;

		}
		forumArticleMapper.updateArticleCount(UpdateArticleCountTypeEnum.GOOD_COUNT.getType(), changeCount, objectId);

	}

	/**
	 * 评论 点赞 踩
	 *
	 * @param objectId
	 * @param userId
	 * @param opTypeEnum
	 */
	public void commentLike(String objectId, ForumComment forumComment, String userId, OperRecordOpTypeEnum opTypeEnum) {

		LikeRecord record = this.likeRecordMapper.selectByObjectIdAndUserIdAndOpType(objectId, userId, opTypeEnum.getType());
		Integer changeCount = 0;
		if (record != null) {
			// 如果没已经点赞
			this.likeRecordMapper.deleteByObjectIdAndUserIdAndOpType(objectId, userId, opTypeEnum.getType());
			changeCount = -1;
		} else {

			LikeRecord likeRecord = new LikeRecord();
			likeRecord.setObjectId(objectId);
			likeRecord.setUserId(userId);
			likeRecord.setOpType(opTypeEnum.getType());
			likeRecord.setCreateTime(new Date());
			likeRecord.setAuthorUserId(forumComment.getUserId());
//			System.out.printf("222222222222222"+opTypeEnum+"22222222222222222222222222"+likeRecord+"222222222222222222222222222222222222222222222222");
			this.likeRecordMapper.insert(likeRecord);
			changeCount = 1;
		}
		forumCommentMapper.updateCommentGoodCount(changeCount, Integer.parseInt(objectId));






	}


}