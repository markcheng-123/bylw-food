package com.bylw.foodforum.service;

import com.bylw.foodforum.vo.admin.ImageMigrationResultVO;

public interface ImageMigrationService {

    ImageMigrationResultVO migrateLocalUploadsToOss();
}
