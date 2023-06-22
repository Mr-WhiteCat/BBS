package com.GPbbs.controller;

import com.GPbbs.annotation.GlobalInterceptor;
import com.GPbbs.annotation.VerifyParam;
import com.GPbbs.controller.base.ABaseController;
import com.GPbbs.entity.config.WebConfig;
import com.GPbbs.entity.constants.Constants;
import com.GPbbs.entity.enums.ResponseCodeEnum;
import com.GPbbs.entity.enums.UserOperFrequencyTypeEnum;
import com.GPbbs.entity.vo.ResponseVO;
import com.GPbbs.exception.BusinessException;
import com.GPbbs.utils.StringTools;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/file")
public class FileController extends ABaseController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_VALUE = "application/json;charset=UTF-8";


    @Resource
    private WebConfig webConfig;



    /**
     * 获取图片
     */
    @RequestMapping("/getImage/{imageFolder}/{imageName}")
    public void getImage(HttpServletResponse response,
                         @PathVariable("imageFolder") String imageFolder,
                         @PathVariable("imageName") String imageName) {
        readImage(response, imageFolder, imageName);
    }

    /**
     * 获取图片真正方法 读取头像也使用此方法
     * @param response
     * @param imageFolder
     * @param imageName
     */
    private void readImage(HttpServletResponse response, String imageFolder, String imageName) {
        ServletOutputStream sos = null;
        FileInputStream in = null;
        ByteArrayOutputStream baos = null;
        try {
            if (StringTools.isEmpty(imageFolder) || StringUtils.isBlank(imageName)) {
                return;
            }
            // 拿到后缀
            String imageSuffix = StringTools.getFileSuffix(imageName);
            String filePath = webConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_IMAGE + imageFolder + "/" + imageName;

            if (Constants.FILE_FOLDER_TEMP_2.equals(imageFolder) || imageFolder.contains(Constants.FILE_FOLDER_AVATAR_NAME)) {
                // 如果是temp中文件 或者 在avatar中文件
                filePath = webConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + imageFolder + "/" + imageName;
            }
            File file = new File(filePath);
            if (!file.exists()) {
                // 如果文件不存在
                return;
            }
            imageSuffix = imageSuffix.replace(".", "");
            if (!Constants.FILE_FOLDER_AVATAR_NAME.equals(imageFolder)) {
                // 不是头像就设置缓存
                response.setHeader("Cache-Control", "max-age=2592000");
            }
            response.setContentType("image/" + imageSuffix);
            in = new FileInputStream(file);
            sos = response.getOutputStream();
            baos = new ByteArrayOutputStream();
            int ch = 0;
            while (-1 != (ch = in.read())) {
                baos.write(ch);
            }
            sos.write(baos.toByteArray());
        } catch (Exception e) {
            logger.error("读取图片异常", e);
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (sos != null) {
                try {
                    sos.close();
                } catch (IOException e) {
                    logger.error("IO异常", e);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error("IO异常", e);
                }
            }
        }
    }

    /**
     * 获取头像
     * @param response
     * @param userId
     */
    @RequestMapping("/getAvatar/{userId}")
    public void getAvatar(HttpServletResponse response,
                          @VerifyParam(required = true)
                          @PathVariable("userId") String userId) {

        String avatarFolderName = Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_AVATAR_NAME;
        //
        String avatarPath = webConfig.getProjectFolder() + avatarFolderName + userId + Constants.AVATAR_SUFFIX;
        File avatarFoleder = new File(avatarFolderName);
        if (!avatarFoleder.exists()) {
            // 如果文件不存在
            avatarFoleder.mkdirs();
        }

        // 读取头像
        File file = new File(avatarPath);
        // 头像文件名夹名
        String imageFolder = Constants.FILE_FOLDER_AVATAR_NAME;
        // 头像图片名称
        String imageName = userId + Constants.AVATAR_SUFFIX;
        if (!file.exists()) {
            imageName = Constants.AVATAR_DEFUALT;
        }
        readImage(response, imageFolder, imageName);
    }


}
