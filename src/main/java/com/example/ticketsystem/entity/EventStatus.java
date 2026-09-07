/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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

