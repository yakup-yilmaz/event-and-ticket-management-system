package com.example.ticketsystem.strategy;

import com.example.ticketsystem.exception.BusinessException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 *
 * @author Yakup Yılmaz
 */
@Component
public class PricingStrategyFactory 
{

    private final Map<PricingType,PricingStrategy> strategies;
   
    
 public PricingStrategyFactory(List<PricingStrategy> strategyList) 
    {
        this.strategies =strategyList.stream()
                .collect(Collectors.toMap(PricingStrategy::getType, Function.identity()));
    }
    
   public PricingStrategy getStrategy(PricingType type) {
        return Optional.ofNullable(strategies.get(type))
                .orElseThrow(() -> new BusinessException("Gecerli fiyatlandirma stratejisi bulunamadi: " + type));
    }
}
