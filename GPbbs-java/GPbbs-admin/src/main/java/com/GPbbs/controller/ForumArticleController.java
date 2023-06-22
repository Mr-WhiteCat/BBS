package com.GPbbs.controller;

import com.GPbbs.annotation.GlobalInterceptor;
import com.GPbbs.annotation.VerifyParam;
import com.GPbbs.controller.base.ABaseController;
import com.GPbbs.entity.config.AdminConfig;
import com.GPbbs.entity.config.WebConfig;
import com.GPbbs.entity.constants.Constants;
import com.GPbbs.entity.dto.SessionWebUserDto;
import com.GPbbs.entity.enums.*;
import com.GPbbs.entity.po.*;
import com.GPbbs.entity.query.ForumArticleAttachmentQuery;
import com.GPbbs.entity.query.ForumArticleQuery;
import com.GPbbs.entity.query.ForumCommentQuery;
import com.GPbbs.entity.vo.PaginationResultVO;
import com.GPbbs.entity.vo.ResponseVO;
import com.GPbbs.entity.vo.web.FormArticleDetailVO;
import com.GPbbs.entity.vo.web.ForumArticleAttachmentVo;
import com.GPbbs.entity.vo.web.ForumArticleVO;
import com.GPbbs.entity.vo.web.UserDownloadInfoVO;
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
import java.util.List;

@RestController
@RequestMapping("/forum")
public class ForumArticleController extends ABaseController {

    private static final Logger logger = LoggerFactory.getLogger(ForumArticleController.class);

    @Resource
    private ForumArticleService forumArticleService;

    @Resource
    private ForumArticleAttachmentService forumArticleAttachmentService;


    @Resource
    private ForumArticleAttachmentDownloadService forumArticleAttachmentDownloadService;

    @Resource
    private AdminConfig adminConfig;

    @Resource
    private ForumCommentService forumCommentService;

    /**
     * 加载帖子
     * @param articleQuery
     * @return
     */
    @RequestMapping("/loadArticle")
    public ResponseVO loadArticle(ForumArticleQuery articleQuery) {
        articleQuery.setOrderBy("post_time desc");
        return getSuccessResponseVO(forumArticleService.findListByPage(articleQuery));
    }

    /**
     * 删除帖子
     * @param articleIds
     * @return
     */
    @RequestMapping("/delArticle")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO delArticle(@VerifyParam(required = true) String articleIds) {
        forumArticleService.delArticle(articleIds);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/updateBoard")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO updateBoard(@VerifyParam(required = true) String articleId,
                                  @VerifyParam(required = true) Integer pBoardId,
                                  Integer boardId) {

        boardId = boardId == null ? 0 : boardId;
        forumArticleService.updateBoard(articleId, pBoardId, boardId);
        return getSuccessResponseVO(null);
    }

    /**
     * 查看附件
     * @param articleId
     * @return
     */
    @RequestMapping("/getAttachment")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO getAttachment(@VerifyParam(required = true) String articleId) {
        ForumArticleAttachmentQuery articleAttachmentQuery = new ForumArticleAttachmentQuery();
        articleAttachmentQuery.setArticleId(articleId);
        List<ForumArticleAttachment> attachmentList = forumArticleAttachmentService.findListByParam(articleAttachmentQuery);
        if (attachmentList.isEmpty()) {
            throw new BusinessException("附件不存在");
        }
        return getSuccessResponseVO(attachmentList.get(0));
    }

    /**
     * 下载附件 通过流返回
     * @param request
     * @param response
     * @param fileId
     */
    @RequestMapping("/attachmentDownload")
    @GlobalInterceptor(checkParams = true)
    public void attachmentDownload( HttpServletRequest request, HttpServletResponse response,
                                   @VerifyParam(required = true) String fileId) {

        ForumArticleAttachment attachment = forumArticleAttachmentService.getForumArticleAttachmentByFileId(fileId);
//        System.out.printf("+++++++++++"+attachment+"+++++++++++++++");
        InputStream in = null;
        OutputStream out = null;
        String downloadFileName = attachment.getFileName();
        String filePath = adminConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_ATTACHMENT + attachment.getFilePath();
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
     * 文章置顶
     * @param topType
     * @param articleId
     * @return
     */
    @RequestMapping("/topArticle")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO topArticle(@VerifyParam(required = true) Integer topType,
                                 @VerifyParam(required = true) String articleId) {
        ForumArticle forumArticle = new ForumArticle();
        forumArticle.setTopType(topType);
        forumArticleService.updateForumArticleByArticleId(forumArticle, articleId);
        return getSuccessResponseVO(null);
    }

    /**
     * 审核文章
     * @param articleIds
     * @return
     */
    @RequestMapping("/delCommenauditArticle")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO auditArticle(@VerifyParam(required = true) String articleIds) {
        forumArticleService.auditArticle(articleIds);
        return getSuccessResponseVO(null);
    }

    /**
     * 获取所有评论
     * @param commentQuery
     * @return
     */
    @RequestMapping("/loadComment")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO loadComment(ForumCommentQuery commentQuery) {
        commentQuery.setLoadChildren(true);
        commentQuery.setOrderBy("post_time desc");
        return getSuccessResponseVO(forumCommentService.findListByPage(commentQuery));
    }

    /**
     * 获取文章评论
     * @param commentQuery
     * @return
     */
    @RequestMapping("/loadComment4Article")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO loadComment4Article(ForumCommentQuery commentQuery) {
        commentQuery.setLoadChildren(true);
        commentQuery.setOrderBy("post_time desc");
        commentQuery.setLoadChildren(true);
        commentQuery.setpCommentId(0);
        return getSuccessResponseVO(forumCommentService.findListByParam(commentQuery));
    }

    /**
     * 删除评论
     * @param commentIds
     * @return
     */
    @RequestMapping("/delComment")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO delComment(@VerifyParam(required = true) String commentIds) {
        forumCommentService.delComment(commentIds);
        return getSuccessResponseVO(null);
    }

    /**
     * 审核评论
     * @param commentIds
     * @return
     */
    @RequestMapping("/auditComment")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO auditComment(@VerifyParam(required = true) String commentIds) {
        forumCommentService.auditComment(commentIds);
        return getSuccessResponseVO(null);
    }

}
