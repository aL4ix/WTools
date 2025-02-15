package utils;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import models.BulkEntryRow;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class BulkCSVProcessor {
    public static List<BulkEntryRow> parseBulkFile(String csvFile) throws IOException, CsvValidationException {
        List<BulkEntryRow> bulkEntryRows;

        try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
            // Skip header row
            String[] header = reader.readNext();

            LinkedHashMap<String, BulkEntryRow> recordMap = new LinkedHashMap<>();
            String[] line;

            while ((line = reader.readNext()) != null) {
                if (line.length == 0) {
                    continue; // Handle empty lines
                }

                String title = line[0];
                if (title.isBlank()) { // Handle sum row
                    continue;
                }
                BulkEntryRow bulkEntryRow = recordMap.computeIfAbsent(title, k -> new BulkEntryRow(title));

                // Process all days
                for (int i = 1; i < line.length; i++) {
                    String dayString = header[i];
                    if (dayString.isBlank()) { // Handle sum column
                        continue;
                    }
                    int dayNumber = Integer.parseInt(dayString);
                    LocalDate date = LocalDate.now().withDayOfMonth(dayNumber);
                    BigDecimal hours = new BigDecimal(line[i]);

                    bulkEntryRow.addDateHours(date, hours);
                }
            }

            bulkEntryRows = new ArrayList<>(recordMap.values());

        }
        return bulkEntryRows;
    }
}
