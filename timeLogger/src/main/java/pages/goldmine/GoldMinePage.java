package pages.goldmine;

import enums.Activity;
import enums.CollegeActivity;
import models.BulkDateHour;
import models.BulkEntryRow;
import models.TimeEntry;
import models.TimeGroup;
import pages.TimeLogger;

import java.util.List;

import enums.CollegeActivity;

public class GoldMinePage extends TimeLogger implements AutoCloseable {
    HomePage homePage;

    public GoldMinePage(String username, String password, String browserName, String url) {
        super(browserName, username, password);
        LoginPage loginPage = new LoginPage(browser, url);
        homePage = loginPage.login(username, password);
    }

    @Override
    public boolean logTimeGroup(TimeGroup group) {
        // TODO
        System.out.printf("For issue: %s\n", group.getIssue());
        for (TimeEntry entry : group.getEntries()) {
            System.out.printf("Creating entry: %s\n", entry);
//            timeEntryPage.createEntry(group.getIssue(), entry);
            homePage.openIssueByNum(Integer.parseInt(group.getIssue()));
//            homePage.openLogTimePageByIssueNum()
//            System.out.println("Created!");
        }
        return false;
    }

    @Override
    public boolean logBulk(String issue, List<BulkEntryRow> bulkEntryRows) {
        for (BulkEntryRow row : bulkEntryRows) {
            Activity activity = CollegeActivity.getActivityFromTicketTitle(row.ticketTitle());
            for (BulkDateHour dateHourEntry : row.dateHourEntries()) {
                LogTimePage logTimePage = homePage.openLogTimePageByIssueNum(Integer.parseInt(issue));
                logTimePage.logTime(dateHourEntry.date(), dateHourEntry.hours(), row.ticketTitle(), activity);
            }
        }
        return true;
    }

    @Override
    public void close() {
        browser.close();
    }
}
