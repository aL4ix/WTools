package utils;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CSV2Map<T extends Enum<T> & ColumnEnum<T>> {
    private final Class<T> enumClass;
    private final String csvFile;

    public CSV2Map(Class<T> enumClass, String csvFile) {
        this.enumClass = enumClass;
        this.csvFile = csvFile;
    }

    public List<Map.Entry<T, String>> readCSV() throws IOException, CsvException {
        List<Map.Entry<T, String>> csvData = new ArrayList<>();
        try (CSVReader csvReader = new CSVReaderBuilder(new FileReader(csvFile)).build()) {
            String[] header = csvReader.readNext();
            T[] enumConstants = enumClass.getEnumConstants();
            if (header.length != enumConstants.length) {
                throw new CsvException("Number of columns in CSV file does not match number of enum constants");
            }
            for (int i = 0; i < header.length; i++) {
                if (!header[i].equals(enumConstants[i].getColumnName())) {
                    throw new CsvException("Column name in CSV file does not match enum constant: " + header[i] + " vs " + enumConstants[i].getColumnName());
                }
            }
            String[] values;
            while ((values = csvReader.readNext()) != null) {
                for (int i = 0; i < values.length; i++) {
                    csvData.add(new AbstractMap.SimpleImmutableEntry<>(enumConstants[i], values[i]));
                }
            }
        }
        return csvData;
    }
}