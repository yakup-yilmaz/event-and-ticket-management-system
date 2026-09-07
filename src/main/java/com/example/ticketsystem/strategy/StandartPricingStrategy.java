/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
