/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.ticketsystem.dto;

import com.example.ticketsystem.entity.EventStatus;
import com.example.ticketsystem.strategy.PricingType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 *
 * @author Yakup Yılmaz
 */
@Data
public class EventUpdateRequest 
{
    @Size(max = 150, message = "Etkinlik adı en fazla 150 karakter olabilir")
    private String name;

    @Size(max = 500, message = "Açıklama en fazla 500 karakter olabilir")
    private String description;

    @Future(message = "Etkinlik tarihi gelecekte bir zaman olmalıdır")
    private LocalDateTime eventDate;

    @DecimalMin(value = "0.0", inclusive = false, message = "Fiyat 0'dan büyük olmalıdır")
    private BigDecimal basePrice;

    @Min(value = 1, message = "Toplam koltuk sayısı en az 1 olmalıdır")
    private Integer totalSeats;
    private PricingType pricingType;
    private EventStatus status;    
}
