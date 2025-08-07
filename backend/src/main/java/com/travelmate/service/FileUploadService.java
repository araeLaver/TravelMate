package com.travelmate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileUploadService {
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    @Value("${app.upload.max-size:10485760}") // 10MB default
    private Long maxFileSize;
    
    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("파일이 비어있습니다.");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("파일 크기가 제한을 초과했습니다.");
        }
        
        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        
        if (!isAllowedExtension(fileExtension)) {
            throw new RuntimeException("허용되지 않은 파일 형식입니다.");
        }
        
        // 고유한 파일명 생성
        String uniqueFileName = UUID.randomUUID().toString() + "." + fileExtension;
        
        try {
            // 업로드 디렉토리 생성
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 파일 저장
            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("파일 업로드 성공: {}", uniqueFileName);
            
            // 실제 서비스에서는 CDN URL 반환
            return "/uploads/" + uniqueFileName;
            
        } catch (IOException e) {
            log.error("파일 업로드 실패: {}", e.getMessage());
            throw new RuntimeException("파일 업로드에 실패했습니다.", e);
        }
    }
    
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        
        try {
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("파일 삭제 성공: {}", fileName);
            }
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", e.getMessage());
        }
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
    
    private boolean isAllowedExtension(String extension) {
        // 허용된 확장자 목록
        String[] allowedExtensions = {"jpg", "jpeg", "png", "gif", "webp", "bmp"};
        
        for (String allowed : allowedExtensions) {
            if (allowed.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        
        return false;
    }
}