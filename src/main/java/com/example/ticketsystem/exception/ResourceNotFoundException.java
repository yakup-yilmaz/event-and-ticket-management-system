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
