package browser;

import org.openqa.selenium.ElementClickInterceptedException;

public class TestRailUtils {

    private static final String SELECTOR_OR_COMBO_DIV_BY_ID = "//div[@id='%s']";
    private static final String SELECTOR_PLUS_LINK = "%s/a";
    private static final String SELECTOR_INTERNAL_OPTION = "%s//li[contains(text(),'%s')]";
    private static final String COMBO_UL = "%s//ul[@class='chzn-choices']";
    private static final String COMBO_INTERNAL_LI = "%s//li[text()='%s']";
    private static final String STEP_ROW_TR = "//table[@id='custom_steps_separated_table']//tr[%d]";
    private static final String ADD_FIRST_STEP_LINK = "//a[text()='Add the first step']";
    private static final String ADD_STEP_LINK = "%s//a[@class='addStep']";
    private static final String STEP_CONTENT_DIV = "%s/td[@class='step-content']//div[contains(@class,'step-text-content')]//div[@role='textbox']";
    private static final String STEP_EXPECTED_DIV = "%s/td[@class='step-content']//div[contains(@class,'step-text-expected')]//div[@role='textbox']";

    public static void selectByVisibleText(Browser browser, String id, String value) {
        String xpath = String.format(SELECTOR_OR_COMBO_DIV_BY_ID, id);
        String format = String.format(SELECTOR_PLUS_LINK, xpath);
        browser.moveToElement(xpath);
        browser.click(format);
        String rel = String.format(SELECTOR_INTERNAL_OPTION, xpath, value);
        browser.click(rel);
    }

    public static void comboByVisibleText(Browser browser, String id, String value) {
        if (value.isEmpty()) {
            return;
        }
        for (int retry = 0; retry < 3; retry++) {
            try {
                String xpath = String.format(SELECTOR_OR_COMBO_DIV_BY_ID, id);
                browser.click(String.format(COMBO_UL, xpath));
                String rel = String.format(COMBO_INTERNAL_LI, xpath, value);
                browser.click(rel);
                break;
            } catch (ElementClickInterceptedException ignored) {
            }
        }
    }

    private static String stepRow(int rowNum) {
        return String.format(STEP_ROW_TR, rowNum);
    }

    public static void addStep(Browser browser, int rowNum, String description, String expected) {
        if (rowNum == 0) {
            browser.click(ADD_FIRST_STEP_LINK);
        } else {
            String prevRow = stepRow(rowNum);
            String addStep = String.format(ADD_STEP_LINK, prevRow);
//      System.out.println(addStep);
            browser.click(addStep);
        }
        String row = stepRow(rowNum + 1);
        String stepContent = String.format(STEP_CONTENT_DIV, row);
        String stepExpected = String.format(STEP_EXPECTED_DIV, row);
//    System.out.println(stepContent);
//    System.out.println(stepExpected);
        browser.sendKeys(stepContent, description);
        browser.sendKeys(stepExpected, expected);
    }
}
