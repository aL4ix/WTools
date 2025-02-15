package main;

import com.opencsv.exceptions.CsvValidationException;
import models.BulkEntryRow;
import pages.sharepoint.SharePointPage;
import utils.BulkCSVProcessor;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws IOException, CsvValidationException {
        Configuration conf = getConfiguration();

//        TimeGroup timeGroup = CSVParser.parse();
//        System.out.println(timeGroup);

        List<BulkEntryRow> bulkEntryRows = BulkCSVProcessor.parseBulkFile("merged_report.csv");

        try (SharePointPage sharepoint = new SharePointPage(conf.username(), conf.password(), conf.browser(), conf.url())) {
//            sharepoint.logTimeGroup(timeGroup);
            sharepoint.logBulk(conf.issue(), bulkEntryRows);
        }
    }

    public static Configuration getConfiguration() throws IOException {
        Properties properties = new Properties();
        FileReader reader = new FileReader("configuration.properties");
        properties.load(reader);
        String username = properties.getProperty("username");
        String password = properties.getProperty("password");
        String browser = properties.getProperty("browser");
        String url = properties.getProperty("url");
        String issue = properties.getProperty("issue");
        return new Configuration(username, password, browser, url, issue);
    }

    public record Configuration(String username, String password, String browser, String url, String issue) {
    }
}
