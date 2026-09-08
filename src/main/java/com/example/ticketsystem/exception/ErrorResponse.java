package com.example.ticketsystem.exception;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 *
 * @author Yakup Yılmaz
 */
@Data
@Builder
public class ErrorResponse 
{
  private int status;
  
  private String code;
  
  private String message;
  
  private LocalDateTime timestamp;
  
  private Map<String,String> validationErrors; //validasyonlar için
}
