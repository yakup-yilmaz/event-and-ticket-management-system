package com.example.ticketsystem.strategy;

import com.example.ticketsystem.entity.Event;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 *
 * @author Yakup Yılmaz
 */
@Component
public class StandartPricingStrategy implements PricingStrategy
{

    @Override
    public BigDecimal calculatePrice(Event event) 
    {
        return event.getBasePrice();
    }

    @Override
    public PricingType getType() 
    {
        return PricingType.STANDARD;
    }
    
}
