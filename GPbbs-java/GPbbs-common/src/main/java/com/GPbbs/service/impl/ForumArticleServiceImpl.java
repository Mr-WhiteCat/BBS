package com.GPbbs.service.impl;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.GPbbs.entity.config.AppConfig;
import com.GPbbs.entity.constants.Constants;
import com.GPbbs.entity.dto.FileUploadDto;
import com.GPbbs.entity.dto.SysSetting4AuditDto;
import com.GPbbs.entity.enums.*;
import com.GPbbs.entity.po.ForumArticleAttachment;
import com.GPbbs.entity.po.ForumBoard;
import com.GPbbs.entity.po.UserMessage;
import com.GPbbs.entity.query.ForumArticleAttachmentQuery;
import com.GPbbs.exception.BusinessException;
import com.GPbbs.mappers.ForumArticleAttachmentMapper;
import com.GPbbs.service.ForumBoardService;
import com.GPbbs.service.UserInfoService;
import com.GPbbs.service.UserMessageService;
import com.GPbbs.utils.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.GPbbs.entity.query.ForumArticleQuery;
import com.GPbbs.entity.po.ForumArticle;
import com.GPbbs.entity.vo.PaginationResultVO;
import com.GPbbs.entity.query.SimplePage;
import com.GPbbs.mappers.ForumArticleMapper;
import com.GPbbs.service.ForumArticleService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


/**
 * 
 * 文章信息 业务接口实现
 * 
 */
@Service("forumArticleService")
public class ForumArticleServiceImpl implements ForumArticleService {

	@Resource
	private ForumArticleMapper<ForumArticle,ForumArticleQuery> forumArticleMapper;

	@Resource
	private ForumBoardService forumBoardService;

	@Resource
	private FileUtils fileUtils;

	@Resource
	private ForumArticleAttachmentMapper<ForumArticleAttachment, ForumArticleAttachmentQuery> forumArticleAttachmentMapper;

	@Resource
	private UserInfoService userInfoService;

	@Resource
	private ImageUtils imageUtils;

	@Resource
	private AppConfig appConfig;

	@Lazy
	@Resource
	private ForumArticleService forumArticleService;

