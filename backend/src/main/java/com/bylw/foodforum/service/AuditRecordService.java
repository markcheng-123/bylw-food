package com.bylw.foodforum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bylw.foodforum.entity.AuditRecord;

public interface AuditRecordService extends IService<AuditRecord> {

    void createRecord(String businessType, Long businessId, Integer result, String remark, Long operatorAdminId);
}
