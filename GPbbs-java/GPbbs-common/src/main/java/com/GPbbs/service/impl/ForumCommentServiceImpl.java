package com.GPbbs.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.GPbbs.entity.constants.Constants;
import com.GPbbs.entity.dto.FileUploadDto;
import com.GPbbs.entity.enums.*;
import com.GPbbs.entity.po.ForumArticle;
import com.GPbbs.entity.po.UserInfo;
import com.GPbbs.entity.po.UserMessage;
import com.GPbbs.exception.BusinessException;
import com.GPbbs.mappers.ForumArticleMapper;
import com.GPbbs.service.UserInfoService;
import com.GPbbs.service.UserMessageService;
import com.GPbbs.utils.FileUtils;
import com.GPbbs.utils.StringTools;
import com.GPbbs.utils.SysCacheUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.GPbbs.entity.query.ForumCommentQuery;
import com.GPbbs.entity.po.ForumComment;
import com.GPbbs.entity.vo.PaginationResultVO;
import com.GPbbs.entity.query.SimplePage;
import com.GPbbs.mappers.ForumCommentMapper;
import com.GPbbs.service.ForumCommentService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


/**
 * 
 * 评论 业务接口实现
 * 
 */
@Service("forumCommentService")
public class ForumCommentServiceImpl implements ForumCommentService {

	@Resource
	private ForumCommentMapper<ForumComment,ForumCommentQuery> forumCommentMapper;

	@Resource
	private ForumArticleMapper<ForumArticle, ForumCommentQuery> forumArticleMapper;

	@Resource
	private UserInfoService userInfoService;

	@Resource
	private UserMessageService userMessageService;

	@Resource
	private FileUtils fileUtils;

