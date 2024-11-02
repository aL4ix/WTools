package utils;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import enums.Activity;
import enums.CollegeActivity;
import models.TimeGroup;
import models.UnvalidatedTimeEntry;
import models.UnvalidatedTimeGroup;
import org.junit.Assert;

import java.io.FileReader;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class CSVParser {

    public static final String DATE_SPLITTER = "~";
    static Set<DayOfWeek> WEEKEND = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

    public static TimeGroup parse() throws IOException, CsvValidationException {
        CSVReader csv = new CSVReader(new FileReader("timesheet.csv"));
        String[] row;
        String issueNum = null;
        UnvalidatedTimeGroup unvalidatedTimeGroup = null;
        while ((row = csv.readNext()) != null) {
            String rowZero = row[0];
            int rowLength = row.length;
            if (rowLength == 1) { // Issue Num row
                if ("".equals(rowZero)) { // Allow empty rows
                    continue;
                }
                issueNum = rowZero;
                unvalidatedTimeGroup = new UnvalidatedTimeGroup(issueNum);
            } else if (rowLength == 4) { // Time row
                String comment = row[1];
                String hours = row[2];
                CollegeActivity activity = Activity.fromDisplayedName(row[3], CollegeActivity.values());
                Assert.assertNotEquals("Make sure to add an issue num before a section of hours", null, issueNum);
                assert unvalidatedTimeGroup != null;

                int dateSplit = rowZero.indexOf(DATE_SPLITTER);
                if (dateSplit != -1) { // Applies to all existing dates
                    LocalDate start = LocalDate.parse(rowZero.substring(0, dateSplit));
                    LocalDate end = LocalDate.parse(rowZero.substring(dateSplit + 1));
                    List<LocalDate> datesList = start.datesUntil(end.plusDays(1))
                            .filter(p -> !WEEKEND.contains(p.getDayOfWeek()))
                            .toList();
                    System.out.println(datesList);
                    for (LocalDate date : datesList) {
                        unvalidatedTimeGroup.addEntry(new UnvalidatedTimeEntry(date, comment, hours, activity));
                    }
                } else { // Only one date
                    LocalDate date;
                    if ("TODAY".equals(rowZero)) {
                        date = LocalDate.now();
                    } else {
                        date = LocalDate.parse(rowZero);
                    }
                    unvalidatedTimeGroup.addEntry(new UnvalidatedTimeEntry(date, comment, hours, activity));
                }
            }
        }

        Objects.requireNonNull(unvalidatedTimeGroup);
        return unvalidatedTimeGroup.validate();
    }
}
