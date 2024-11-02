package utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;

public class Browser implements AutoCloseable {
    public static final Duration TIME_OUT_IN_SECONDS = Duration.ofSeconds(30);
    public static final Duration SHORT_TIME_OUT_IN_SECONDS = Duration.ofSeconds(5);
    WebDriver driver;

    public Browser(String browser) {
        System.setProperty("webdriver.http.factory", "jdk-http-client");
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

    public void waitForClickAbilityOfElement(String xpath) {
        findElementWithClickAbilityOfElement(xpath);
    }


    public void sendKeys(String xpath, String text) {
        WebElement e = findElementWithVisibilityOfElement(xpath);
        e.sendKeys(text);
    }

    public void sendKeysWithClear(String xpath, String text) {
        WebElement e = findElementWithVisibilityOfElement(xpath);
        e.clear();
        e.sendKeys(text);
    }

    public void sendKeys(String xpath, Keys key) {
        WebElement e = findElementWithVisibilityOfElement(xpath);
        e.sendKeys(key);
    }

    public void click(String xpath) {
        WebElement e = findElementWithClickAbilityOfElement(xpath);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView(true);", e);
        e.click();
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

    public void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public WebDriver getDriver() {
        return driver;
    }

    @Override
    public void close() {
        quit();
    }
}