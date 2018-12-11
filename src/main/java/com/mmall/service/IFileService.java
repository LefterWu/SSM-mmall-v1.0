package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @Description: 处理文件的服务
 * @author: wuleshen
 */
public interface IFileService {

    /**
     * 上传文件
     * @param file 文件
     * @param path 上传路径
     * @return 上传文件的文件名（uuid重命名后的）
     */
    String upload(MultipartFile file, String path);
}
