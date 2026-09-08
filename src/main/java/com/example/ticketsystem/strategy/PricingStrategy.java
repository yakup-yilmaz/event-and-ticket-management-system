package com.example.ticketsystem.strategy;

import com.example.ticketsystem.entity.Event;
import java.math.BigDecimal;

/**
 *
 * @author Yakup Yılmaz
 */
public interface PricingStrategy 
{
   BigDecimal calculatePrice(Event event);
   
   PricingType getType();
   
   
   
}
