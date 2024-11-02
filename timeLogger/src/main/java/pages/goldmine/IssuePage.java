package pages.goldmine;

import pages.Page;
import utils.Browser;

public class IssuePage extends Page {

    public static final String TASK_H2 = "//h2[text()='Task #12345']";
    public static final String LOG_TIME = "(//a[contains(@class,'icon-time-add')])[1]";

    public IssuePage(Browser browser) {
        super(browser);
        browser.waitForVisibilityOfElement(TASK_H2);
    }

    public LogTimePage clickLogTime() {
        browser.click(LOG_TIME);
        return new LogTimePage(browser);
    }
}
