package models;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BulkDateHour(LocalDate date, BigDecimal hours) {
    @Override
    public String toString() {
        return "{" +
                "date=" + date +
                ", hours=" + hours +
                '}';
    }
}
