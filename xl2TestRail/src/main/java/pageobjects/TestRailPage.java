package pageobjects;

import browser.Browser;

public class TestRailPage extends Page {
    private final String host;
    private static String userInput = "//input[@id='name']";
    private static String passwdInput = "//input[@id='password']";
    private static String loginButton = "//button[@id='button_primary']";

    public TestRailPage(Browser browser, String host) {
        super(browser);
        this.host = host;
    }

    public HomePage login(String user, String passwd) {
        browser.get(host);
        browser.sendKeys(userInput, user);
        browser.sendKeys(passwdInput, passwd);
        browser.click(loginButton);
        return new HomePage(browser, host);
    }
}
