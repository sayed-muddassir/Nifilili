package com.nifilili.business.service;

import com.nifilili.business.dto.response.BusinessFileUploadResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface BusinessFileService {

    BusinessFileUploadResponse uploadFile(MultipartFile file);

    DownloadedBusinessFile getFile(String filePath);

    record DownloadedBusinessFile(Resource resource, String fileName, String contentType, long size) {
    }
}
