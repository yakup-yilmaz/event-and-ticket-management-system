package com.example.ticketsystem.strategy;

import com.example.ticketsystem.entity.Event;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 *
 * @author Yakup Yılmaz
 */
@Component
public class OccupancyBasedPricingStrategy implements PricingStrategy
{

    @Override
    public BigDecimal calculatePrice(Event event)
    {
        int totalSeats = event.getTotalSeats();

        // Kapasite sıfırsa (teorik uç durum) taban fiyatı dön
        if (totalSeats <= 0) {
            return event.getBasePrice();
        }

        int soldSeats = totalSeats - event.getAvailableSeats();
        double occupancyRate = (double) soldSeats / totalSeats;

        if (occupancyRate >= 0.8) {
            return event.getBasePrice()
                    .multiply(BigDecimal.valueOf(1.50))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        }
        else if (occupancyRate >= 0.5) {
            return event.getBasePrice()
                    .multiply(BigDecimal.valueOf(1.20))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        }

        return event.getBasePrice();
    }

    @Override
    public PricingType getType() {
       return PricingType.OCCUPANCY_BASED;
    }
 
 
    
}
