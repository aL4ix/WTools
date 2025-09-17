package models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public record BulkEntryRow(String ticketTitle, List<BulkDateHour> dateHourEntries) {
    public BulkEntryRow(String ticketTitle) {
        this(ticketTitle, new ArrayList<>());
    }

    public void addDateHours(LocalDate date, BigDecimal hours) {
        if (hours.compareTo(BigDecimal.ZERO) > 0) {
            dateHourEntries.add(new BulkDateHour(date, hours));
        }
    }
}
