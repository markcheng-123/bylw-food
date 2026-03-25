package com.bylw.foodforum.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bylw.foodforum.entity.AuditRecord;
import com.bylw.foodforum.mapper.AuditRecordMapper;
import com.bylw.foodforum.service.AuditRecordService;
import org.springframework.stereotype.Service;

@Service
public class AuditRecordServiceImpl extends ServiceImpl<AuditRecordMapper, AuditRecord> implements AuditRecordService {

    @Override
    public void createRecord(String businessType, Long businessId, Integer result, String remark, Long operatorAdminId) {
        AuditRecord record = new AuditRecord();
        record.setBusinessType(businessType);
        record.setBusinessId(businessId);
        record.setResult(result);
        record.setRemark(remark);
        record.setOperatorAdminId(operatorAdminId);
        save(record);
    }
}
