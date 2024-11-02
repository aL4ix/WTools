package pages.sharepoint;

import models.TimeEntry;
import models.TimeGroup;
import pages.TimeLogger;

public class SharePointPage extends TimeLogger implements AutoCloseable {

    private final TimeEntryPage timeEntryPage;

    public SharePointPage(String username, String password, String browser, String url) {
        super(browser, username, password);
        LoginPage loginPage = new LoginPage(this.browser, url);
        timeEntryPage = loginPage.login(username, password);
    }

    @Override
    public boolean logTimeGroup(TimeGroup group) {
        System.out.printf("For issue: %s\n", group.getIssue());
        for (TimeEntry entry : group.getEntries()) {
            System.out.printf("Creating entry: %s\n", entry);
            timeEntryPage.createEntry(group.getIssue(), entry);
            System.out.println("Created!");
        }
        return true;
    }

    @Override
    public void close() {
        browser.close();
    }
}
