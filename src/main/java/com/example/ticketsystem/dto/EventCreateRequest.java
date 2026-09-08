package com.example.ticketsystem.dto;

import com.example.ticketsystem.strategy.PricingType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Yakup Yılmaz
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventCreateRequest 
{
    
    @NotBlank(message = "Etkinlik adı boş bırakılamaz")
    @Size(max = 150, message = "Etkinlik adı en fazla 150 karakter olabilir")
    private String name;

    @NotBlank(message = "Açıklama boş bırakılamaz")
    @Size(max = 500, message = "Açıklama en fazla 500 karakter olabilir")
    private String description;

    @NotNull(message = "Toplam koltuk sayısı belirtilmelidir")
    @Min(value = 1, message = "Toplam koltuk sayısı en az 1 olmalıdır")
    private Integer totalSeats;

    @NotNull(message = "Taban fiyat belirtilmelidir")
    @DecimalMin(value = "0.0", inclusive = false, message = "Fiyat 0'dan büyük olmalıdır")
    private BigDecimal basePrice;

    @NotNull(message = "Etkinlik tarihi belirtilmelidir")
    @Future(message = "Etkinlik tarihi gelecekte bir zaman olmalıdır")
    private LocalDateTime eventDate;

    private PricingType pricingType;
    
}
