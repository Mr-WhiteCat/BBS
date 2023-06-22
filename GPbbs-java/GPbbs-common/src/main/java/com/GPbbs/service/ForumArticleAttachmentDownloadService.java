package com.GPbbs.service;

import java.util.List;

import com.GPbbs.entity.query.ForumArticleAttachmentDownloadQuery;
import com.GPbbs.entity.po.ForumArticleAttachmentDownload;
import com.GPbbs.entity.vo.PaginationResultVO;


/**
 * 
 * 用户附件下载 业务接口
 * 
 */
public interface ForumArticleAttachmentDownloadService {

	/**
	 * 根据条件查询列表
	 */
	List<ForumArticleAttachmentDownload> findListByParam(ForumArticleAttachmentDownloadQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(ForumArticleAttachmentDownloadQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<ForumArticleAttachmentDownload> findListByPage(ForumArticleAttachmentDownloadQuery param);

	/**
	 * 新增
	 */
	Integer add(ForumArticleAttachmentDownload bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<ForumArticleAttachmentDownload> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<ForumArticleAttachmentDownload> listBean);

	/**
	 * 根据FileIdAndUserId查询对象
	 */
	ForumArticleAttachmentDownload getForumArticleAttachmentDownloadByFileIdAndUserId(String fileId,String userId);


	/**
	 * 根据FileIdAndUserId修改
	 */
	Integer updateForumArticleAttachmentDownloadByFileIdAndUserId(ForumArticleAttachmentDownload bean,String fileId,String userId);


	/**
	 * 根据FileIdAndUserId删除
	 */
	Integer deleteForumArticleAttachmentDownloadByFileIdAndUserId(String fileId,String userId);

}