package com.GPbbs.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 
 * 文章信息 数据库操作接口
 * 
 */
public interface ForumArticleMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据ArticleId更新
	 */
	 Integer updateByArticleId(@Param("bean") T t,@Param("articleId") String articleId);


	/**
	 * 根据ArticleId删除
	 */
	 Integer deleteByArticleId(@Param("articleId") String articleId);


	/**
	 * 根据ArticleId获取对象
	 */
	 T selectByArticleId(@Param("articleId") String articleId);

	/**
	 * 改变文章阅读数量
	 */
	void updateArticleCount(@Param("updateType") Integer updateType, @Param("changeCount") Integer changeCount, @Param("articleId") String articleId);

	/**
	 * 更新板块时，更具Id更新数据对象
	 */
	void updateBoardNameBatch(@Param("boardType") Integer boardType, @Param ("boardName") String boardName , @Param ("boardId") Integer boardId);


	/**
	 * 批量更改用户账号使用状态
	 */
	void updateStatusBatchByUserId(@Param("status") Integer status, @Param("userId") String userId);



}
