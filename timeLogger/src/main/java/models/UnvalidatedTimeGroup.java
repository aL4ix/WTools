package models;

import org.junit.Assert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class UnvalidatedTimeGroup {
    private final String issue;

    private final List<UnvalidatedTimeEntry> entries;

    public UnvalidatedTimeGroup(String issue) {
        this.issue = issue;
        entries = new ArrayList<>();
    }

    public boolean addEntry(UnvalidatedTimeEntry entry) {
        return entries.add(entry);
    }


    public TimeGroup validate() {
        TimeGroup timeGroup = new TimeGroup(issue);
        HashMap<LocalDate, BigDecimal> dateHours = new HashMap<>();
        final BigDecimal EIGHT_HOURS = new BigDecimal("8");
        for (UnvalidatedTimeEntry entry : entries) {
            LocalDate date = entry.date();
            BigDecimal loggedHours = dateHours.getOrDefault(date, BigDecimal.ZERO);
            String entryHours = entry.hours();
            BigDecimal finalHours;
            if (entryHours.equals("*")) {
                finalHours = EIGHT_HOURS.subtract(loggedHours);
                Assert.assertTrue("Calculating result for '*' brought a negative number",
                        finalHours.signum() > 0);
            } else {
                finalHours = new BigDecimal(entryHours);
            }
            dateHours.put(date, loggedHours.add(finalHours));
            timeGroup.addEntry(new TimeEntry(date, entry.description(), finalHours,
                    entry.activity()));
        }
        for (Entry<LocalDate, BigDecimal> entry : dateHours.entrySet()) {
            BigDecimal hours = entry.getValue();
            Assert.assertEquals(String.format("Entry BigNumber.compareTo() failed: %s has %s hours, "
                    + "expected 8.", entry.getKey(), hours), 0, hours.compareTo(EIGHT_HOURS));
        }

        return timeGroup;
    }
}
