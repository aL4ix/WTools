package models;

import enums.Activity;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TimeEntry(LocalDate date, String description, BigDecimal hours, Activity activity) {
}
