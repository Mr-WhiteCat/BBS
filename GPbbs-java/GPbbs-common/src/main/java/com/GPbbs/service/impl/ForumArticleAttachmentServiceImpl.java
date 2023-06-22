package com.GPbbs.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.GPbbs.entity.dto.SessionWebUserDto;
import com.GPbbs.entity.enums.*;
import com.GPbbs.entity.po.*;
import com.GPbbs.entity.query.ForumArticleAttachmentDownloadQuery;
import com.GPbbs.exception.BusinessException;
import com.GPbbs.mappers.ForumArticleAttachmentDownloadMapper;
import com.GPbbs.service.ForumArticleService;
import com.GPbbs.service.UserInfoService;
import com.GPbbs.service.UserMessageService;
import org.springframework.stereotype.Service;

import com.GPbbs.entity.query.ForumArticleAttachmentQuery;
import com.GPbbs.entity.vo.PaginationResultVO;
import com.GPbbs.entity.query.SimplePage;
import com.GPbbs.mappers.ForumArticleAttachmentMapper;
import com.GPbbs.service.ForumArticleAttachmentService;
import org.springframework.transaction.annotation.Transactional;


/**
 * 
 * 文件信息 业务接口实现
 * 
 */
@Service("forumArticleAttachmentService")
public class ForumArticleAttachmentServiceImpl implements ForumArticleAttachmentService {

	@Resource
	private ForumArticleAttachmentMapper<ForumArticleAttachment,ForumArticleAttachmentQuery> forumArticleAttachmentMapper;

	@Resource
	private ForumArticleAttachmentDownloadMapper<ForumArticleAttachmentDownload, ForumArticleAttachmentDownloadQuery> forumArticleAttachmentDownloadMapper;

	@Resource
	private UserInfoService userInfoService;

	@Resource
	private ForumArticleService forumArticleService;

	@Resource
	private UserMessageService userMessageService;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<ForumArticleAttachment> findListByParam(ForumArticleAttachmentQuery param) {
		return this.forumArticleAttachmentMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(ForumArticleAttachmentQuery param) {
		return this.forumArticleAttachmentMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<ForumArticleAttachment> findListByPage(ForumArticleAttachmentQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<ForumArticleAttachment> list = this.findListByParam(param);
		PaginationResultVO<ForumArticleAttachment> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(),page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(ForumArticleAttachment bean){
		return this.forumArticleAttachmentMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<ForumArticleAttachment> listBean){
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.forumArticleAttachmentMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<ForumArticleAttachment> listBean){
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.forumArticleAttachmentMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 根据FileId获取对象
	 */
	@Override
	public ForumArticleAttachment getForumArticleAttachmentByFileId(String fileId){
		return this.forumArticleAttachmentMapper.selectByFileId(fileId);
	}

	/**
	 * 根据FileId修改
	 */
	@Override
	public Integer updateForumArticleAttachmentByFileId(ForumArticleAttachment bean,String fileId){
		return this.forumArticleAttachmentMapper.updateByFileId(bean,fileId);
	}

	/**
	 * 根据FileId删除
	 */
	@Override
	public Integer deleteForumArticleAttachmentByFileId(String fileId){
		return this.forumArticleAttachmentMapper.deleteByFileId(fileId);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ForumArticleAttachment downloadAttachment(String fileId, SessionWebUserDto sessionWebUserDto) {
		ForumArticleAttachment attachment = this.forumArticleAttachmentMapper.selectByFileId(fileId);
		if (null == attachment) {
			throw new BusinessException("附件不存在");
		}
		// 判断是否下载 ，下载过了就不需要积分
		ForumArticleAttachmentDownload download = null;
		if (attachment.getPoints() > 0 && !sessionWebUserDto.getUserId().equals(attachment.getUserId())) {
			// 不是自己上传的附件
			download = this.forumArticleAttachmentDownloadMapper.selectByFileIdAndUserId(fileId, sessionWebUserDto.getUserId());
			if (download == null) {
				// 没有下载过 需要判断积分
				UserInfo userInfo = userInfoService.getUserInfoByUserId(sessionWebUserDto.getUserId());
				if (userInfo.getCurrentPoints() - attachment.getPoints() < 0) {
					throw new BusinessException("积分不够");
				}
			}
		}
		// 下载记录设置
		ForumArticleAttachmentDownload updateDownload = new ForumArticleAttachmentDownload();
		updateDownload.setArticleId(attachment.getArticleId());
		updateDownload.setFileId(attachment.getFileId());
		updateDownload.setUserId(sessionWebUserDto.getUserId());
		updateDownload.setDownloadCount(1);
		this.forumArticleAttachmentDownloadMapper.insertOrUpdate(updateDownload);

		// 更新下载次数
		this.forumArticleAttachmentMapper.updateDownloadCount(fileId);

		// 如果是上传者本人下载则不需要扣积分 下载过一次
		if (sessionWebUserDto.getUserId().equals(attachment.getUserId()) || download != null) {
			return attachment;
		}

		// 扣减下载者积分
		userInfoService.updateUserPoints(sessionWebUserDto.getUserId(), UserPointsOperTypeEnum.USER_DOWNLOAD_ATTACHMENT,
				UpdateArticleCountTypeEnum.UserPointsChangeTypeEnum.REDUCE.getChangeType(), attachment.getPoints());

		// 给附件上传者增加积分
		userInfoService.updateUserPoints(attachment.getUserId(), UserPointsOperTypeEnum.DOWNLOAD_ATTACHMENT,
				UpdateArticleCountTypeEnum.UserPointsChangeTypeEnum.ADD.getChangeType(), attachment.getPoints());

		// 记录消息
		ForumArticle forumArticle = forumArticleService.getForumArticleByArticleId(attachment.getArticleId());

		UserMessage userMessage = new UserMessage();
		userMessage.setMessageType(MessageTypeEnum.DOWNLOAD_ATTACHMENT.getType());
		userMessage.setCreateTime(new Date());
		userMessage.setArticleId(attachment.getArticleId());
		userMessage.setCommentId(0);
		userMessage.setSendUserId(sessionWebUserDto.getUserId());
		userMessage.setSendNickName(sessionWebUserDto.getNickName());
		userMessage.setStatus(MessageStatusEnum.NO_READ.getStatus());
		userMessage.setReceivedUserId(attachment.getUserId());
		userMessage.setArticleTitle(forumArticle.getTitle());
		// 如果不是本人下载 ，则插入数据库
		if (!sessionWebUserDto.getUserId().equals(attachment.getUserId())) {
			userMessageService.add(userMessage);
		}

		return attachment;
	}
}