package com.GPbbs.controller;

import com.GPbbs.annotation.GlobalInterceptor;
import com.GPbbs.annotation.VerifyParam;
import com.GPbbs.controller.base.ABaseController;
import com.GPbbs.entity.config.WebConfig;
import com.GPbbs.entity.constants.Constants;
import com.GPbbs.entity.dto.SessionWebUserDto;
import com.GPbbs.entity.enums.*;
import com.GPbbs.entity.po.*;
import com.GPbbs.entity.query.ForumArticleAttachmentQuery;
import com.GPbbs.entity.query.ForumArticleQuery;
import com.GPbbs.entity.vo.PaginationResultVO;
import com.GPbbs.entity.vo.ResponseVO;
import com.GPbbs.entity.vo.web.*;
import com.GPbbs.exception.BusinessException;
import com.GPbbs.service.*;
import com.GPbbs.utils.CopyTools;
import com.GPbbs.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/forum")
public class ForumArticleController extends ABaseController {

    private static final Logger logger = LoggerFactory.getLogger(ForumArticleController.class);

    @Resource
    private ForumArticleService forumArticleService;

    @Resource
    private ForumArticleAttachmentService forumArticleAttachmentService;

    @Resource
    private LikeRecordService likeRecordService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private ForumArticleAttachmentDownloadService forumArticleAttachmentDownloadService;

    @Resource
    private WebConfig webConfig;


    @Resource
    private ForumBoardService forumBoardService;




    /**
     * 获取板块信息
     * @param session
     * @param boardId 板块ID
     * @param pBoardId 父级板块ID
     * @param orderType 0 ：热榜 1 ： 发布时间 2 ： 最新发布
     * @param pageNo  页码
     * @return
     */

    @RequestMapping("/loadArticle")
    public ResponseVO loadArticle(HttpSession session, Integer boardId, Integer pBoardId, Integer orderType, Integer pageNo) {
        ForumArticleQuery articleQuery = new ForumArticleQuery();
        articleQuery.setBoardId(boardId == null || boardId == 0 ? null : boardId);
        // 设置父板块id
        articleQuery.setpBoardId(pBoardId);
        articleQuery.setPageNo(pageNo);

        SessionWebUserDto userDto = getUserInfoFromSession(session);
        if (userDto != null) {
            // 如果登录可以查询本人发布 待审核文章
            articleQuery.setCurrentUserId(userDto.getUserId());
        } else {
            // 未登录 只可查询已发布审核文章
            // 设置过滤条件 status 状态为 1 ：表示已经审核 0 ：表示待审核 -1 ：表示已删除
            articleQuery.setStatus(ArticleStatusEnum.AUDIT.getStatus());
        }

        ArticleOrderTypeEnum orderTypeEnum = ArticleOrderTypeEnum.getByType(orderType);
        orderTypeEnum = orderTypeEnum == null ? ArticleOrderTypeEnum.HOT : orderTypeEnum;
        articleQuery.setOrderBy(orderTypeEnum.getOrderSql());

        // 拿到文章列表
        PaginationResultVO resultVO = forumArticleService.findListByPage(articleQuery);
        return getSuccessResponseVO(convert2PaginationVO(resultVO, ForumArticleVO.class));
    }

    /**
     * 获取文章详情
     * @param session
     * @param articleId
     * @return
     */

    @RequestMapping("/getArticleDetail")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO getArticleDetail(HttpSession session,
                                       @VerifyParam(required = true) String articleId) {

        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);

        ForumArticle forumArticle = forumArticleService.readArticle(articleId);

