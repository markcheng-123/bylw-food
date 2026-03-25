package com.bylw.foodforum.service;

import com.bylw.foodforum.vo.upload.UploadFileVO;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {

    UploadFileVO uploadImage(MultipartFile file);
}