	@Lazy
	@Resource
	private ForumCommentService forumCommentService;
	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<ForumComment> findListByParam(ForumCommentQuery param) {

		// 查一级评论
		List<ForumComment> list = this.forumCommentMapper.selectList(param);
		// 获取二级评论
		if (param.getLoadChildren() != null && param.getLoadChildren()) {
			ForumCommentQuery subQuery = new ForumCommentQuery();
			subQuery.setQueryLikeType(param.getQueryLikeType());
			subQuery.setCurrentUserId(param.getCurrentUserId());
			subQuery.setArticleId(param.getArticleId());
//			subQuery.setLoadChildren(param.getLoadChildren());
			subQuery.setStatus(param.getStatus());
//			subQuery.setOnlyQueryChildren(true);

			List<Integer> pcommentIdList = list.stream().map(ForumComment::getCommentId).distinct().collect(Collectors.toList());


			subQuery.setPcommentIdList(pcommentIdList);

			List<ForumComment> subCommentList = this.forumCommentMapper.selectList(subQuery);

			// 根据pcomment分组 将二级评论转换为一个临时的map
			Map<Integer, List<ForumComment>> tempMap =subCommentList.stream().collect(Collectors.groupingBy(ForumComment::getpCommentId));

			list.forEach(item -> {
				item.setChildren(tempMap.get(item.getCommentId()));
			});
		}

		return list;
	}


	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(ForumCommentQuery param) {
		return this.forumCommentMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<ForumComment> findListByPage(ForumCommentQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<ForumComment> list = this.findListByParam(param);
		PaginationResultVO<ForumComment> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(),page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(ForumComment bean){
		return this.forumCommentMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<ForumComment> listBean){
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.forumCommentMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<ForumComment> listBean){
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.forumCommentMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 根据CommentId获取对象
	 */
	@Override
	public ForumComment getForumCommentByCommentId(Integer commentId){
		return this.forumCommentMapper.selectByCommentId(commentId);
	}

	/**
	 * 根据CommentId修改
	 */
	@Override
	public Integer updateForumCommentByCommentId(ForumComment bean,Integer commentId){
		return this.forumCommentMapper.updateByCommentId(bean,commentId);
	}

	/**
	 * 根据CommentId删除
	 */
	@Override
	public Integer deleteForumCommentByCommentId(Integer commentId){
		return this.forumCommentMapper.deleteByCommentId(commentId);
	}

	/**
	 * 评论置顶与取消
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void changeTopType(String userId, Integer commentId, Integer topType) {
		CommentTopTypeEnum typeEnum = CommentTopTypeEnum.getByType(topType);
		// 参数不对
		if (null == typeEnum) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}

		// 查询是否有该条评论
		ForumComment forumComment = forumCommentMapper.selectByCommentId(commentId);
		if (forumComment == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}

		// 根据评论查询文章
		ForumArticle forumArticle = forumArticleMapper.selectByArticleId(forumComment.getArticleId());

		if (forumArticle == null) {

			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}

		// 不是该文章作者 || 不是一级评论
		if (!forumArticle.getUserId().equals(userId) || forumComment.getpCommentId() != 0) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		// 如果置顶状态和数据库相同不操作

		if (forumComment.getTopType().equals(topType)) {

			return;
		}


		// 置顶
		if (CommentTopTypeEnum.TOP.getType().equals(topType)) {
			forumCommentMapper.updateTopTypeByArticleId(forumComment.getArticleId());
		}

		ForumComment updateInfo = new ForumComment();
		updateInfo.setTopType(topType);
		forumCommentMapper.updateByCommentId(updateInfo, commentId);
	}

	/**
	 * 发布评论
	 * @param comment
	 * @param image
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void postComment(ForumComment comment, MultipartFile image) {
		// 判断文章是否存在
		ForumArticle forumArticle = forumArticleMapper.selectByArticleId(comment.getArticleId());
		if (forumArticle == null || !ArticleStatusEnum.AUDIT.getStatus () .equals( forumArticle.getStatus())) {
			// 如果文章没有 或者 文章未审核
			throw new BusinessException("评论的文章不存在");
		}
		// 判断父级评论是否存在
		ForumComment pComment = null;
		if (comment.getpCommentId() != 0) {
			pComment = forumCommentMapper.selectByCommentId(comment.getpCommentId());
			if (pComment == null) {
				// 判断父评论是否存在
				throw new BusinessException("回复的评论不存在");
			}
		}
		// 判断回复的用户是否存在
		if (!StringTools.isEmpty(comment.getReplyUserId())) {
			UserInfo userInfo = userInfoService.getUserInfoByUserId(comment.getReplyUserId());
			if (userInfo == null) {
				throw new BusinessException("回复的用户不存在");
			}
			comment.setReplyNickName(userInfo.getNickName());
		}
		comment.setPostTime(new Date());
		if (image != null) {
			// 上传图片（文件对象，评论枚举对象[后缀名]---重要， 文件共有路径）
			FileUploadDto fileUploadDto = fileUtils.uploadFile2Local(image, FileUploadTypeEnum.COMMENT_IMAGE, Constants.FILE_FOLDER_IMAGE);
			// 设置文件路径
			comment.setImgPath(fileUploadDto.getLocalPath());
		}

		// 查看是否需要审核
		Boolean needAudit = SysCacheUtils.getSysSetting().getAuditStting().getCommentAudit();

		// 设置状态
		comment.setStatus(needAudit ? CommentStatusEnum.NO_AUDIT.getStatus() : CommentStatusEnum.AUDIT.getStatus());
		this.forumCommentMapper.insert(comment);

		//判断是否需要审核
		if (needAudit) {
			// 需要审核直接结束
			return;
		}

		// 如果不需要审核 需要跟新数据库，并发送消息
		updateCommentInfo(comment, forumArticle, pComment);

	}

	public void updateCommentInfo(ForumComment comment, ForumArticle forumArticle, ForumComment pComment) {
		// 获取管理员设置的积分
		Integer commentPoints = SysCacheUtils.getSysSetting().getCommentSetting().getCommentPoints();

		if (commentPoints > 0) {
//			System.out.printf("**************************************************************************************************");

			this.userInfoService.updateUserPoints(comment.getUserId(), UserPointsOperTypeEnum.POST_COMMENT, UpdateArticleCountTypeEnum.UserPointsChangeTypeEnum.ADD.getChangeType(), commentPoints);
		}

		if (comment.getpCommentId() == 0) {
			// 如果是一级评论 更改该文章的评论数量
			this.forumArticleMapper.updateArticleCount(UpdateArticleCountTypeEnum.COMMENT_COUNT.getType(), Constants.ONE, comment.getArticleId());
		}

		// 记录消息
		UserMessage userMessage = new UserMessage();
		userMessage.setMessageType(MessageTypeEnum.COMMENT.getType());
		userMessage.setCreateTime(new Date());
		userMessage.setArticleId(forumArticle.getArticleId());
		userMessage.setCommentId(comment.getCommentId());
		userMessage.setSendUserId(comment.getUserId());
		userMessage.setSendNickName(comment.getNickName());
		userMessage.setStatus(MessageStatusEnum.NO_READ.getStatus());
		userMessage.setMessageContent(comment.getContent());
		userMessage.setArticleTitle(forumArticle.getTitle());
		if (comment.getpCommentId() == 0) {
			// 一级评论 发送给文章作者
			userMessage.setReceivedUserId(forumArticle.getUserId());
		} else if (comment.getpCommentId() != 0 && StringTools.isEmpty(comment.getReplyUserId())) {
			// 回复的是一级评论
			userMessage.setReceivedUserId(pComment.getUserId());
		} else if (comment.getpCommentId() != 0 && !StringTools.isEmpty(comment.getReplyUserId())) {
			// 回复的是二级评论
			userMessage.setReceivedUserId(comment.getReplyUserId());
		}
		if (!comment.getUserId().equals(userMessage.getReceivedUserId())) {
			// 发布评论人不是本人的情况下才
			userMessageService.add(userMessage);
		}
	}


	@Override
	public void delComment(String commentIds) {

		String[] commentIdArray = commentIds.split(",");
		for (String commentIdStr : commentIdArray) {
			Integer commentId = Integer.parseInt(commentIdStr);
			delCommentSingle(commentId);
		}
	}
    @Override
	@Transactional(rollbackFor = Exception.class)
	public void delCommentSingle(Integer commentId) {
		ForumComment comment = forumCommentMapper.selectByCommentId(commentId);
		if (null == comment || CommentStatusEnum.DEL.getStatus().equals(comment.getStatus())) {
			return;
		}
		ForumComment forumComment = new ForumComment();
		forumComment.setStatus(CommentStatusEnum.DEL.getStatus());
		forumCommentMapper.updateByCommentId(forumComment, commentId);

		// 删除已经审核的评论，更新评论数量
		if (CommentStatusEnum.AUDIT.getStatus().equals(comment.getStatus())) {
			if (comment.getpCommentId() == 0) {
				forumArticleMapper.updateArticleCount(UpdateArticleCountTypeEnum.COMMENT_COUNT.getType(), -1, comment.getArticleId());
			}
			Integer points = SysCacheUtils.getSysSetting().getCommentSetting().getCommentPoints();
			userInfoService.updateUserPoints(comment.getUserId(), UserPointsOperTypeEnum.DEL_COMMENT, UpdateArticleCountTypeEnum.UserPointsChangeTypeEnum.REDUCE.getChangeType(), points);
		}

		UserMessage userMessage = new UserMessage();
		userMessage.setReceivedUserId(comment.getUserId());
		userMessage.setMessageType(MessageTypeEnum.SYS.getType());
		userMessage.setCreateTime(new Date());
		userMessage.setStatus(MessageStatusEnum.NO_READ.getStatus());
//		userMessage.setMessageContent("评论【" + comment.getContent() + "】被管理员删除");
		userMessage.setMessageContent("haihaihai 评论  ( ﹁ ﹁ ) ~→" + comment.getContent()  + "被管理员删除了  ┭┮﹏┭┮");
		userMessageService.add(userMessage);
	}

	@Override
	public void auditComment(String commentIds) {
		String[] commentIdArray = commentIds.split(",");
		for (String commentIdStr : commentIdArray) {
			Integer commentId = Integer.parseInt(commentIdStr);
			forumCommentService.auditCommentSingle(commentId);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void auditCommentSingle(Integer commentId) {
		ForumComment comment = forumCommentMapper.selectByCommentId(commentId);
		if (!CommentStatusEnum.NO_AUDIT.getStatus().equals(comment.getStatus())) {
			// 如果已经审核过
			return;
		}
		ForumComment forumComment = new ForumComment();
		forumComment.setStatus(CommentStatusEnum.AUDIT.getStatus());
		forumCommentMapper.updateByCommentId(forumComment, commentId);

		ForumArticle forumArticle = forumArticleMapper.selectByArticleId(comment.getArticleId());
		ForumComment pComment = null;
		if (comment.getpCommentId() != 0 && StringTools.isEmpty(comment.getReplyUserId())) {
			// 如果不是一级评论 并且 是回复评论
			pComment = forumCommentMapper.selectByCommentId(comment.getpCommentId());
		}
		// 更新评论相关信息
		updateCommentInfo(comment, forumArticle, pComment);
	}
}