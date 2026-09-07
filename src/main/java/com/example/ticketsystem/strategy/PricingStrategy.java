/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
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
