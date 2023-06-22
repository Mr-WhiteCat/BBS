package com.GPbbs.service;

import java.util.List;

import com.GPbbs.entity.po.ForumArticleAttachment;
import com.GPbbs.entity.query.ForumArticleQuery;
import com.GPbbs.entity.po.ForumArticle;
import com.GPbbs.entity.vo.PaginationResultVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.multipart.MultipartFile;


/**
 * 
 * 文章信息 业务接口
 * 
 */
public interface ForumArticleService {

	/**
	 * 根据条件查询列表
	 */
	List<ForumArticle> findListByParam(ForumArticleQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(ForumArticleQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<ForumArticle> findListByPage(ForumArticleQuery param);

	/**
	 * 新增
	 */
	Integer add(ForumArticle bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<ForumArticle> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<ForumArticle> listBean);

	/**
	 * 根据ArticleId查询对象
	 */
	ForumArticle getForumArticleByArticleId(String articleId);


	/**
	 * 根据ArticleId修改
	 */
	Integer updateForumArticleByArticleId(ForumArticle bean,String articleId);


	/**
	 * 根据ArticleId删除
	 */
	Integer deleteForumArticleByArticleId(String articleId);

	/**
	 * 根据ArticleId读取文章
	 */
	ForumArticle readArticle(String artcileId);

	/**
	 * 发布文章
	 * 管理员发帖不需要审核
	 */
	void postArticle(Boolean isAdmin, ForumArticle article, ForumArticleAttachment articleAttachment, MultipartFile cover,MultipartFile attachment);

	/**
	 * 新增稳扎那个
	 */
	void updateArticle(Boolean isAdmin, ForumArticle article, ForumArticleAttachment articleAttachment, MultipartFile cover,MultipartFile attachment);

	/**
	 * 自定义删除方法
	 */
	void delArticle(String articleIds);

	/**
	 * 删除单个文章
	 */
	void delArticleSignle(String articleId);

	/**
	 * 重置板块
	 */
	void updateBoard(String articleId, Integer pBoardId, Integer boardId);

	/**
	 * 审核文章
	 */
	void auditArticle(String articleIds);

	/**
	 * 审核单个文章
	 */
	void auditArticleSignle(String articleId);



}