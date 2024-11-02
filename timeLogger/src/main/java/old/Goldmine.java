package old;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import enums.Activity;
import enums.CollegeActivity;
import org.junit.Assert;
import pages.goldmine.HomePage;
import pages.goldmine.LogTimePage;
import pages.goldmine.LoginPage;
import utils.Browser;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Goldmine {

    public static void logTime(String username, String password, String browserName, String url) throws IOException, CsvValidationException {
        CSVReader csv = new CSVReader(new FileReader("timesheet.csv"));

        try (Browser browser = new Browser(browserName)) {
            LoginPage loginPage = new LoginPage(browser, url);
            HomePage homePage = loginPage.login(username, password);
            String[] row;
            int issueNum = -1;
            while ((row = csv.readNext()) != null) {
                int len = row.length;
                String rowZero = row[0];
                if (len == 1) { // Issue Num row
                    if ("".equals(rowZero)) { // Allow empty rows
                        continue;
                    }
                    issueNum = Integer.parseInt(rowZero);
                } else if (len == 4) { // Time row
                    LocalDate date;
                    if ("TODAY".equals(rowZero)) {
                        date = LocalDate.now();
                    } else {
                        date = LocalDate.parse(rowZero);
                    }
                    String comment = row[1];
                    BigDecimal hours = new BigDecimal(row[2]);
                    CollegeActivity benchActivity = Activity.fromDisplayedName(row[3], CollegeActivity.values());
                    Assert.assertNotEquals("Make sure to add an issue num before a section of hours", issueNum, -1);
                    System.out.printf("Creating for issue %d: %s '%s' %s %s\n", issueNum, date.toString(), comment, hours,
                            benchActivity.getDisplayedName());
                    // Actual creation
                    LogTimePage logTimePage = homePage.openLogTimePageByIssueNum(issueNum);
                    logTimePage.logTime(date, hours, comment, benchActivity);
                    System.out.println("Created!");
                }
            }
        }
    }
}
