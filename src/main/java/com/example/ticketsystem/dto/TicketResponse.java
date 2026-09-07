/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.ticketsystem.dto;

import com.example.ticketsystem.entity.TicketStatus;
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
public class TicketResponse 
{
 
    private Long id;
    private String ticketCode;
    private String seatNumber; 
    private BigDecimal pricePaid;
    private TicketStatus status;
    private LocalDateTime purchasedAt; 
    
    private Long userId;
    private String userName; 
    private Long eventId;
    private String eventName;
    private LocalDateTime eventDate;
    
}
