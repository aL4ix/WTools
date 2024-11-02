package pages.goldmine;

import org.junit.Assert;
import pages.Page;
import utils.Browser;

public class LoginPage extends Page {
    private static final String USERNAME = "//*[@id='username']";
    private static final String PASSWORD = "//*[@id='password']";
    private static final String SUBMIT = "//*[@id='login-submit']";
    private static final String ERROR = "//*[@id='flash_error']";
    private final String url;

    public LoginPage(Browser browser, String url) {
        super(browser);
        browser.get(url);
        this.url = url;
    }

    public HomePage login(String user, String pass) {
        browser.sendKeys(USERNAME, user);
        browser.sendKeys(PASSWORD, pass);
        browser.click(SUBMIT);
        if (browser.visibilityOfElement(ERROR)) {
            String errorMessage = browser.getText(ERROR);
            Assert.fail(String.format("Failed to login: %s", errorMessage));
        }
        return new HomePage(browser, url);
    }
}
