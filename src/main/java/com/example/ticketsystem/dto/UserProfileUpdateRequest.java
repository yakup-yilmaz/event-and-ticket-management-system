package com.example.ticketsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Kullanıcının kendi profilini güncellemesi (role/email/password değiştirilmez). */
@Data
public class UserProfileUpdateRequest {

    @NotBlank(message = "Ad alanı boş bırakılamaz")
    @Size(min = 2, max = 50, message = "Ad 2 ile 50 karakter arasında olmalıdır")
    private String firstName;

    @NotBlank(message = "Soyad alanı boş bırakılamaz")
    @Size(min = 2, max = 50, message = "Soyad 2 ile 50 karakter arasında olmalıdır")
    private String lastName;
}
