package com.example.travel.review.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * 파일을 저장하고 접근 가능한 URL을 반환합니다.
     */
    public String storeFile(MultipartFile file, String subDir) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("빈 파일은 저장할 수 없습니다.");
            }

            // 원본 파일명에서 확장자 추출
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            // 고유한 파일명 생성
            String fileName = UUID.randomUUID().toString() + fileExtension;

            // 저장 디렉토리 경로 생성
            Path uploadPath = Paths.get(uploadDir, subDir);

            // 디렉토리가 존재하지 않으면 생성
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 파일 저장
            Path filePath = uploadPath.resolve(fileName);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            // 파일에 접근할 수 있는 URL 경로 반환
            return subDir + "/" + fileName;

        } catch (IOException ex) {
            log.error("파일 저장 중 오류 발생: ", ex);
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", ex);
        }
    }

    /**
     * 파일 삭제
     */
    public boolean deleteFile(String fileUrl) {
        try {
            Path filePath = Paths.get(uploadDir, fileUrl);
            return Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            log.error("파일 삭제 중 오류 발생: ", ex);
            return false;
        }
    }
}