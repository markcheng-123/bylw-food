package com.bylw.foodforum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("audit_record")
public class AuditRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String businessType;

    private Long businessId;

    private Long operatorAdminId;

    private Integer result;

    private String remark;

    private LocalDateTime createdAt;
}