	@Resource
	private UserMessageService userMessageService;


	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<ForumArticle> findListByParam(ForumArticleQuery param) {
		return this.forumArticleMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(ForumArticleQuery param) {
		return this.forumArticleMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<ForumArticle> findListByPage(ForumArticleQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<ForumArticle> list = this.findListByParam(param);
		PaginationResultVO<ForumArticle> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(),page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(ForumArticle bean){
		return this.forumArticleMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<ForumArticle> listBean){
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.forumArticleMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<ForumArticle> listBean){
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.forumArticleMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 根据ArticleId获取对象
	 */
	@Override
	public ForumArticle getForumArticleByArticleId(String articleId){
		return this.forumArticleMapper.selectByArticleId(articleId);
	}

	/**
	 * 根据ArticleId修改
	 */
	@Override
	public Integer updateForumArticleByArticleId(ForumArticle bean,String articleId){
		return this.forumArticleMapper.updateByArticleId(bean,articleId);
	}

	/**
	 * 根据ArticleId删除
	 */
	@Override
	public Integer deleteForumArticleByArticleId(String articleId){
		return this.forumArticleMapper.deleteByArticleId(articleId);
	}

	/**
	 * 根据ArticleId读取文章
	 */
	@Override
	public ForumArticle readArticle(String artcileId) {
		ForumArticle forumArticle = this.forumArticleMapper.selectByArticleId(artcileId);
		if (forumArticle == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_404);
		}
		if (ArticleStatusEnum.AUDIT.getStatus().equals(forumArticle.getStatus())) {
			forumArticleMapper.updateArticleCount(UpdateArticleCountTypeEnum.READ_COUNT.getType(), Constants.ONE, artcileId);
		}
		return forumArticle;
	}

	/**
	 * 发布文章
	 * 管理员发帖不需要审核
	 */
	@Override
	public void postArticle(Boolean isAdmin, ForumArticle article, ForumArticleAttachment articleAttachment, MultipartFile cover, MultipartFile attachment) {
		resetBoardInfo(isAdmin, article);

		// 生成articleId
		String articleId = StringTools.getRandomString(Constants.LENGTH_15);
		article.setArticleId(articleId);

		Date curDate = new Date();
		article.setPostTime(curDate);
		article.setLastUpdateTime(curDate);

		if (cover != null) {
			// 如果有封面
			FileUploadDto fileUploadDto = fileUtils.uploadFile2Local(cover, FileUploadTypeEnum.ARTICLE_COVER, Constants.FILE_FOLDER_IMAGE);
			// 设置封面路径
			article.setCover(fileUploadDto.getLocalPath());

		}


		if (attachment != null) {
			// 设置文章是否有附件
			article.setAttachmentType(ArticleAttachmentTypeEnum.HAVE_ATTACHMENT.getType());
			//上传附件
			uploadAttachment(article, articleAttachment, attachment, false);
		} else {
			article.setAttachmentType(ArticleAttachmentTypeEnum.NO_ATTACHMENT.getType());
		}

		// 设置审核信息
		if (isAdmin) {
			article.setStatus(ArticleStatusEnum.AUDIT.getStatus());
		} else {
			SysSetting4AuditDto auditDto = SysCacheUtils.getSysSetting().getAuditStting();
			article.setStatus(auditDto.getPostAudit() ? ArticleStatusEnum.NO_AUDIT.getStatus() : ArticleStatusEnum.AUDIT.getStatus());
		}

		// 替换图片
		String content = article.getContent();
		if (!StringTools.isEmpty(content)) {
			String month = imageUtils.resetImageHtml(content);
			//避免替换博客中template关键，所以前后带上/
			String replaceMonth = "/" + month + "/";
			content = content.replace(Constants.FILE_FOLDER_TEMP, replaceMonth);
			article.setContent(content);
			String markdownContent = article.getMarkdownContent();
			if (!StringTools.isEmpty(markdownContent)) {
				// 如果有markdown内容
				markdownContent = markdownContent.replace(Constants.FILE_FOLDER_TEMP, replaceMonth);
				article.setMarkdownContent(markdownContent);
			}
		}

		this.forumArticleMapper.insert(article);

		// 增加积分
		Integer postPoints = SysCacheUtils.getSysSetting().getPostSetting().getPostPoints();

		if (postPoints > 0 && ArticleStatusEnum.AUDIT.getStatus().equals(article.getStatus())) {
			// 更新用户积分

			this.userInfoService.updateUserPoints(article.getUserId(),UserPointsOperTypeEnum.POST_COMMENT, UpdateArticleCountTypeEnum.UserPointsChangeTypeEnum.ADD.getChangeType(), postPoints);
		}

	}

	/**
	 * 校验后端板块信息，避免浏览器直接输入板块id进行编辑
	 *
	 * @param isAdmin
	 * @param forumArticle
	 */
	private void resetBoardInfo(Boolean isAdmin, ForumArticle forumArticle) {
		ForumBoard board = forumBoardService.getForumBoardByBoardId(forumArticle.getpBoardId());
		if (null == board || board.getPostType() == Constants.ZERO && !isAdmin) {
			// 板块不存在 || 只允许板块管理员发帖
			throw new BusinessException("一级板块不存在");
		}

		forumArticle.setpBoardName(board.getBoardName());

		if (forumArticle.getBoardId() != null && forumArticle.getBoardId() != Constants.ZERO) {
			// 如果是二级板块文章
			board = forumBoardService.getForumBoardByBoardId(forumArticle.getBoardId());
			if (null == board || board.getPostType() == 0 && !isAdmin) {
				throw new BusinessException("二级板块不存在");
			}
			forumArticle.setBoardName(board.getBoardName());
		} else {
			// 一级板块文章
			forumArticle.setBoardId(0);
			forumArticle.setBoardName("");
		}
	}

	/**
	 * 上传附件 设置增加积分
	 * @param article
	 * @param attachment
	 * @param file
	 * @param isUpdate
	 */
	public void uploadAttachment(ForumArticle article, ForumArticleAttachment attachment, MultipartFile file, Boolean isUpdate) {
		// 读取最大上传附件大小
		Integer allowSizeMb = SysCacheUtils.getSysSetting().getPostSetting().getAttachmentSize();
		long allowSize = allowSizeMb * Constants.FILE_SIZE_1M;
		if (file.getSize() > allowSize) {
			// 如果大于限制
			throw new BusinessException("附件最大只能" + allowSizeMb + "MB");
		}
		ForumArticleAttachment dbInfo = null;
		if (isUpdate) {
			// 查询附件在数据库中查询
			ForumArticleAttachmentQuery attachmentQuery = new ForumArticleAttachmentQuery();
			attachmentQuery.setArticleId(article.getArticleId());
			List<ForumArticleAttachment> articleAttachmentList = this.forumArticleAttachmentMapper.selectList(attachmentQuery);
			if (!articleAttachmentList.isEmpty()) {
				// 如果有附件
				dbInfo = articleAttachmentList.get(0);
				// 删除之前的附件
				new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_ATTACHMENT + dbInfo.getFilePath()).delete();
			}
		}

		// 上传文件
		FileUploadDto fileUploadDto = fileUtils.uploadFile2Local(file, FileUploadTypeEnum.ARTICLE_ATTACHMENT, Constants.FILE_FOLDER_ATTACHMENT);
		if (dbInfo == null) {
			// 如果不更新文件，或者没有相关文件记录
			attachment.setFileId(StringTools.getRandomNumber(Constants.LENGTH_15));
			attachment.setArticleId(article.getArticleId());
			attachment.setFileName(fileUploadDto.getOriginalFilename());
			attachment.setFilePath(fileUploadDto.getLocalPath());
			attachment.setFileSize(file.getSize());
			attachment.setDownloadCount(Constants.ZERO);
			attachment.setUserId(article.getUserId());
			attachment.setFileType(AttachmentFileTypeEnum.ZIP.getType());
			forumArticleAttachmentMapper.insert(attachment);
		} else {
			//  要更新附件，并且有相关记录（在数据库里有相关数据）
			ForumArticleAttachment updateInfo = new ForumArticleAttachment();
			updateInfo.setFileName(fileUploadDto.getOriginalFilename());
			// 更新文件名称
			updateInfo.setFileSize(file.getSize());
			// 更新文件大小
			updateInfo.setFilePath(fileUploadDto.getLocalPath());
			// 更新文件路径
			forumArticleAttachmentMapper.updateByFileId(updateInfo, dbInfo.getFileId());
		}
	}

	/**
	 * 访客端修改文章
	 * @param isAdmin
	 * @param article
	 * @param articleAttachment
	 * @param cover
	 * @param attachment
	 */
	@Override
	public void updateArticle(Boolean isAdmin, ForumArticle article, ForumArticleAttachment articleAttachment, MultipartFile cover, MultipartFile attachment) {

		// 判断修改文章文章是否是本人
		ForumArticle dbInfo = forumArticleMapper.selectByArticleId(article.getArticleId());
		if (!isAdmin && !dbInfo.getUserId().equals(article.getUserId())) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		article.setLastUpdateTime(new Date());
		resetBoardInfo(isAdmin, article);

		if (cover != null) {
			// 如果有封面
			FileUploadDto fileUploadDto = fileUtils.uploadFile2Local(cover, FileUploadTypeEnum.ARTICLE_COVER, Constants.FILE_FOLDER_IMAGE);
			article.setCover(fileUploadDto.getLocalPath());
		}

		if (attachment != null) {
			article.setAttachmentType(ArticleAttachmentTypeEnum.HAVE_ATTACHMENT.getType());
			//上传附件
			uploadAttachment(article, articleAttachment, attachment, true);
		}

		// 获取数据库中的附件记

		ForumArticleAttachmentQuery attachmentQuery = new ForumArticleAttachmentQuery();
		attachmentQuery.setArticleId(article.getArticleId());
		List<ForumArticleAttachment> articleAttachmentList = this.forumArticleAttachmentMapper.selectList(attachmentQuery);
		ForumArticleAttachment dbAttachment = null;

		if (!articleAttachmentList.isEmpty()) {
			dbAttachment = articleAttachmentList.get(0);
		}

		if (dbAttachment != null) {
			if (article.getAttachmentType() == 0) {
				// 如果修改文章时，不传附件
				// 删除之前的附件
				new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_ATTACHMENT + dbAttachment.getFilePath()).delete();
				// 删除记录
				this.forumArticleAttachmentMapper.deleteByFileId(dbAttachment.getFileId());
			} else {
				// 更新积分
				if (!dbAttachment.getPoints().equals(articleAttachment.getPoints())) {
					// 如果用户设置下载积分改变
					ForumArticleAttachment pointsUpdate = new ForumArticleAttachment();
					pointsUpdate.setPoints(articleAttachment.getPoints());
					// 更新数据库中文件积分
					this.forumArticleAttachmentMapper.updateByFileId(pointsUpdate, dbAttachment.getFileId());
				}
			}
		}


		// 文章是否需要审核
		if (isAdmin) {
			// 如果是管理员，直接发帖
			article.setStatus(ArticleStatusEnum.AUDIT.getStatus());
		} else {
			SysSetting4AuditDto auditDto = SysCacheUtils.getSysSetting().getAuditStting();
			article.setStatus(auditDto.getPostAudit() ? ArticleStatusEnum.NO_AUDIT.getStatus() :
					ArticleStatusEnum.AUDIT.getStatus());
		}

		// 替换图片
		String content = article.getContent();
		if (!StringTools.isEmpty(content)) {
			String month = imageUtils.resetImageHtml(content);
			// 拿到content中的图片
			// 避免替换博客中template关键，所以前后带上/
			String replaceMonth = "/" + month + "/";
			content = content.replace(Constants.FILE_FOLDER_TEMP, replaceMonth);
			article.setContent(content);
			String markdownContent = article.getMarkdownContent();
			if (!StringTools.isEmpty(markdownContent)) {
				markdownContent = markdownContent.replace(Constants.FILE_FOLDER_TEMP, replaceMonth);
				article.setMarkdownContent(markdownContent);
			}
		}

		this.forumArticleMapper.updateByArticleId(article, article.getArticleId());
	}

	@Override
	public void delArticle(String articleIds) {
		String[] articleIdArray = articleIds.split(",");
		for (String articleId : articleIdArray) {
			forumArticleService.delArticleSignle(articleId);
			// 这里不是直接调用的
		}
	}

	/**
	 * 管理端删除文章
	 * @param articleId
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delArticleSignle(String articleId) {
		ForumArticle article = forumArticleMapper.selectByArticleId(articleId);
		if (null == article || ArticleStatusEnum.DEL.getStatus().equals(article.getStatus())) {
			// 如果是已经删除的
			return;
		}

		// 进行数据更新
		ForumArticle updateInfo = new ForumArticle();
		updateInfo.setStatus(ArticleStatusEnum.DEL.getStatus());
		forumArticleMapper.updateByArticleId(updateInfo, articleId);

		// 删除文章减去积分
		Integer points = SysCacheUtils.getSysSetting().getPostSetting().getPostPoints();
		if (points > 0 && ArticleStatusEnum.AUDIT.getStatus().equals(article.getStatus())) {
			userInfoService.updateUserPoints(article.getUserId(), UserPointsOperTypeEnum.DEL_ARTICLE, UpdateArticleCountTypeEnum.UserPointsChangeTypeEnum.REDUCE.getChangeType(), points);
		}
		// 发送消息
		UserMessage userMessage = new UserMessage();
		userMessage.setReceivedUserId(article.getUserId());
		userMessage.setMessageType(MessageTypeEnum.SYS.getType());
		userMessage.setCreateTime(new Date());
		userMessage.setStatus(MessageStatusEnum.NO_READ.getStatus());
		userMessage.setMessageContent("嗨！你发布的文章  ( ﹁ ﹁ ) ~→" + article.getTitle() + "被管理员删除啦  ┭┮﹏┭┮");
		userMessageService.add(userMessage);
	}

	/**
	 * 管理端重置板块
	 */
	@Override
	public void updateBoard(String articleId, Integer pBoardId, Integer boardId) {

		ForumArticle forumArticle = new ForumArticle();
		forumArticle.setpBoardId(pBoardId);
		forumArticle.setBoardId(boardId);
		resetBoardInfo(true, forumArticle);
		forumArticleMapper.updateByArticleId(forumArticle, articleId);
	}

	/**
	 * 审核文章
	 * @param articleIds
	 */
	@Override
	public void auditArticle(String articleIds) {
		String[] articleIdArray = articleIds.split(",");
		for (String articleId : articleIdArray) {
			forumArticleService.auditArticleSignle(articleId);
			// 这里不是直接调用的
		}

	}

	/**
	 * 审核单个文章
	 * @param articleId
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void auditArticleSignle(String articleId) {

		ForumArticle article = getForumArticleByArticleId(articleId);
		if (article == null || !ArticleStatusEnum.NO_AUDIT.getStatus().equals(article.getStatus())) {
			return;
		}
		ForumArticle updateInfo = new ForumArticle();
		updateInfo.setStatus(ArticleStatusEnum.AUDIT.getStatus());
		forumArticleMapper.updateByArticleId(updateInfo, articleId);

		Integer points = SysCacheUtils.getSysSetting().getPostSetting().getPostPoints();
		if (points > 0) {
			userInfoService.updateUserPoints(article.getUserId(), UserPointsOperTypeEnum.POST_ARTICLE, UpdateArticleCountTypeEnum.UserPointsChangeTypeEnum.ADD.getChangeType(), points);
		}
	}
}