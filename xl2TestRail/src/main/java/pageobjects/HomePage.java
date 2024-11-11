package pageobjects;

import browser.Browser;

public class HomePage extends Page {
    private final String host;
    private final String addCaseToFolderToFormat = "index.php?/cases/add/%d";

    public HomePage(Browser browser, String host) {
        super(browser);
        this.host = host;
    }

    public NewCasePage addCaseToFolder(int folderNum) {
        browser.get(host + String.format(addCaseToFolderToFormat, folderNum));
        return new NewCasePage(browser);
    }
}
