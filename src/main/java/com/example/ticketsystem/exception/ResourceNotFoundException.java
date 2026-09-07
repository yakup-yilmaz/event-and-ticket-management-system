/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.ticketsystem.exception;

/**
 *
 * @author Yakup Yılmaz
 */

//Veri tabanında aradığımız kaydı bulamazsak (User,Event,Ticket) hepsi için kullanılabilir
public class ResourceNotFoundException extends RuntimeException
{
    public ResourceNotFoundException(String message)
    {
        super(message);
    }
}
