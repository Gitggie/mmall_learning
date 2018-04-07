package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @Author Wuleijian
 * @Date 2018/4/4 9:57
 * @Description
 */
public interface IFileService {
    String upload(MultipartFile file, String path);
}
