package pageobjects;

import browser.Browser;
import browser.TestRailUtils;
import xl2testrail.Case;
import xl2testrail.Step;

import java.util.Map.Entry;

public class NewCasePage extends Page {

    private static final String sectionId = "section_id_chzn";
    private static final String titleInput = "//input[@id='title']";
    private static final String templateId = "template_id_chzn";
    private static final String typeId = "type_id_chzn";
    private static final String refsInput = "//input[@name='refs']";
    private static final String automated = "//input[@id='custom_hbr_automated']";
    private static final String deviceCombo = "custom_hbr_device_container";
    private static final String preconditionsInput = "//div[@id='custom_preconds_display']";
    private static final String groupsCombo = "custom_testng_groups_container";
    private static final String acceptAndNextButton = "//button[@id='accept_and_next']";
    private static final String closeAnnouncement = "//button[@aria-label='Close']";
    private static final String successfullyCreatedTest = "//div[contains(@class,'message-success')]";
    private static final String TEST_CASE_STEPS = "Test Case (Steps)";
    private static final String EDIT = "//a[@data-testid='testCaseEditButton']";
    private static final String SAVE = "//button[@data-testid='addTestCaseButton']";


    public NewCasePage(Browser browser) {
        super(browser);
    }

    public NewCasePage createNewCase(Case aCase, String section, String type, String refs, String device, String groups, boolean isAutomated) {
        for (int retry = 0; retry < 3; retry++) {
            String title = aCase.getTitle();
            browser.getText(titleInput);
            browser.sendKeysWithClear(titleInput, title);
        }
        if (browser.visibilityOfElement(closeAnnouncement)) {
            browser.click(closeAnnouncement);
        }
        TestRailUtils.selectByVisibleText(browser, sectionId, section);
        TestRailUtils.selectByVisibleText(browser, templateId, TEST_CASE_STEPS);
        TestRailUtils.selectByVisibleText(browser, typeId, type);
        browser.sendKeys(refsInput, refs);
        if (browser.visibilityOfElement(closeAnnouncement)) {
            browser.click(closeAnnouncement);
        }
        if (isAutomated) {
            browser.click(automated);
        }
        TestRailUtils.comboByVisibleText(browser, deviceCombo, device);
        browser.sendKeys(preconditionsInput, aCase.getPreconditions());
        TestRailUtils.comboByVisibleText(browser, groupsCombo, groups);
        for (Entry<Integer, Step> entry : aCase.getSteps().entrySet()) {
            Step step = entry.getValue();
            TestRailUtils.addStep(browser, entry.getKey() - 1, step.description(), step.expected());
        }

        System.out.println("READY?");
        browser.sleep(5000);
//        browser.click(SAVE);
        browser.click(acceptAndNextButton);
        System.out.println("SUBMITTED");
        browser.waitForReadyStateComplete();
        browser.waitForVisibilityOfElement(successfullyCreatedTest);
        browser.sleep(1000);

        return new NewCasePage(browser);
    }

    public void edit() {
        browser.click(EDIT);
    }
}
