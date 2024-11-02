package pages.sharepoint;

import models.TimeEntry;
import org.openqa.selenium.Keys;
import pages.Page;
import utils.Browser;

public class TimeEntryPage extends Page {

    private static final String ISSUE = "//article//form//label[text()='Issue']/following-sibling::div[1]//input[@type!='hidden']";
    private static final String ACTIVITY = "//article//form//label[text()='Activity *']/following-sibling::div[1]//input[@type!='hidden']";
    private static final String DATE = "//input[@name=\"applicableDate\"]";
    private static final String HOURS = "//input[@name=\"hours\"]";
    private static final String SUMMARY = "//textarea[@name=\"comments\"]";
    private static final String SUBMIT = "//button[@type=\"submit\"]";

    //static final String //div[./span=01]
    public TimeEntryPage(Browser browser) {
        super(browser);
        browser.waitForClickAbilityOfElement(SUBMIT);
    }

    public boolean createEntry(String issue, TimeEntry entry) {
        handleCombo(ISSUE, issue);
        handleCombo(ACTIVITY, entry.activity().getDisplayedName());
        browser.sendKeys(DATE, String.format("%02d", entry.date().getDayOfMonth()));
        browser.sendKeysWithClear(HOURS, entry.hours().toString());
        browser.sendKeys(SUMMARY, entry.description());
        System.out.println("CHECK MANUALLY NOW!");
        browser.sleep(20000);
        browser.click(SUBMIT);
        System.out.println("Submitted");
        if (!browser.getText(SUMMARY).isEmpty()) {
            browser.click(SUBMIT);
            System.out.println("Submitted Again");
        }
        browser.sleep(500);
        return true;
    }

    private void handleCombo(String xpath, String text) {
        browser.click(xpath + "/..");
        browser.sendKeysWithClear(xpath, text);
        browser.sendKeys(xpath, Keys.TAB);
    }
}
