package browser;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.time.temporal.ChronoUnit.SECONDS;

public class Browser implements AutoCloseable {
    private static final Duration TIME_OUT_IN_SECONDS = Duration.ofSeconds(30);
    private static final Duration SHORT_TIME_OUT_IN_SECONDS = Duration.ofSeconds(5);
    private final WebDriver driver;

    public Browser(String browser) {
        if ("firefox".equals(browser)) {
            WebDriverManager.firefoxdriver().setup();
            driver = new FirefoxDriver();
        } else {
            WebDriverManager.chromedriver().setup();
            driver = new ChromeDriver();
        }
        driver.manage().window().maximize();
    }

    private WebElement findElementWithVisibilityOfElement(String xpath) {
        WebDriverWait wait = new WebDriverWait(driver, TIME_OUT_IN_SECONDS);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
    }

    private WebElement findElementWithClickAbilityOfElement(String xpath) {
        WebDriverWait wait = new WebDriverWait(driver, TIME_OUT_IN_SECONDS);
        return wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
    }

    public boolean visibilityOfElement(String xpath) {
        WebDriverWait wait = new WebDriverWait(driver, SHORT_TIME_OUT_IN_SECONDS);
        boolean result = true;
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
        } catch (TimeoutException e) {
            result = false;
        }
        return result;
    }

    public boolean invisibilityOfElement(String xpath) {
        WebDriverWait wait = new WebDriverWait(driver, SHORT_TIME_OUT_IN_SECONDS);
        boolean result = true;
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(xpath)));
        } catch (TimeoutException e) {
            result = false;
        }
        return result;
    }

    public void waitForVisibilityOfElement(String xpath) {
        findElementWithVisibilityOfElement(xpath);
    }

    public void waitForInvisibilityOfElement(String xpath) {
        WebDriverWait wait = new WebDriverWait(driver, TIME_OUT_IN_SECONDS);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(xpath)));
    }

    public void waitForClickAbilityOfElement(String xpath) {
        findElementWithClickAbilityOfElement(xpath);
    }


    public void sendKeys(String xpath, String text) {
        WebElement e = findElementWithVisibilityOfElement(xpath);
        e.sendKeys(text);
    }

    public void sendKeysWithClear(String xpath, String text) {
        clear(xpath);
        sendKeys(xpath, text);
    }

    public void clear(String xpath) {
        WebElement e = findElementWithVisibilityOfElement(xpath);
        e.clear();
    }

    public void sendKeys(String xpath, Keys key) {
        WebElement e = findElementWithVisibilityOfElement(xpath);
        e.sendKeys(key);
    }

    public void click(String xpath) {
        WebElement e = findElementWithClickAbilityOfElement(xpath);
        try {
            e.click();
        } catch (StaleElementReferenceException | ElementClickInterceptedException ignored) {
            moveToElement(xpath);
            WebElement again = findElementWithClickAbilityOfElement(xpath);
            again.click();
        }
    }

    public void scrollIntoView(String xpath) {
        WebElement e = driver.findElement(By.xpath(xpath));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView(true);", e);

    }

    public String getText(String xpath) {
        WebElement e = findElementWithVisibilityOfElement(xpath);
        return e.getText();
    }

    public void get(String url) {
        driver.get(url);
    }

    public void quit() {
        driver.quit();
    }

    public File getScreenshot() {
        TakesScreenshot screenshotDriver = (TakesScreenshot) driver;
        File screenshotFile = screenshotDriver.getScreenshotAs(OutputType.FILE);
        return screenshotFile;
    }

    public ArrayList<String> getAttributeOfElements(String xpath, String attribute) {
        waitForVisibilityOfElement(xpath);
        ArrayList<String> results = new ArrayList<>();
        for (WebElement e : driver.findElements(By.xpath(xpath))) {
            String value;
            if ("".equals(attribute)) {
                value = e.getText();
            } else {
                value = e.getAttribute(attribute);
            }
            results.add(value);
        }
        return results;
    }

    public void selectByVisibleText(String xpath, String text) {
        WebElement element = findElementWithClickAbilityOfElement(xpath);
        Select select = new Select(element);
        select.selectByVisibleText(text);
    }

    public void selectByValue(String xpath, String value) {
        WebElement element = findElementWithClickAbilityOfElement(xpath);
        Select select = new Select(element);
        select.deselectByValue(value);
    }

    public void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        quit();
    }

    public <SubElementsType extends Enum<SubElementsType> & SubElements> List<HashMap<SubElementsType, String>>
    getAttributesForSubElements(String xpathForElements, Class<SubElementsType> subElementsClass, String attribute) {
        SubElementsType[] enumConstants = subElementsClass.getEnumConstants();
        List<HashMap<SubElementsType, String>> results = new ArrayList<>();
        for (WebElement element : driver.findElements(By.xpath(xpathForElements))) {
            HashMap<SubElementsType, String> subElements = new HashMap<>();
            for (SubElementsType enumConstant : enumConstants) {
                String relativeXpath = enumConstant.getRelativeXpath();
                WebElement subElement = element.findElement(By.xpath(relativeXpath));
                String attributeValue;
                if ("".equals(attribute)) {
                    attributeValue = subElement.getText();
                } else {
                    attributeValue = subElement.getAttribute(attribute);
                }
                subElements.put(enumConstant, attributeValue);
            }
            results.add(subElements);
        }
        return results;
    }

    public void switchToFrame(String xpath) {
        WebElement e = findElementWithVisibilityOfElement(xpath);
        driver.switchTo().frame(e);
    }

    public void switchToParentFrame() {
        driver.switchTo().parentFrame();
    }

    public void switchToDefaultContent() {
        driver.switchTo().defaultContent();
    }

    public void waitForReadyStateComplete() {
        LocalTime startTime = LocalTime.now();
        while (SECONDS.between(LocalTime.now(), startTime) <= 20) {
            if (((JavascriptExecutor) driver).executeScript("return document.readyState").toString().equals("complete")) {
                return;
            }
        }
    }

    public void moveToElement(String xpath) {
        WebElement element = driver.findElement(By.xpath(xpath));
        new Actions(driver)
                .moveToElement(element)
                .perform();
    }
}