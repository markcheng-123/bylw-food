package com.bylw.foodforum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bylw.foodforum.common.PageResult;
import com.bylw.foodforum.common.exception.BusinessException;
import com.bylw.foodforum.dto.strategy.StrategyQueryDTO;
import com.bylw.foodforum.entity.Strategy;
import com.bylw.foodforum.entity.User;
import com.bylw.foodforum.mapper.StrategyMapper;
import com.bylw.foodforum.service.StrategyService;
import com.bylw.foodforum.service.UserService;
import com.bylw.foodforum.vo.strategy.StrategyCardVO;
import com.bylw.foodforum.vo.strategy.StrategyDetailVO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class StrategyServiceImpl extends ServiceImpl<StrategyMapper, Strategy> implements StrategyService {

    private final UserService userService;

    public StrategyServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public PageResult<StrategyCardVO> queryStrategies(StrategyQueryDTO queryDTO) {
        LambdaQueryWrapper<Strategy> wrapper = buildVisibleWrapper(queryDTO.getKeyword());
        long total = count(wrapper);
        long offset = (queryDTO.getCurrent() - 1) * queryDTO.getSize();
        List<Strategy> strategies = list(wrapper
            .orderByDesc(Strategy::getSort)
            .orderByDesc(Strategy::getPublishedAt)
            .orderByDesc(Strategy::getCreatedAt)
            .last("LIMIT " + offset + "," + queryDTO.getSize()));
        return new PageResult<StrategyCardVO>(total, toCardVOList(strategies));
    }

    @Override
    public StrategyDetailVO getStrategyDetail(Long id) {
        Strategy strategy = getById(id);
        if (strategy == null || !isVisible(strategy.getStatus())) {
            throw new BusinessException("攻略不存在");
        }

        StrategyDetailVO detailVO = new StrategyDetailVO();
        BeanUtils.copyProperties(strategy, detailVO);
        User author = strategy.getAuthorUserId() == null ? null : userService.getById(strategy.getAuthorUserId());
        detailVO.setAuthorNickname(author == null ? "官方推荐" : author.getNickname());
        return detailVO;
    }

    private LambdaQueryWrapper<Strategy> buildVisibleWrapper(String keyword) {
        LambdaQueryWrapper<Strategy> wrapper = new LambdaQueryWrapper<Strategy>()
            .and(item -> item.eq(Strategy::getStatus, 1).or().eq(Strategy::getStatus, 0));

        if (StringUtils.hasText(keyword)) {
            String trimmed = keyword.trim();
            wrapper.and(item -> item.like(Strategy::getTitle, trimmed)
                .or()
                .like(Strategy::getSummary, trimmed)
                .or()
                .like(Strategy::getContent, trimmed));
        }
        return wrapper;
    }

    private List<StrategyCardVO> toCardVOList(List<Strategy> strategies) {
        if (strategies.isEmpty()) {
            return new ArrayList<StrategyCardVO>();
        }

        List<Long> authorIds = strategies.stream()
            .map(Strategy::getAuthorUserId)
            .filter(item -> item != null)
            .distinct()
            .collect(Collectors.toList());

        Map<Long, User> userMap = userService.listByIds(authorIds)
            .stream()
            .collect(Collectors.toMap(User::getId, item -> item, (left, right) -> left, HashMap::new));

        return strategies.stream().map(strategy -> {
            StrategyCardVO cardVO = new StrategyCardVO();
            BeanUtils.copyProperties(strategy, cardVO);
            User author = strategy.getAuthorUserId() == null ? null : userMap.get(strategy.getAuthorUserId());
            cardVO.setAuthorNickname(author == null ? "官方推荐" : author.getNickname());
            return cardVO;
        }).collect(Collectors.toList());
    }

    private boolean isVisible(Integer status) {
        return status == null || status == 0 || status == 1;
    }
}
