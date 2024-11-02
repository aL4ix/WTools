package models;

import enums.Activity;
import org.junit.Assert;

import java.time.DayOfWeek;
import java.time.LocalDate;

public record UnvalidatedTimeEntry(LocalDate date, String description, String hours, Activity activity) {
    public UnvalidatedTimeEntry {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        Assert.assertFalse(String.format("Are you sure you want to log weekend hours? %s", date),
                dayOfWeek.equals(DayOfWeek.SATURDAY) || dayOfWeek.equals(DayOfWeek.SUNDAY));
    }
}
