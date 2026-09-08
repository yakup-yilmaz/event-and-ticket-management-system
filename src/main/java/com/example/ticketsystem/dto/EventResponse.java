package com.example.ticketsystem.dto;

import com.example.ticketsystem.entity.EventStatus;
import com.example.ticketsystem.strategy.PricingType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 *
 * @author Yakup Yılmaz
 */
@Data
@Builder
public class EventResponse 
{
    private Long id;
    private String name;
    private String description;
    private Integer totalSeats;
    private Integer availableSeats;
    private BigDecimal basePrice;
    private LocalDateTime eventDate;
    private EventStatus status;
    private PricingType pricingType;
}
