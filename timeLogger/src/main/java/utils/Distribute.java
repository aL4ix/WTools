package utils;

import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

enum MyColumns implements ColumnEnum<MyColumns> {
    DAY("Day"),
    ID("Id");

    private final String columnName;

    MyColumns(String columnName) {
        this.columnName = columnName;
    }

    @Override
    public String getColumnName() {
        return columnName;
    }
}

public class Distribute {
    public static void main(String[] args) throws CsvException, IOException {
        CSV2Map<MyColumns> csv2Map = new CSV2Map<>(MyColumns.class, "newTimesheet.csv");
        List<Map.Entry<MyColumns, String>> csvData = csv2Map.readCSV();

        for (Map.Entry<MyColumns, String> entry : csvData) {
            System.out.println("Enum constant: " + entry.getKey() + ", Value: " + entry.getValue());
        }
    }
}