/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.ticketsystem.exception;

/**
 *
 * @author Yakup Yılmaz
 */

public class BusinessException extends RuntimeException 
{
  public BusinessException(String message)
  {
      super(message);
  }
}
