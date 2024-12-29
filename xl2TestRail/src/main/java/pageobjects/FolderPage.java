package pageobjects;

import browser.Browser;

public class FolderPage extends Page{
    private static String TITLE_F = "//a[.//span[@data-testid='sectionCaseTitle']=\"%s\"]";

    public FolderPage(Browser browser) {
        super(browser);
    }

    public NewCasePage openCaseWithTitle(String title) {
        String formatted = TITLE_F.formatted(title);
        System.out.println(formatted);
        browser.click(formatted);
        return new NewCasePage(browser);
    }
}
