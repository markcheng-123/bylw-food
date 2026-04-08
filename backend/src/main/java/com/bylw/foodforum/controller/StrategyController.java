package com.bylw.foodforum.controller;

import com.bylw.foodforum.common.ApiResponse;
import com.bylw.foodforum.common.PageResult;
import com.bylw.foodforum.dto.strategy.StrategyQueryDTO;
import com.bylw.foodforum.service.StrategyService;
import com.bylw.foodforum.vo.strategy.StrategyCardVO;
import com.bylw.foodforum.vo.strategy.StrategyDetailVO;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/strategies")
public class StrategyController {

    private final StrategyService strategyService;

    public StrategyController(StrategyService strategyService) {
        this.strategyService = strategyService;
    }

    @GetMapping
    public ApiResponse<PageResult<StrategyCardVO>> queryStrategies(@Valid StrategyQueryDTO queryDTO) {
        return ApiResponse.success(strategyService.queryStrategies(queryDTO));
    }

    @GetMapping("/{id}")
    public ApiResponse<StrategyDetailVO> getStrategyDetail(@PathVariable Long id) {
        return ApiResponse.success(strategyService.getStrategyDetail(id)); 
    }
}
