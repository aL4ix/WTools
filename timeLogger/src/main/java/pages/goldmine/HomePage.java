package pages.goldmine;

import pages.Page;
import utils.Browser;

public class HomePage extends Page {

    private static final String HOME_H2 = "//h2[text()='Home']";
    private static final String ISSUES_URL = "%s/goldmine/issues/%d?tab=time_entries";
    private static final String LOG_TIME_URL = "%s/goldmine/issues/%d/time_entries/new";
    private final String url;

    public HomePage(Browser browser, String url) {
        super(browser);
        this.url = url;
        browser.waitForVisibilityOfElement(HOME_H2);
    }

    public IssuePage openIssueByNum(int issueNum) {
        browser.get(String.format(ISSUES_URL, url, issueNum));
        return new IssuePage(browser);
    }

    public LogTimePage openLogTimePageByIssueNum(int issueNum) {
        browser.get(String.format(LOG_TIME_URL, url, issueNum));
        return new LogTimePage(browser);
    }
}
