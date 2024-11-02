package main;

import com.opencsv.exceptions.CsvValidationException;
import models.TimeGroup;
import pages.sharepoint.SharePointPage;
import utils.CSVParser;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws IOException, CsvValidationException {
        Properties properties = new Properties();
        FileReader reader = new FileReader("configuration.properties");
        properties.load(reader);
        String username = properties.getProperty("username");
        String password = properties.getProperty("password");
        String browser = properties.getProperty("browser");
        String url = properties.getProperty("url");

        TimeGroup timeGroup = CSVParser.parse();
        System.out.println(timeGroup);

        try (SharePointPage sharepoint = new SharePointPage(username, password, browser, url)) {
            sharepoint.logTimeGroup(timeGroup);
        }
    }
}