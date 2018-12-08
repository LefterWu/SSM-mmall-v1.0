package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @Description: 处理文件的服务
 * @author: wuleshen
 */
public interface IFileService {

    String upload(MultipartFile file, String path);
}
