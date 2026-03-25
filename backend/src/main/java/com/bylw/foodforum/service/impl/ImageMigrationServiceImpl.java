package com.bylw.foodforum.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.bylw.foodforum.common.exception.BusinessException;
import com.bylw.foodforum.entity.User;
import com.bylw.foodforum.service.ImageMigrationService;
import com.bylw.foodforum.service.UserService;
import com.bylw.foodforum.vo.admin.ImageMigrationResultVO;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ImageMigrationServiceImpl implements ImageMigrationService {

    private static final String LOCAL_PREFIX = "/uploads/";

    private final JdbcTemplate jdbcTemplate;
    private final UserService userService;

    @Value("${app.upload-path}")
    private String uploadPath;

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

    public ImageMigrationServiceImpl(JdbcTemplate jdbcTemplate, UserService userService) {
        this.jdbcTemplate = jdbcTemplate;
        this.userService = userService;
    }

    @Override
    public ImageMigrationResultVO migrateLocalUploadsToOss() {
        ensureAdmin();
        validateOssConfig();

        String endpoint = normalizeEndpoint(ossEndpoint);
        String endpointHost = extractEndpointHost(endpoint);
        Map<String, String> urlCache = new HashMap<String, String>();
        ImageMigrationResultVO result = new ImageMigrationResultVO();

        OSS ossClient = new OSSClientBuilder().build(endpoint, ossAccessKeyId, ossAccessKeySecret);
        try {
            migrateSimpleField(ossClient, endpointHost, "user", "avatar", result, urlCache);
            migrateSimpleField(ossClient, endpointHost, "food_post", "cover_image", result, urlCache);
            migrateSimpleField(ossClient, endpointHost, "food_image", "image_url", result, urlCache);
            migrateSimpleField(ossClient, endpointHost, "merchant_application", "business_license", result, urlCache);
            migrateSimpleField(ossClient, endpointHost, "merchant_profile", "business_license_url", result, urlCache);
            migrateSimpleField(ossClient, endpointHost, "merchant_profile", "food_safety_cert_url", result, urlCache);
            migrateSimpleField(ossClient, endpointHost, "chef_info", "avatar_url", result, urlCache);
            migrateSimpleField(ossClient, endpointHost, "strategy", "cover_image", result, urlCache);
            migrateFoodDishImageUrls(ossClient, endpointHost, result, urlCache);
            return result;
        } finally {
            ossClient.shutdown();
        }
    }

    private void migrateSimpleField(
        OSS ossClient,
        String endpointHost,
        String table,
        String column,
        ImageMigrationResultVO result,
        Map<String, String> urlCache
    ) {
        String querySql = "SELECT id, " + column + " AS image_url FROM " + table + " WHERE " + column + " LIKE '/uploads/%'";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(querySql);
        for (Map<String, Object> row : rows) {
            result.setScanned(result.getScanned() + 1);
            Long id = ((Number) row.get("id")).longValue();
            String originalUrl = row.get("image_url") == null ? null : String.valueOf(row.get("image_url"));
            if (!StringUtils.hasText(originalUrl)) {
                result.setSkipped(result.getSkipped() + 1);
                continue;
            }
            String migratedUrl = migrateOneUrl(ossClient, endpointHost, originalUrl, result, urlCache);
            if (!StringUtils.hasText(migratedUrl)) {
                continue;
            }
            jdbcTemplate.update("UPDATE " + table + " SET " + column + " = ? WHERE id = ?", migratedUrl, id);
            result.setMigrated(result.getMigrated() + 1);
        }
    }

    private void migrateFoodDishImageUrls(
        OSS ossClient,
        String endpointHost,
        ImageMigrationResultVO result,
        Map<String, String> urlCache
    ) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT id, image_url FROM food_dish WHERE image_url IS NOT NULL AND image_url <> '' AND image_url LIKE '%/uploads/%'");
        for (Map<String, Object> row : rows) {
            result.setScanned(result.getScanned() + 1);
            Long id = ((Number) row.get("id")).longValue();
            String originalValue = String.valueOf(row.get("image_url"));
            String[] parts = originalValue.split(",");
            List<String> migratedParts = new ArrayList<String>();
            boolean changed = false;
            for (String part : parts) {
                String trimmed = part == null ? "" : part.trim();
                if (!trimmed.startsWith(LOCAL_PREFIX)) {
                    if (StringUtils.hasText(trimmed)) {
                        migratedParts.add(trimmed);
                    }
                    continue;
                }
                String migratedUrl = migrateOneUrl(ossClient, endpointHost, trimmed, result, urlCache);
                if (StringUtils.hasText(migratedUrl)) {
                    migratedParts.add(migratedUrl);
                    changed = true;
                }
            }
            if (!changed) {
                result.setSkipped(result.getSkipped() + 1);
                continue;
            }
            String nextValue = String.join(",", migratedParts);
            jdbcTemplate.update("UPDATE food_dish SET image_url = ? WHERE id = ?", nextValue, id);
            result.setMigrated(result.getMigrated() + 1);
        }
    }

    private String migrateOneUrl(
        OSS ossClient,
        String endpointHost,
        String localUrl,
        ImageMigrationResultVO result,
        Map<String, String> urlCache
    ) {
        if (urlCache.containsKey(localUrl)) {
            return urlCache.get(localUrl);
        }
        String fileName = localUrl.substring(LOCAL_PREFIX.length());
        File localFile = new File(uploadPath, fileName);
        if (!localFile.exists() || !localFile.isFile()) {
            result.setMissingLocalFile(result.getMissingLocalFile() + 1);
            return null;
        }

        String objectKey = buildObjectKey(fileName);
        try (InputStream inputStream = new FileInputStream(localFile)) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(localFile.length());
            String contentType = Files.probeContentType(localFile.toPath());
            if (StringUtils.hasText(contentType)) {
                metadata.setContentType(contentType);
            }
            ossClient.putObject(new PutObjectRequest(ossBucket, objectKey, inputStream, metadata));
        } catch (Exception exception) {
            throw new BusinessException("迁移图片到OSS失败: " + fileName);
        }

        String migratedUrl = buildPublicUrl(endpointHost, objectKey);
        urlCache.put(localUrl, migratedUrl);
        return migratedUrl;
    }

    private String buildObjectKey(String fileName) {
        String prefix = StringUtils.hasText(ossDirPrefix) ? ossDirPrefix.trim() : "bylw/";
        if (!prefix.endsWith("/")) {
            prefix = prefix + "/";
        }
        return prefix + "legacy/" + fileName;
    }

    private String buildPublicUrl(String endpointHost, String objectKey) {
        if (StringUtils.hasText(ossPublicBaseUrl)) {
            String base = ossPublicBaseUrl.trim();
            if (base.endsWith("/")) {
                base = base.substring(0, base.length() - 1);
            }
            return base + "/" + objectKey;
        }
        return "https://" + ossBucket + "." + endpointHost + "/" + objectKey;
    }

    private String normalizeEndpoint(String endpoint) {
        String trimmed = endpoint.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        return "https://" + trimmed;
    }

    private String extractEndpointHost(String endpoint) {
        String trimmed = endpoint.trim();
        if (trimmed.startsWith("http://")) {
            return trimmed.substring("http://".length());
        }
        if (trimmed.startsWith("https://")) {
            return trimmed.substring("https://".length());
        }
        return trimmed;
    }

    private void ensureAdmin() {
        User currentUser = userService.getCurrentUserEntity();
        if (currentUser.getRole() == null || currentUser.getRole() != 9) {
            throw new BusinessException(403, "No admin permission");
        }
    }

    private void validateOssConfig() {
        if (!StringUtils.hasText(ossEndpoint)
            || !StringUtils.hasText(ossBucket)
            || !StringUtils.hasText(ossAccessKeyId)
            || !StringUtils.hasText(ossAccessKeySecret)) {
            throw new BusinessException("OSS配置不完整，无法迁移历史图片");
        }
    }
}
