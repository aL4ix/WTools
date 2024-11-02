package pages.goldmine;

import pages.Page;
import utils.Browser;

public class SpentTimePage extends Page {
    public SpentTimePage(Browser browser) {
        super(browser);
        browser.waitForVisibilityOfElement("//h2[text()='Spent time']");
    }
}