        // 未取到 || （未审核 + 不是浏览者自己发布的文章 + 非管理员） || 已经删除的文章
        if (forumArticle == null
                || (ArticleStatusEnum.NO_AUDIT.getStatus().equals(forumArticle.getStatus()) && (sessionWebUserDto == null || !sessionWebUserDto.getUserId().equals(forumArticle.getUserId()) && !sessionWebUserDto.getAdmin()))
                || ArticleStatusEnum.DEL.getStatus().equals(forumArticle.getStatus())) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }

        // 文章详情页
        FormArticleDetailVO detailVO = new FormArticleDetailVO();
        detailVO.setForumArticle(CopyTools.copy(forumArticle, ForumArticleVO.class));

        // 有附件
        if (forumArticle.getAttachmentType() == 1) {
            ForumArticleAttachmentQuery articleAttachmentQuery = new ForumArticleAttachmentQuery();
            articleAttachmentQuery.setArticleId(forumArticle.getArticleId());
            // 查询附件
            List<ForumArticleAttachment> forumArticleAttachmentList = forumArticleAttachmentService.findListByParam(articleAttachmentQuery);
            if (!forumArticleAttachmentList.isEmpty()) {
                detailVO.setAttachment(CopyTools.copy(forumArticleAttachmentList.get(0), ForumArticleAttachmentVo.class));
            }
        }

        // 是否已经点赞
        if (sessionWebUserDto != null) {
            LikeRecord likeRecord = likeRecordService.getLikeRecordByObjectIdAndUserIdAndOpType(articleId,sessionWebUserDto.getUserId(),OperRecordOpTypeEnum.ARTICLE_LIKE.getType());

//            LikeRecord like = likeRecordService.getUserOperRecordByObjectIdAndUserIdAndOpType(articleId, sessionWebUserDto.getUserId(), OperRecordOpTypeEnum.ARTICLE_LIKE.getType());
            if (likeRecord != null) {
                detailVO.setHaveLike(true);
            }
        }
        return getSuccessResponseVO(detailVO);
    }

    /**
     * 点赞
     * , frequencyType = UserOperFrequencyTypeEnum.DO_LIKE
     */
    @RequestMapping("/doLike")
    @GlobalInterceptor(checkLogin = true, checkParams = true, frequencyType = UserOperFrequencyTypeEnum.DO_LIKE)
    public ResponseVO doLike(HttpSession session, @VerifyParam(required = true) String articleId) {
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        likeRecordService.doLike(articleId, userDto.getUserId(), userDto.getNickName(), OperRecordOpTypeEnum.ARTICLE_LIKE);
        return getSuccessResponseVO(null);
    }


    /**
     * 获取用户下载信息
     * @param session
     * @param fileId
     * @return
     */
    @RequestMapping("/getUserDownloadInfo")
    @GlobalInterceptor(checkLogin = true, checkParams = true)
    public ResponseVO getUserDownloadInfo(HttpSession session, @VerifyParam(required = true) String fileId) {
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        UserInfo userInfo = userInfoService.getUserInfoByUserId(webUserDto.getUserId());
        UserDownloadInfoVO downloadInfoVO = new UserDownloadInfoVO();
        downloadInfoVO.setUserPoints(userInfo.getCurrentPoints());
        // 查看是否下载过
        ForumArticleAttachmentDownload attachmentDownload = forumArticleAttachmentDownloadService.getForumArticleAttachmentDownloadByFileIdAndUserId(fileId, webUserDto.getUserId());
        if(attachmentDownload != null){
            downloadInfoVO.setHaveDownload(true);
        }
        return getSuccessResponseVO(downloadInfoVO);
    }


    /**
     * 下载附件 通过流返回
     * @param session
     * @param request
     * @param response
     * @param fileId
     */
    @RequestMapping("/attachmentDownload")
    @GlobalInterceptor(checkLogin = true, checkParams = true)
    public void attachmentDownload(HttpSession session, HttpServletRequest request, HttpServletResponse response,
                                   @VerifyParam(required = true) String fileId) {
        ForumArticleAttachment attachment = forumArticleAttachmentService.downloadAttachment(fileId, getUserInfoFromSession(session));
        InputStream in = null;
        OutputStream out = null;
        String downloadFileName = attachment.getFileName();
        String filePath = webConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_ATTACHMENT + attachment.getFilePath();
        File file = new File(filePath);
        try {
            in = new FileInputStream(file);
            out = response.getOutputStream();
            response.setContentType("application/x-msdownload; charset=UTF-8");
            // 解决中文文件名乱码问题
            if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0) {//IE浏览器
                downloadFileName = URLEncoder.encode(downloadFileName, "UTF-8");
            } else {
                downloadFileName = new String(downloadFileName.getBytes("UTF-8"), "ISO8859-1");
            }
            response.setHeader("Content-Disposition", "attachment;filename=\"" + downloadFileName + "\"");
            byte[] byteData = new byte[1024];
            int len = 0;
            while ((len = in.read(byteData)) != -1) {
                out.write(byteData, 0, len); // write
            }
            out.flush();
        } catch (Exception e) {
            logger.error("下载异常", e);
            throw new BusinessException("下载失败");
        } finally {
            try {
                if (in != null) {
                    in.close();
                }

            } catch (IOException e) {
                logger.error("IO异常", e);
            }
            try {
                if (out != null) {
                    out.close();
                }

            } catch (IOException e) {
                logger.error("IO异常", e);
            }
        }
    }

    /**
     * 发帖时获取板块信息
     */
    @RequestMapping("/loadBoard4Post")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadBoard4Post(HttpSession session) {
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        Integer postType = null;
        if (!userDto.getAdmin()) {
            postType = 1;
        }
        return getSuccessResponseVO(forumBoardService.getBoardTree(postType));
    }

    /**
     * 发布文章
     * @param session
     * @param cover
     * @param attachment
     * @param points
     * @param title
     * @param pBoardId
     * @param boardId
     * @param summary
     * @param editorType
     * @param content
     * @param markdownContent
     * @return
     */
    @RequestMapping("/postArticle")
    @GlobalInterceptor(checkLogin = true, checkParams = true , frequencyType = UserOperFrequencyTypeEnum.POST_ARTICLE)
    public ResponseVO postArticle(HttpSession session,
                                  MultipartFile cover,
                                  MultipartFile attachment,
                                  Integer points,
                                  @VerifyParam(required = true, max = 150) String title,
                                  @VerifyParam(required = true) Integer pBoardId,
                                  Integer boardId,
                                  @VerifyParam(max = 200) String summary,
                                  @VerifyParam(required = true) Integer editorType,
                                  @VerifyParam(required = true) String content,
                                  String markdownContent) {

        title = StringTools.escapeHtml(title);
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        ForumArticle forumArticle = new ForumArticle();
        forumArticle.setpBoardId(pBoardId);
        forumArticle.setBoardId(boardId);
        forumArticle.setTitle(title);
        forumArticle.setContent(content);

        // 校验编辑器类别
        EditorTypeEnum editorTypeEnum = EditorTypeEnum.getByType(editorType);
        if (null == editorTypeEnum) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (EditorTypeEnum.MARKDOWN.getType().equals(editorType) && StringTools.isEmpty(markdownContent)) {
            // 如果是markdown编辑器，并且编辑内容是空
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        forumArticle.setMarkdownContent(markdownContent);
        forumArticle.setEditorType(editorType);
        forumArticle.setSummary(summary);
        forumArticle.setUserId(userDto.getUserId());
        forumArticle.setNickName(userDto.getNickName());
        forumArticle.setUserIpAddress(userDto.getProvince());

        // 附件信息
        ForumArticleAttachment forumArticleAttachment = new ForumArticleAttachment();
        // 设置附件所需要下载的积分
        forumArticleAttachment.setPoints(points == null ? 0 : points);
        // 传入是否是管理员， 管理员不需要审核
        forumArticleService.postArticle(userDto.getAdmin(), forumArticle, forumArticleAttachment, cover, attachment);
        return getSuccessResponseVO(forumArticle.getArticleId());
    }

    /**
     * 修改文章获取详情
     * @param session
     * @param articleId
     * @return
     */
    @RequestMapping("/articleDetail4Update")
    @GlobalInterceptor(checkLogin = true, checkParams = true)
    public ResponseVO articleDetail4Update(HttpSession session, @VerifyParam(required = true) String articleId) {
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        ForumArticle forumArticle = forumArticleService.getForumArticleByArticleId(articleId);
        if (forumArticle == null || !forumArticle.getUserId().equals(userDto.getUserId())) {
            // 如果没有文章 || 不是文章作者
            throw new BusinessException("文章不存在或你无权编辑该文章");
        }
//        FormArticleUpdateDetailVO detailVO = new FormArticleUpdateDetailVO();
//        detailVO.setForumArticle(forumArticle);

        FormArticleDetailVO detailVO = new FormArticleDetailVO();
        detailVO.setForumArticle(CopyTools.copy(forumArticle,ForumArticleVO.class));

        if (forumArticle.getAttachmentType() == Constants.ONE) {
            // 查看是否有附件
            ForumArticleAttachmentQuery articleAttachmentQuery = new ForumArticleAttachmentQuery();
            articleAttachmentQuery.setArticleId(forumArticle.getArticleId());
            List<ForumArticleAttachment> forumArticleAttachmentList = forumArticleAttachmentService.findListByParam(articleAttachmentQuery);
            if (!forumArticleAttachmentList.isEmpty()) {

                detailVO.setAttachment(CopyTools.copy(forumArticleAttachmentList.get(0), ForumArticleAttachmentVo.class));
            }
        }
        return getSuccessResponseVO(detailVO);
    }

    /**
     * 更新文章
     * @param session
     * @param cover
     * @param attachment
     * @param points
     * @param articleId
     * @param pBoardId
     * @param boardId
     * @param title
     * @param content
     * @param markdownContent
     * @param editorType
     * @param summary
     * @param attachmentType
     * @return
     */
    @RequestMapping("/updateArticle")
    @GlobalInterceptor(checkLogin = true, checkParams = true)
    public ResponseVO updateArticle(HttpSession session,
                                    MultipartFile cover,
                                    MultipartFile attachment,
                                    Integer points,
                                    @VerifyParam(required = true) String articleId,
                                    @VerifyParam(required = true) Integer pBoardId,
                                    Integer boardId,
                                    @VerifyParam(required = true, max = 150) String title,
                                    @VerifyParam(required = true) String content,
                                    String markdownContent,
                                    @VerifyParam(required = true) Integer editorType,
                                    @VerifyParam(max = 200) String summary,
                                    @VerifyParam(required = true) Integer attachmentType) {
        title = StringTools.escapeHtml(title);
        SessionWebUserDto userDto = getUserInfoFromSession(session);

        ForumArticle forumArticle = new ForumArticle();
        forumArticle.setArticleId(articleId);

        forumArticle.setpBoardId(pBoardId);
        forumArticle.setBoardId(boardId);
        forumArticle.setTitle(title);
        forumArticle.setContent(content);
        forumArticle.setMarkdownContent(markdownContent);
        forumArticle.setEditorType(editorType);
        forumArticle.setSummary(summary);
        forumArticle.setUserIpAddress(userDto.getProvince());
        forumArticle.setAttachmentType(attachmentType);
        forumArticle.setUserId(userDto.getUserId());
        //附件信息
        ForumArticleAttachment forumArticleAttachment = new ForumArticleAttachment();
        forumArticleAttachment.setPoints(points == null ? 0 : points);

        forumArticleService.updateArticle(userDto.getAdmin(), forumArticle, forumArticleAttachment, cover, attachment);
        return getSuccessResponseVO(forumArticle.getArticleId());
    }

    /**
     * 搜索
     * @param keyword
     * @return
     */
    @RequestMapping("/search")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO updateArticle(@VerifyParam(required = true, min = 2) String keyword) {
        ForumArticleQuery query = new ForumArticleQuery();
        query.setTitleFuzzy(keyword);
        PaginationResultVO result = forumArticleService.findListByPage(query);
        return getSuccessResponseVO(result);
    }



}
