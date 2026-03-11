package com.floodrescue.report.shared.util;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    /**
     * Upload file lên MinIO
     * 
     * @return objectName (dùng để lưu DB + lấy presigned URL sau này)
     */
    public String uploadFile(MultipartFile file, String folder) {
        try {
            ensureBucketExists();

            String extension = getExtension(file.getOriginalFilename());
            String objectName = folder + "/" + UUID.randomUUID() + extension;

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            log.info("Uploaded file to MinIO: {}", objectName);
            return objectName;

        } catch (Exception e) {
            log.error("Failed to upload file to MinIO", e);
            throw new RuntimeException("Upload file thất bại", e);
        }
    }

    /**
     * Lấy presigned URL có thời hạn 1 giờ để client truy cập ảnh
     */
    public String getPresignedUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .method(Method.GET)
                    .expiry(1, TimeUnit.HOURS)
                    .build());
        } catch (Exception e) {
            log.error("Failed to get presigned URL for: {}", objectName, e);
            throw new RuntimeException("Lấy URL thất bại", e);
        }
    }

    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("Failed to delete file: {}", objectName, e);
        }
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("Created MinIO bucket: {}", bucketName);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains("."))
            return "";
        return filename.substring(filename.lastIndexOf("."));
    }
}
