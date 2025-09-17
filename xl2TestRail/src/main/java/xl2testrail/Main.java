package xl2testrail;

import browser.Browser;
import pageobjects.FolderPage;
import pageobjects.HomePage;
import pageobjects.NewCasePage;
import pageobjects.TestRailPage;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileReader("configuration.properties"));
        final int folderNum = Integer.parseInt((String) properties.get("folderNum"));
        final String section = (String) properties.get("section");
        final String type = (String) properties.get("type");
        final String refs = (String) properties.get("refs");
        final String device = (String) properties.get("device");
        final String groups = (String) properties.get("groups");
        final boolean isAutomated = Boolean.parseBoolean((String) properties.get("isAutomated"));
        final String user = (String) properties.get("username");
        final String passwd = (String) properties.get("password");
        final String host = (String) properties.get("host");
        final String browserName = (String) properties.get("browser");

        List<Case> cases = TestCasesParser.parse("tcs.xlsx");
        System.out.println(cases);

        try (Browser browser = new Browser(browserName)) {
            TestRailPage testRailPage = new TestRailPage(browser, host);
            HomePage homePage = testRailPage.login(user, passwd);
            NewCasePage newCasePage = homePage.addCaseToFolder(folderNum);
            for (Case aCase : cases) {
                /*FolderPage folderPage = homePage.openFolder("/index.php?/suites/view/12345&group_by=cases:section_id&group_order=asc&display_deleted_cases=0");
                NewCasePage newCasePage = folderPage.openCaseWithTitle(aCase.getTitle());
                newCasePage.edit();*/
                newCasePage.createNewCase(aCase, section, type, refs, device, groups, isAutomated);
            }
        }
    }
}
