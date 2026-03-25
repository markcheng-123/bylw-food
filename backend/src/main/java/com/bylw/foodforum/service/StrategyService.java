package com.bylw.foodforum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bylw.foodforum.common.PageResult;
import com.bylw.foodforum.dto.strategy.StrategyQueryDTO;
import com.bylw.foodforum.entity.Strategy;
import com.bylw.foodforum.vo.strategy.StrategyCardVO;
import com.bylw.foodforum.vo.strategy.StrategyDetailVO;

public interface StrategyService extends IService<Strategy> {

    PageResult<StrategyCardVO> queryStrategies(StrategyQueryDTO queryDTO);

    StrategyDetailVO getStrategyDetail(Long id);
}
