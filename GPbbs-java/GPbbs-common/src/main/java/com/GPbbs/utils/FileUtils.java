package com.GPbbs.utils;

import com.GPbbs.entity.config.AppConfig;
import com.GPbbs.entity.constants.Constants;
import com.GPbbs.entity.dto.FileUploadDto;
import com.GPbbs.entity.enums.DateTimePatternEnum;
import com.GPbbs.entity.enums.FileUploadTypeEnum;
import com.GPbbs.exception.BusinessException;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;

@Component
public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    @Resource
    private AppConfig appConfig;

    @Resource
    private ImageUtils imageUtils;


    public FileUploadDto uploadFile2Local(MultipartFile file, FileUploadTypeEnum uploadTypeEnum, String folder) {
        // 返回的是文件路径 文件名称（上传文件）
        try {
            FileUploadDto uploadDto = new FileUploadDto();
            String originalFilename = file.getOriginalFilename();
            String fileSuffix = StringTools.getFileSuffix(originalFilename);
            if (originalFilename.length() > Constants.LENGTH_200) {
                // 如果名字太长 进行限制
                originalFilename = StringTools.getFileName(originalFilename).substring(0, Constants.LENGTH_190) + fileSuffix;
            }
            if (!ArrayUtils.contains(uploadTypeEnum.getSuffixArray(), fileSuffix)) {
                // 判断文件类型是狗符合上传条件
                throw new BusinessException("文件类型不正确");
            }
            String month = DateUtil.format(new Date(), DateTimePatternEnum.YYYYMM.getPattern());
            // 拿到共有文件路径
            String baseFolder = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
            // 定义存储文件具体路径（目标路径）
            File targetFileFolder = new File(baseFolder + folder + month + "/");
            // 重命名文件名
            String fileName = StringTools.getRandomString(Constants.LENGTH_15) + fileSuffix;
            File targetFile = new File(targetFileFolder.getPath() + "/" + fileName);
            // 简化代码
            // File targetFile = new File(baseFolder + folder + month + "/", fileName);
            String localPath = month + "/" + fileName;

            if (uploadTypeEnum == FileUploadTypeEnum.AVATAR) {
                // 如果是头像上传
                targetFileFolder = new File(baseFolder + Constants.FILE_FOLDER_AVATAR_NAME);
                targetFile = new File(targetFileFolder.getPath() + "/" + folder + Constants.AVATAR_SUFFIX);
                localPath = folder + Constants.AVATAR_SUFFIX;
            }
            if (!targetFileFolder.exists()) {
                // 如果路径不存在
                targetFileFolder.mkdirs();
            }
            // 上传文件
            file.transferTo(targetFile);
            // 压缩图片
            if (uploadTypeEnum == FileUploadTypeEnum.COMMENT_IMAGE) {
                String thumbnailName = targetFile.getName( ).replace ( ".", "_.");
                File thumbnail = new File( targetFile.getParent () + "/" + thumbnailName);
                Boolean thumbnailCreated = imageUtils.createThumbnail(targetFile, Constants.LENGTH_200, Constants.LENGTH_200, thumbnail);
                if (!thumbnailCreated){
                    org.apache.commons.io.FileUtils.copyFile(targetFile,thumbnail);}

            } else if (uploadTypeEnum == FileUploadTypeEnum.AVATAR || uploadTypeEnum == FileUploadTypeEnum.ARTICLE_COVER) {
                imageUtils.createThumbnail(targetFile, Constants.LENGTH_200, Constants.LENGTH_200, targetFile);
            }
            uploadDto.setLocalPath(localPath);
            uploadDto.setOriginalFilename(originalFilename);
            return uploadDto;
        } catch (BusinessException e) {
            logger.error("文件上传失败", e);
            throw e;
        } catch (Exception e) {
            logger.error("文件上传失败", e);
            throw new BusinessException("文件上传失败");
        }
    }


}
