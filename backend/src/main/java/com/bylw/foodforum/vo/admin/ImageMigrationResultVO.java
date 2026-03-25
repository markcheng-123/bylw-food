package com.bylw.foodforum.vo.admin;

import lombok.Data;

@Data
public class ImageMigrationResultVO {

    private int scanned;

    private int migrated;

    private int skipped;

    private int missingLocalFile;
}
