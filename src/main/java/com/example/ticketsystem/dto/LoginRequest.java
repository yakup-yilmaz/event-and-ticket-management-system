package com.example.ticketsystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "E-posta boş olamaz")
    @Email(message = "Geçerli bir e-posta giriniz")
    private String email;

    @ToString.Exclude
    @NotBlank(message = "Şifre boş olamaz")
    private String password;
}
