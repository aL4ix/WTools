package utils;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import models.BulkEntryRow;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BulkCSVProcessor {
    public static void main(String[] args) throws CsvValidationException, IOException {
        String csvFile = "merged_report.csv";
        List<BulkEntryRow> bulkEntryRows = parseBulkFile(csvFile);

        for (BulkEntryRow bulkEntryRow : bulkEntryRows) {
            System.out.println("Title: " + bulkEntryRow.ticketTitle());
            System.out.println("Data: " + bulkEntryRow.dateHourEntries());
            System.out.println();
        }
    }

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

                String titleFromFile = line[0];
                if (titleFromFile.isBlank()) { // Handle sum row
                    continue;
                }
                String title = prepareTitle(titleFromFile);
                BulkEntryRow bulkEntryRow = recordMap.computeIfAbsent(title, k -> new BulkEntryRow(title));

                // Process all days
                for (int i = 1; i < line.length; i++) {
                    String dayString = "";
                    try {
                        dayString = header[i];
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println("Header not found for column=%d. Replacing it with an empty string.\n%s".formatted(i, Arrays.toString(header)));
                    }
                    if (dayString.isBlank()) { // Handle sum column
                        continue;
                    }
                    int dayNumber = Integer.parseInt(dayString);
                    LocalDate date = LocalDate.now().withDayOfMonth(dayNumber);
                    String cell = getOrDefault(line, i, "");
                    if (cell.isBlank()) {
                        cell = "0";
                    }
                    BigDecimal hours = new BigDecimal(cell);

                    bulkEntryRow.addDateHours(date, hours);
                }
            }

            bulkEntryRows = new ArrayList<>(recordMap.values());

        }
        return bulkEntryRows;
    }

    public static <T> T getOrDefault(T[] array, int index, T defaultValue) {
        if (index >= 0 && index < array.length) {
            return array[index];
        } else {
            return defaultValue;
        }
    }

    private static String prepareTitle(String title) {
        String regex = "^\\S+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(title);
        String result;
        if (matcher.find()) {
            result = matcher.group();
        } else {
            result = title;
        }
        final int LENGTH = 10;
        result = String.format("%-" + LENGTH + "s", result);
        return result;
    }
}

