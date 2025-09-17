package pages.sharepoint;

import enums.Activity;
import models.BulkDateHour;
import models.BulkEntryRow;
import pages.Page;
import utils.Browser;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static pages.sharepoint.TimeEntryPage.handleCombo;

public class BulkPage extends Page {
    public static final String F_ISSUE = "//label[@for='entries.%d.issueId']/..//input[@role='combobox']";
    public static final String F_ACTIVITY = "//label[@for='entries.%d.activityId']/..//input[@role='combobox']";
    public static final String F_SUMMARY = "//input[@name='entries.%d.comments']";
    public static final String F_HOURS = "//input[@name='entries.%d.hours.%d.numberOfHours']";
    public static final String HEADER = "//form/table/thead/tr/th";
    public static final String ADD_ENTRY = "//button[text()='Add Entry']";
    public static final String SUBMIT = "//button[@type='submit']";

    public BulkPage(Browser browser) {
        super(browser);
    }

    public void createEntry(String issue, Activity activity, BulkEntryRow row, int rowNum) {
        String issueXPath = F_ISSUE.formatted(rowNum);
        handleCombo(browser, issueXPath, issue);
        String activityXPath = F_ACTIVITY.formatted(rowNum);
        handleCombo(browser, activityXPath, activity.getDisplayedName());
        String summary = F_SUMMARY.formatted(rowNum);
        String title = row.ticketTitle();
        browser.sendKeys(summary, title);
        ArrayList<String> header = browser.getAttributeOfElements(HEADER, "");

        for (BulkDateHour dateHourEntry : row.dateHourEntries()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE dd");
            String dateString = formatter.format(dateHourEntry.date());
            int colNum = header.indexOf(dateString) - 1;
            String hours = F_HOURS.formatted(rowNum, colNum);
            browser.sendKeysWithClear(hours, dateHourEntry.hours().toPlainString());
        }

        browser.click(ADD_ENTRY);
    }
}
