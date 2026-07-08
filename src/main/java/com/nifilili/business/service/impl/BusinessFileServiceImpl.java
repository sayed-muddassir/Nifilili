package com.nifilili.business.service.impl;

import com.nifilili.business.dto.response.BusinessFileUploadResponse;
import com.nifilili.business.service.BusinessFileService;
import com.nifilili.business.service.storage.FileStorageProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessFileServiceImpl implements BusinessFileService {

    private final FileStorageProvider storageProvider;

    @Override
    public BusinessFileUploadResponse uploadFile(MultipartFile file) {
        log.debug("Received file upload request: {}", file != null ? file.getOriginalFilename() : "null");
        
        FileStorageProvider.UploadedFileInfo uploadedFileInfo = storageProvider.uploadFile(file);
        
        BusinessFileUploadResponse response = new BusinessFileUploadResponse(
            uploadedFileInfo.relativePath(),
            uploadedFileInfo.fileName(),
            uploadedFileInfo.contentType(),
            uploadedFileInfo.size(),
            uploadedFileInfo.originalFileName(),
            uploadedFileInfo.version(),
            uploadedFileInfo.contentHash()
        );
        
        log.info("File upload successful: {}", response.getFileName());
        return response;
    }

    @Override
    public DownloadedBusinessFile getFile(String filePath) {
        log.debug("Received file download request: {}", filePath);
        DownloadedBusinessFile file = storageProvider.downloadFile(filePath);
        log.info("File download ready: {}", file.fileName());
        return file;
    }
}
