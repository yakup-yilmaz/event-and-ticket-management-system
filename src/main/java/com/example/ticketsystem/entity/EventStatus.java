package com.example.ticketsystem.entity;

/**
 *
 * @author Yakup Yılmaz
 */
public enum EventStatus 
{
    ACTIVE,      // Etkinlik bilet satışına açık
    PASSED,      // Etkinlik tarihi geçmiş, tamamlanmış
    CANCELLED,   // Etkinlik iptal edilmiş
    SOLD_OUT     // Tüm biletler tükenmiş (Koltuk sayısı = 0)    
}

