package com.GPbbs.controller;

import com.GPbbs.annotation.GlobalInterceptor;
import com.GPbbs.annotation.VerifyParam;
import com.GPbbs.controller.base.ABaseController;
import com.GPbbs.entity.dto.SessionWebUserDto;
import com.GPbbs.entity.enums.*;
import com.GPbbs.entity.po.ForumComment;
import com.GPbbs.entity.po.LikeRecord;
import com.GPbbs.entity.query.ForumCommentQuery;
import com.GPbbs.entity.vo.ResponseVO;
import com.GPbbs.exception.BusinessException;
import com.GPbbs.service.ForumCommentService;
import com.GPbbs.service.LikeRecordService;
import com.GPbbs.utils.StringTools;
import com.GPbbs.utils.SysCacheUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/comment")
public class ForumCommentController extends ABaseController {

    @Resource
    private ForumCommentService forumCommentService;

    @Resource
    private LikeRecordService likeRecordService;

    /**
     * 加载评论
     * @param session
     * @param articleId
     * @param pageNo
     * @param orderType
     * @return
     */

    @RequestMapping("/loadComment")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO loadComment(HttpSession session,
                                  @VerifyParam(required = true) String articleId,
                                  Integer pageNo,
                                  Integer orderType) {

        final String ORDER_TYPE0 = "good_count desc,comment_id asc"; // 热榜
        final String ORDER_TYPE1 = "comment_id desc"; //最新

        // 如果评论不展开 接口不让掉
        if (!SysCacheUtils.getSysSetting().getCommentSetting().getCommentOpen()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        ForumCommentQuery commentQuery = new ForumCommentQuery();
        commentQuery.setArticleId(articleId);
//        commentQuery.setLoadChildren(true);
        String orderBy = orderType == null || orderType == 0 ? ORDER_TYPE0 : ORDER_TYPE1;
        commentQuery.setOrderBy("top_type desc," + orderBy);


        SessionWebUserDto userDto = getUserInfoFromSession(session);
        if (userDto != null) {
            // 是否查看子评论是否已经点赞
            commentQuery.setQueryLikeType(true);
            // 查询当前用户id
            commentQuery.setCurrentUserId(userDto.getUserId());
        } else {
            commentQuery.setStatus(CommentStatusEnum.AUDIT.getStatus());
        }
        // 设置单页评论数
        commentQuery.setPageSize(PageSize.SIZE50.getSize());
        commentQuery.setPageNo(pageNo);
        commentQuery.setpCommentId(0);
        commentQuery.setLoadChildren(true);
        return getSuccessResponseVO(forumCommentService.findListByPage(commentQuery));
    }

    /**
     * 点赞
     * @param session
     * @param commentId
     * @return
     */

    @RequestMapping("/doLike")
    @GlobalInterceptor(checkLogin = true, checkParams = true)
    public ResponseVO doLike(HttpSession session,
                             @VerifyParam(required = true) Integer commentId){
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        String objectId = String.valueOf(commentId);
        likeRecordService.doLike(objectId, userDto.getUserId(), userDto.getNickName(), OperRecordOpTypeEnum.COMMENT_LIKE);


        // 查询是否存在点赞记录
        LikeRecord userOperRecord = likeRecordService.getLikeRecordByObjectIdAndUserIdAndOpType(objectId, userDto.getUserId(),
                OperRecordOpTypeEnum.COMMENT_LIKE.getType());
        // 查询评论
        ForumComment comment = forumCommentService.getForumCommentByCommentId(commentId);
        comment.setLikeType(userOperRecord == null ? 0 : 1);
        return getSuccessResponseVO(comment);
    }

    /**
     * 评论置顶
     * @param session
     * @param commentId
     * @param topType 取消 或 置顶 （0:取消置顶 1:置顶）
     * @return
     */
    @RequestMapping("/changeTopType")
    @GlobalInterceptor(checkLogin = true, checkParams = true)
    public ResponseVO changeTopType(HttpSession session,
                                    @VerifyParam(required = true) Integer commentId,
                                    @VerifyParam(required = true) Integer topType) {
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        forumCommentService.changeTopType(userDto.getUserId(), commentId, topType);
        return getSuccessResponseVO(null);
    }

    /**
     * 发布评论
     * @param session
     * @param articleId
     * @param pCommentId
     * @param content
     * @param replyUserId
     * @param image
     * @return
     */
    @RequestMapping("/postComment")
    @GlobalInterceptor(checkLogin = true, checkParams = true, frequencyType = UserOperFrequencyTypeEnum.POST_COMMENT)
    public ResponseVO changeTopType(HttpSession session,
                                    @VerifyParam(required = true) String articleId,
                                    @VerifyParam(required = true) Integer pCommentId,
                                    @VerifyParam(min = 5, max = 800) String content,
                                    String replyUserId,
                                    MultipartFile image) {

        // 是否关闭评论， 如果关闭评论，直接报错
        if (!SysCacheUtils.getSysSetting().getCommentSetting().getCommentOpen()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        // 如果没有图片，并且没有评论内容 报错
        if (image == null && StringTools.isEmpty(content)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        // 对评论类容进行转译 防止xss攻击
        content = StringTools.escapeHtml(content);
        ForumComment comment = new ForumComment();

        comment.setUserId(userDto.getUserId());
        comment.setNickName(userDto.getNickName());
        comment.setUserIpAddress(userDto.getProvince());

        comment.setpCommentId(pCommentId);
        comment.setArticleId(articleId);
        comment.setContent(content);
        comment.setReplyUserId(replyUserId);
        // 默认未置顶
        comment.setTopType(CommentTopTypeEnum.NO_TOP.getType());

        // 插入数据
        forumCommentService.postComment(comment, image);

        if (pCommentId != 0) {
            // 如果是二级评论 查询所有该评论所有二级评论
            ForumCommentQuery commentQuery = new ForumCommentQuery();
            commentQuery.setArticleId(articleId);
            commentQuery.setpCommentId(pCommentId);
            commentQuery.setOrderBy("comment_id asc");
//            commentQuery.setCurrentUserId(userDto.getUserId());
            List<ForumComment> children = forumCommentService.findListByParam(commentQuery);
            return getSuccessResponseVO(children);
        }
        // 如果是一级评论 则返回该条评论
        return getSuccessResponseVO(comment);
    }





}
