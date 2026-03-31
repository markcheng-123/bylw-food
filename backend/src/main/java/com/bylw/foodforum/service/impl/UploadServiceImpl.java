package com.bylw.foodforum.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.bylw.foodforum.common.exception.BusinessException;
import com.bylw.foodforum.service.UploadService;
import com.bylw.foodforum.vo.upload.UploadFileVO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadServiceImpl implements UploadService {

    private static final Logger log = LoggerFactory.getLogger(UploadServiceImpl.class);
    private static final long MAX_IMAGE_SIZE = 25L * 1024L * 1024L;

    @Value("${app.upload-path}")
    private String uploadPath;

    @Value("${app.upload.oss-enabled:false}")
    private boolean ossEnabled;

    @Value("${app.upload.endpoint:}")
    private String ossEndpoint;

    @Value("${app.upload.bucket:}")
    private String ossBucket;

    @Value("${app.upload.access-key-id:}")
    private String ossAccessKeyId;

    @Value("${app.upload.access-key-secret:}")
    private String ossAccessKeySecret;

    @Value("${app.upload.dir-prefix:bylw/}")
    private String ossDirPrefix;

    @Value("${app.upload.public-base-url:}")
    private String ossPublicBaseUrl;

    @Override
    public UploadFileVO uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请先选择图片");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BusinessException("图片大小不能超过25MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("仅支持上传图片文件");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);
        String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
            + "-"
            + UUID.randomUUID().toString().replace("-", "")
            + (StringUtils.hasText(extension) ? "." + extension : ".jpg");

        UploadFileVO uploadFileVO = new UploadFileVO();
        uploadFileVO.setFileName(fileName);

        if (ossEnabled && isOssConfigComplete()) {
            try {
                uploadFileVO.setUrl(uploadToOss(file, fileName));
                return uploadFileVO;
            } catch (BusinessException exception) {
                log.warn("OSS upload failed, fallback to local storage. reason={}", exception.getMessage());
            }
        }

        if (ossEnabled && !isOssConfigComplete()) {
            log.warn("OSS enabled but config incomplete, fallback to local storage.");
        }

        uploadFileVO.setUrl(uploadToLocal(file, fileName));
        return uploadFileVO;
    }

    private String uploadToLocal(MultipartFile file, String fileName) {
        File directory = new File(uploadPath);
        if (!directory.exists()) {
            try {
                Files.createDirectories(directory.toPath());
            } catch (IOException exception) {
                throw new BusinessException("创建上传目录失败");
            }
        }

        File targetFile = new File(directory, fileName);
        try {
            file.transferTo(targetFile);
        } catch (IOException exception) {
            throw new BusinessException("上传图片失败");
        }
        return "/uploads/" + fileName;
    }

    private String uploadToOss(MultipartFile file, String fileName) {
        if (!isOssConfigComplete()) {
            throw new BusinessException("OSS配置不完整，请检查环境变量");
        }

        String objectKey = buildObjectKey(fileName);
        String normalizedEndpoint = normalizeEndpoint(ossEndpoint);
        String endpointHost = extractEndpointHost(normalizedEndpoint);
        OSS ossClient = new OSSClientBuilder().build(normalizedEndpoint, ossAccessKeyId, ossAccessKeySecret);

        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            PutObjectRequest request = new PutObjectRequest(ossBucket, objectKey, inputStream, metadata);
            ossClient.putObject(request);
        } catch (Exception exception) {
            throw new BusinessException("上传图片到OSS失败: " + exception.getMessage());
        } finally {
            ossClient.shutdown();
        }

        String publicBase = StringUtils.hasText(ossPublicBaseUrl)
            ? trimEndSlash(ossPublicBaseUrl)
            : ("https://" + ossBucket + "." + endpointHost);
        return publicBase + "/" + objectKey;
    }

    private String buildObjectKey(String fileName) {
        String prefix = StringUtils.hasText(ossDirPrefix) ? ossDirPrefix.trim() : "bylw/";
        if (!prefix.endsWith("/")) {
            prefix = prefix + "/";
        }
        return prefix + fileName;
    }

    private String trimEndSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String normalizeEndpoint(String endpoint) {
        String trimmed = endpoint == null ? "" : endpoint.trim();
        if (!StringUtils.hasText(trimmed)) {
            return "";
        }
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            trimmed = "https://" + trimmed;
        }
        return trimEndSlash(trimmed);
    }

    private String extractEndpointHost(String endpoint) {
        try {
            URI uri = URI.create(endpoint);
            if (StringUtils.hasText(uri.getHost())) {
                return uri.getHost();
            }
        } catch (Exception ignored) {
            // Fallback to plain string parsing.
        }

        String trimmed = endpoint == null ? "" : endpoint.trim();
        if (trimmed.startsWith("http://")) {
            trimmed = trimmed.substring("http://".length());
        } else if (trimmed.startsWith("https://")) {
            trimmed = trimmed.substring("https://".length());
        }
        int slashIndex = trimmed.indexOf('/');
        if (slashIndex > 0) {
            return trimmed.substring(0, slashIndex);
        }
        return trimmed;
    }

    private boolean isOssConfigComplete() {
        return StringUtils.hasText(ossEndpoint)
            && StringUtils.hasText(ossBucket)
            && StringUtils.hasText(ossAccessKeyId)
            && StringUtils.hasText(ossAccessKeySecret);
    }
}
