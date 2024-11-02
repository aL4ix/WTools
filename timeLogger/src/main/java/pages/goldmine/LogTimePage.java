package pages.goldmine;

import enums.Activity;
import org.junit.Assert;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.Keys;
import pages.Page;
import utils.Browser;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LogTimePage extends Page {

    public static final String SPENT_TIME_H2 = "//h2[text()='Spent time']";
    public static final String DATE_INPUT = "//input[@id='time_entry_spent_on']";
    public static final String BODY = "//body";
    public static final String HOURS_INPUT = "//input[@id='time_entry_hours']";
    public static final String COMMENTS_INPUT = "//input[@id='time_entry_comments']";
    public static final String ACTIVITY_SELECT = "//select[@id='time_entry_activity_id']";
    public static final String CREATE_BUTTON = "//input[@name='commit']";
    public static final String FLASH_NOTICE = "//div[@id='flash_notice']";

    public LogTimePage(Browser browser) {
        super(browser);
        browser.waitForVisibilityOfElement(SPENT_TIME_H2);
        browser.sendKeys(BODY, Keys.HOME);
    }

    public SpentTimePage logTime(LocalDate date, BigDecimal hours, String comment, Activity benchActivity) {
        int tries = 0;
        do {
            try {
                browser.click(DATE_INPUT);
            } catch (ElementClickInterceptedException ignored) {
            }
            browser.sendKeys(BODY, Keys.HOME);
        } while (++tries < 3);
        browser.sendKeys(DATE_INPUT, Keys.LEFT);
        browser.sendKeys(DATE_INPUT, Keys.LEFT);
        browser.sendKeys(DATE_INPUT, Keys.LEFT);
        String date1 = String.format("%02d", date.getMonthValue());
        browser.sendKeys(DATE_INPUT, date1);
        String date2 = String.format("%02d", date.getDayOfMonth());
        browser.sendKeys(DATE_INPUT, date2);
        String date3 = String.format("%04d", date.getYear());
        browser.sendKeys(DATE_INPUT, date3);
        browser.sendKeys(HOURS_INPUT, String.valueOf(hours));
        browser.sendKeys(COMMENTS_INPUT, comment);
        browser.selectByVisibleText(ACTIVITY_SELECT, benchActivity.getDisplayedName());
        //ui.sleep(5000);
        browser.click(CREATE_BUTTON);
        browser.waitForVisibilityOfElement(FLASH_NOTICE);
        Assert.assertEquals(browser.getText(FLASH_NOTICE), "Successful creation.");
        return new SpentTimePage(browser);
    }
}
