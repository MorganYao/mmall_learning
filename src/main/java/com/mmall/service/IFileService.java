package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author : pengyao
 * @Date: 2018/6/6 21: 44
 */
public interface IFileService {
    String upload(MultipartFile file,String path);
}
