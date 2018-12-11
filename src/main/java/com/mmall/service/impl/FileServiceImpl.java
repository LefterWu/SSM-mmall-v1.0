package com.mmall.service.impl;

import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author: wuleshen
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService {

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Override
    public String upload(MultipartFile file, String path) {
        //1.得到上传文件名
        //获取原始文件名
        String fileName = file.getOriginalFilename();
        //获取扩展名
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".") + 1);
        //为了避免上传文件名重复，重命名上传文件名
        String uploadFileName = UUID.randomUUID().toString() + "." + fileExtensionName;

        logger.info("开始上传文件,上传文件的文件名:{}, 上传路径:{}, 新文件名:{}",fileName, path, uploadFileName);

        //2.创建上传文件
        //创建本地目录
        File fileDir = new File(path);
        if (!fileDir.exists()) {
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        //创建本地文件
        File targetFile = new File(path, uploadFileName);
        //执行本地上传
        boolean isUploaded = false;
        try {
            //3.文件上传到本地
            file.transferTo(targetFile);

            //4.将targetFile上传到ftp服务器
            List<File> targetFileList = new ArrayList<>();
            targetFileList.add(targetFile);
            isUploaded = FTPUtil.uploadFile(targetFileList);

        } catch (IOException e) {
            logger.error("上传文件异常", e);
            return null;
        } finally {
            //上传完成后，删除本地upload下的文件
            targetFile.delete();
        }
        return isUploaded ? targetFile.getName() : null;
    }
}
