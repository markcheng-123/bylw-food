package com.bylw.foodforum.dto.strategy;

import javax.validation.constraints.Min;
import lombok.Data;

@Data
public class StrategyQueryDTO {

    private String keyword;

    @Min(value = 1, message = "页码不能小于1")
    private long current = 1L;

    @Min(value = 1, message = "每页数量不能小于1")
    private long size = 6L;
}
