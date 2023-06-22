package com.GPbbs.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import com.GPbbs.entity.po.ForumArticle;
import com.GPbbs.exception.BusinessException;
import com.GPbbs.mappers.ForumArticleMapper;
import com.GPbbs.service.ForumArticleService;
import org.springframework.stereotype.Service;

import com.GPbbs.entity.enums.PageSize;
import com.GPbbs.entity.query.ForumBoardQuery;
import com.GPbbs.entity.po.ForumBoard;
import com.GPbbs.entity.vo.PaginationResultVO;
import com.GPbbs.entity.query.SimplePage;
import com.GPbbs.mappers.ForumBoardMapper;
import com.GPbbs.service.ForumBoardService;
import org.springframework.transaction.annotation.Transactional;


/**
 * 
 * 文章板块信息 业务接口实现
 * 
 */
@Service("forumBoardService")
public class ForumBoardServiceImpl implements ForumBoardService {

	@Resource
	private ForumBoardMapper<ForumBoard,ForumBoardQuery> forumBoardMapper;

	@Resource
	private ForumArticleMapper<ForumArticle, ForumBoardQuery> forumArticleMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<ForumBoard> findListByParam(ForumBoardQuery param) {
		return this.forumBoardMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(ForumBoardQuery param) {
		return this.forumBoardMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<ForumBoard> findListByPage(ForumBoardQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<ForumBoard> list = this.findListByParam(param);
		PaginationResultVO<ForumBoard> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(),page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(ForumBoard bean){
		return this.forumBoardMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<ForumBoard> listBean){
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.forumBoardMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<ForumBoard> listBean){
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.forumBoardMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 根据BoardId获取对象
	 */
	@Override
	public ForumBoard getForumBoardByBoardId(Integer boardId){
		return this.forumBoardMapper.selectByBoardId(boardId);
	}

	/**
	 * 根据BoardId修改
	 */
	@Override
	public Integer updateForumBoardByBoardId(ForumBoard bean,Integer boardId){
		return this.forumBoardMapper.updateByBoardId(bean,boardId);
	}

	/**
	 * 根据BoardId删除
	 */
	@Override
	public Integer deleteForumBoardByBoardId(Integer boardId){
		return this.forumBoardMapper.deleteByBoardId(boardId);
	}

	/**
	 * 获取Board信息
	 * @param postType
	 * @return
	 */
	@Override
	public List<ForumBoard> getBoardTree(Integer postType) {
		// 查询设置
		ForumBoardQuery forumBoardQuery = new ForumBoardQuery();
		forumBoardQuery.setOrderBy("sort asc");
		forumBoardQuery.setPostType(postType);
		// 获取
		List<ForumBoard> forumBoardList = this.forumBoardMapper.selectList(forumBoardQuery);
		return convertLine2Tree(forumBoardList, 0);
	}

	/**
	 * 将获取到的线性数据转换未树形结构
	 * @param dataList
	 * @param pid
	 * @return
	 */
	private List<ForumBoard> convertLine2Tree(List<ForumBoard> dataList, Integer pid) {

		List<ForumBoard> children = new ArrayList<>();
		for (ForumBoard m : dataList) {
			if (m.getpBoardId().equals(pid)) {
				// 使用递归
				m.setChildren(convertLine2Tree(dataList, m.getBoardId()));
				children.add(m);
			}
		}
		return children;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveForumBoard(ForumBoard forumBoard) {

		if (forumBoard.getBoardId() == null) {
			ForumBoardQuery boardQuery = new ForumBoardQuery();
			boardQuery.setpBoardId(forumBoard.getpBoardId());
			Integer count = this.forumBoardMapper.selectCount(boardQuery);
			forumBoard.setSort(count + 1);
			this.forumBoardMapper.insert(forumBoard);
		} else {
			ForumBoard dbInfo = this.forumBoardMapper.selectByBoardId(forumBoard.getBoardId());
			if (dbInfo == null) {
				throw new BusinessException("板块不存在");
			}
			this.forumBoardMapper.updateByBoardId(forumBoard, forumBoard.getBoardId());

			if (!dbInfo.getBoardName().equals(forumBoard.getBoardName())) {
				forumArticleMapper.updateBoardNameBatch(dbInfo.getpBoardId() == 0 ? 0 : 1, forumBoard.getBoardName(), forumBoard.getBoardId());
			}
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void changeSort(String boardIds) {
		String[] boardIdArray = boardIds.split(",");
		Integer index = 1;
		for (String boardIdStr : boardIdArray) {
			Integer boardId = Integer.parseInt(boardIdStr);
			ForumBoard board = new ForumBoard();
			board.setSort(index);
			forumBoardMapper.updateByBoardId(board, boardId);
			index++;
		}
	}
}