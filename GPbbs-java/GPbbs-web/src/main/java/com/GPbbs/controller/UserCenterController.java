package com.GPbbs.controller;

import com.GPbbs.annotation.GlobalInterceptor;
import com.GPbbs.annotation.VerifyParam;
import com.GPbbs.controller.base.ABaseController;
import com.GPbbs.entity.dto.SessionWebUserDto;
import com.GPbbs.entity.dto.UserMessageCountDto;
import com.GPbbs.entity.enums.ArticleStatusEnum;
import com.GPbbs.entity.enums.MessageTypeEnum;
import com.GPbbs.entity.enums.ResponseCodeEnum;
import com.GPbbs.entity.enums.UserStatusEnum;
import com.GPbbs.entity.po.ForumArticle;
import com.GPbbs.entity.po.UserInfo;
import com.GPbbs.entity.query.ForumArticleQuery;
import com.GPbbs.entity.query.LikeRecordQuery;
import com.GPbbs.entity.query.UserMessageQuery;
import com.GPbbs.entity.query.UserPointsRecordQuery;
import com.GPbbs.entity.vo.PaginationResultVO;
import com.GPbbs.entity.vo.ResponseVO;
import com.GPbbs.entity.vo.web.ForumArticleVO;
import com.GPbbs.entity.vo.web.UserInfoVO;
import com.GPbbs.entity.vo.web.UserMessageVO;
import com.GPbbs.exception.BusinessException;
import com.GPbbs.service.*;
import com.GPbbs.utils.CopyTools;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@RestController("userCenterController")
@RequestMapping("/ucenter")
public class UserCenterController extends ABaseController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private ForumArticleService forumArticleService;

    @Resource
    private UserMessageService userMessageService;

    @Resource
    private LikeRecordService likeRecordService;

    @Resource
    private UserPointsRecordService userIntegralRecordService;

    /**
     * 获取用户能够暴露的信息
     * @param userId
     * @return
     */
    @RequestMapping("/getUserInfo")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO getUserInfo(@VerifyParam(required = true) String userId) {
        UserInfo userInfo = userInfoService.getUserInfoByUserId(userId);
        if (null == userInfo || UserStatusEnum.DISABLE.getStatus().equals(userInfo.getStatus())) {
            // 如果用户不存在 或者用户被禁用了
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
        // 查询用户文章
        ForumArticleQuery articleQuery = new ForumArticleQuery();
        articleQuery.setUserId(userId);
        articleQuery.setStatus(ArticleStatusEnum.AUDIT.getStatus());
        Integer postCount = forumArticleService.findCountByParam(articleQuery);
        // 拿到部分能暴露的用户信息
        UserInfoVO userInfoVO = CopyTools.copy(userInfo, UserInfoVO.class);
        userInfoVO.setPostCount(postCount);

        // 拿到获赞数
        LikeRecordQuery recordQuery = new LikeRecordQuery();
        recordQuery.setAuthorUserId(userId);
        Integer likeCount = likeRecordService.findCountByParam(recordQuery);
        userInfoVO.setLikeCount(likeCount);
        userInfoVO.setCurrentPoints(userInfo.getCurrentPoints());
        return getSuccessResponseVO(userInfoVO);
    }

    /**
     * 获取用户发帖
     */
     @RequestMapping("/loadUserArticle")
     @GlobalInterceptor(checkParams = true)
     public ResponseVO loadUserArticle(HttpSession session,
                                       @VerifyParam(required = true) String userId,
                                       @VerifyParam(required = true) Integer type,
                                       Integer pageNo) {
         UserInfo userInfo = userInfoService.getUserInfoByUserId(userId);
         if (null == userInfo || UserStatusEnum.DISABLE.getStatus().equals(userInfo.getStatus())) {
             // 如果用户不存在 或者用户被禁用了
             throw new BusinessException(ResponseCodeEnum.CODE_404);
         }
         ForumArticleQuery articleQuery = new ForumArticleQuery();
         // 发布时间，倒序排
         articleQuery.setOrderBy("post_time desc");
         articleQuery.setPageNo(pageNo);
         if (type == 0) {
             // 发布文章
             articleQuery.setUserId(userId);
         } else if (type == 1) {
             // 评论文章
             articleQuery.setCommentUserId(userId);
         } else if (type == 2) {
             // 点赞文章
             articleQuery.setLikeUserId(userId);
         }

         // 当前用户展示待审核
         SessionWebUserDto userDto = getUserInfoFromSession(session);
         if (userDto != null) {
             // 登录 ---- 用于用户本人可以看待审核的文章
             articleQuery.setCurrentUserId(userDto.getUserId());
         } else {
             articleQuery.setStatus(ArticleStatusEnum.AUDIT.getStatus());
         }
         PaginationResultVO<ForumArticle> result = forumArticleService.findListByPage(articleQuery);

         // 将分页对象转换为所需要的对象
         return getSuccessResponseVO(convert2PaginationVO(result, ForumArticleVO.class));
     }

    /**
     * 更新用户信息
     */
    @RequestMapping("/updateUserInfo")
    @GlobalInterceptor(checkParams = true, checkLogin = true)
    public ResponseVO updateUserInfo(HttpSession session,
                                     String nickName,
                                     Integer sex,
                                     @VerifyParam(max = 100) String personDescription,
                                     MultipartFile avatar) {
        SessionWebUserDto userDto = getUserInfoFromSession(session);

        UserInfo userInfo = new UserInfo();
        userInfo.setNickName(nickName);
        userInfo.setUserId(userDto.getUserId());
        userInfo.setSex(sex);
        userInfo.setPersonDescription(personDescription);
        userInfoService.updateUserInfo(userInfo, avatar);
        return getSuccessResponseVO(null);
    }

    /**
     * 获取用户积分信息
     */
    @RequestMapping("/loadUserPointsRecord")
    @GlobalInterceptor(checkParams = true, checkLogin = true)
    public ResponseVO loadUserIntegralRecord(HttpSession session,
                                             Integer pageNo,
                                             String createTimeStart,
                                             String createTimeEnd) {

        UserPointsRecordQuery recordQuery = new UserPointsRecordQuery();
        recordQuery.setUserId(getUserInfoFromSession(session).getUserId());
        recordQuery.setPageNo(pageNo);
        recordQuery.setCreateTimeStart(createTimeStart);
        recordQuery.setCreateTimeEnd(createTimeEnd);
        // 根据id倒序查
        recordQuery.setOrderBy("record_id desc");
        PaginationResultVO resultVO = userIntegralRecordService.findListByPage(recordQuery);
        return getSuccessResponseVO(resultVO);
    }

    /**
     * 获取消息数
     * @param session
     * @return
     */
    @RequestMapping("/getMessageCount")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getMessageCount(HttpSession session) {
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        if (null == userDto) {
            return getSuccessResponseVO(new UserMessageCountDto());
        }
        return getSuccessResponseVO(userMessageService.getUserMessageCount(userDto.getUserId()));
    }

    /**
     * 消息列表
     * @param session
     * @param code 消息类型
     * @param pageNo 分页
     * @return
     */
    @RequestMapping("/loadMessageList")
    @GlobalInterceptor(checkLogin = true, checkParams = true)
    public ResponseVO loadMessageList(HttpSession session,
                                      @VerifyParam(required = true) String code,
                                      Integer pageNo) {

        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByCode(code);
        if (null == messageTypeEnum) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        UserMessageQuery userMessageQuery = new UserMessageQuery();
        userMessageQuery.setPageNo(pageNo);
        userMessageQuery.setReceivedUserId(userDto.getUserId());
        userMessageQuery.setMessageType(messageTypeEnum.getType());
        // 根据消息id倒序排
        userMessageQuery.setOrderBy("message_id desc");
        PaginationResultVO result = userMessageService.findListByPage(userMessageQuery);

        if (pageNo == null || pageNo == 1) {
            // 第一页
            userMessageService.readMessageByType(userDto.getUserId(), messageTypeEnum.getType());

        }
        PaginationResultVO resultVO = convert2PaginationVO(result, UserMessageVO.class);
        return getSuccessResponseVO(resultVO);
    }





}
