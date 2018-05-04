package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @Author Wuleijian
 * @Date 2018/4/4 9:58
 * @Description
 */

@Service("iFileService")
@Slf4j
public class FileServiceImpl implements IFileService {


    //multipartFile 是 string 类型，代表 HTML 中 form data 方式上传的文件，包含二进制数据 + 文件名称。
    //声明：上传的文件，上传到哪里
    public String upload(MultipartFile file, String path) {
        //得到上传时的文件名
        String fileName = file.getOriginalFilename();
        //得到文件扩展名
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".") + 1);
        //得到上传后的文件名
        String uploadFileName = UUID.randomUUID().toString() + "." + fileExtensionName;
        log.info("开始上传文件,上传文件的文件名:{},上传的路径:{},新文件名:{}", fileName, path, uploadFileName);

        //在路径path下新建File类的对象fileDir
        File fileDir = new File(path);
        //todo 不懂了，fileDir又不是文件，为什么要判断存在性
        if (!fileDir.exists()) {
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        //在路径path下新建File类对象targetFile，内存地址指向uploadFileName
        //targetFile在内存中的栈区
        File targetFile = new File(path, uploadFileName);

        try {
            //transferTo() 方法，是 springmvc 封装的方法，用于图片上传时，把内存中图片写入磁盘
            file.transferTo(targetFile);
            //文件已经上传成功了

            //用list存放图片文件，你可选多个图片
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            //已经上传到ftp服务器上

            //todo 为什么要删？你已经上传到服务器了，本地的就可以删了
            targetFile.delete();
        } catch (IOException e) {
            log.error("上传文件异常", e);
            return null;
        }
        //A:abc.jpg
        //B:abc.jpg
        return targetFile.getName();
    }
}
