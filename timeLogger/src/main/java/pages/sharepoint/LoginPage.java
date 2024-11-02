package pages.sharepoint;

import pages.Page;
import utils.Browser;

public class LoginPage extends Page {
    private static final String EMAIL = "//input[@type=\"email\"]";
    private static final String NEXT = "//input[@type=\"submit\"]";
    private static final String PASSWD = "//input[@type=\"password\"]";

    public LoginPage(Browser browser, String url) {
        super(browser);
        browser.get(url);
    }

    public TimeEntryPage login(String username, String password) {
        browser.sendKeys(EMAIL, username);
        browser.click(NEXT);
        browser.sendKeys(PASSWD, password);
        browser.click(NEXT);
        browser.click(NEXT);
        return new TimeEntryPage(browser);
    }
}
