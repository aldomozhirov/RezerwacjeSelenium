import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Main {

    private static final String LOGIN = "alenabaranova948@gmail.com";
    private static final String PASSWORD = "";
    private static final String LOCATION = "Wrocław";
    private static final String DATE_TO_BOOK = "2020-02-25";
    private static final ServiceType SERVICE_TYPE = ServiceType.LP_I_DEPARTMENT;
    private static final String NAME_AND_SURNAME = "Alena Domozhirova";
    private static final String DATE_OF_BIRTH = "1996-01-22";
    private static final String PHONE_NUMBER = "888719445";
    private static final String SUBMISSION_DATE = "2019-03-14";
    private static final String REFERENCE_NUMBER = "32285186";
    private static final String CHROMEDRIVER_PATH = "/Users/alena/Documents/chromedriver";

    private static LoggingPreferences getLoggingPrefs() {
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.PERFORMANCE, Level.INFO);
        logPrefs.enable(LogType.BROWSER, Level.INFO);
        logPrefs.enable(LogType.DRIVER, Level.INFO);
        return logPrefs;
    }

    private static ChromeOptions getChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.setCapability("goog:loggingPrefs", getLoggingPrefs());
        return options;
    }

    private static Map<String,String> getFormData() {
        Map<String, String> formData = new HashMap<>();
        switch (SERVICE_TYPE) {
            case REQUEST_FOR_LEGALIZATION_OF_RESIDENCE:
                // TODO Add dorm data for application
                break;
            case LP_I_DEPARTMENT:
            case LP_II_DEPARTMENT:
            case HEAD_OF_DEPARTMENT:
                formData.put("nazwisko i imię", NAME_AND_SURNAME);
                formData.put("data urodzenia/Date of birth", DATE_OF_BIRTH);
                formData.put("NUMER TELEFONU KONTAKTOWEGO", PHONE_NUMBER);
                formData.put("sygnatura sprawy lub nazwisko inspektora prowadzącego postępowanie", REFERENCE_NUMBER);
                formData.put("wpisz datę złożenia wniosku (przesłania drogą pocztową)", SUBMISSION_DATE);
            break;
        }
        return formData;
    }

    public static void main(String[] args) throws Exception {

        System.setProperty("webdriver.chrome.driver", CHROMEDRIVER_PATH);

        WebDriver driver = new ChromeDriver(getChromeOptions());

        openReservationPage(driver);

        login(driver, LOGIN, PASSWORD);

        selectLocation(driver, LOCATION);

        openTermsPage(driver, SERVICE_TYPE);

        selectMonth(driver, Utils.getMonthNumber(Utils.getDate(DATE_TO_BOOK)));

        book(driver, DATE_TO_BOOK);

        fillBookingForm(driver, getFormData());

    }

    private static void openReservationPage(WebDriver driver) {
        driver.get(Constants.RESERVATION_PAGE_URL);
    }

    private static void login(WebDriver driver, String login, String password) {
        WebElement loginElement = driver.findElement(By.name("data[User][email]"));
        WebElement passwordElement = driver.findElement(By.name("data[User][password]"));
        loginElement.sendKeys(login);
        passwordElement.sendKeys(password);
        passwordElement.submit();
    }

    private static void selectLocation(WebDriver driver, String location) throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, 10000);
        WebElement dropdown = driver.findElement(By.className("hide-on-med-and-down"));
        dropdown.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ul.dropdown-content.active")));
        Thread.sleep(500);
        List<WebElement> locations = driver.findElements(By.xpath("//ul[@class='dropdown-content active']/li"));
        for (WebElement loc : locations) {
            if (loc.getText().equals(location)) {
                loc.click();
                break;
            }
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(String.format(
                "//a[contains(@class, 'hide-on-med-and-down')]/span/b[contains(text(), '%s')]",
                location
        ))));
    }

    private static void openTermsPage(WebDriver driver, ServiceType service) {
        String url = String.format(Constants.TERMS_PAGE_URL_TEMPLATE, service.val1, service.val2);
        driver.get(url);
    }

    private static void selectMonth(WebDriver driver, int month) throws InterruptedException {
        WebElement next = driver.findElement(By.cssSelector("i.fa.fa-chevron-circle-right"));
        while (!driver
                .findElement(By.className("calendar-month-header"))
                .getText().contains(Constants.MONTHS.get(month))) {
            next.click();
            // TODO wait until calendar loads
            Thread.sleep(1000);
        }
    }

    private static void book(WebDriver driver, String date) throws InterruptedException {
        boolean termLocked = false;
        List<WebElement> terms = null;
        while (!termLocked) {
            // Click the date until terms will be found
            while (terms == null || terms.isEmpty()) {
                clickDate(driver, date);
                terms = getAvailableTerms(driver);
                if (terms.isEmpty()) {
                    System.out.println("No available terms");
                }
            }
            // Try to lock one of available terms
            int termNum = 0;
            String termString = "";
            while (!terms.isEmpty()) {
                // Get the list of available terms strings
                List<String> availableTimeTermsStringsList = terms.stream()
                        .map(e -> e.findElement(By.tagName("a")).getText())
                        .collect(Collectors.toList());
                System.out.println("Available terms: " + String.join(", ", availableTimeTermsStringsList));
                // Determine which term to lock
                if (availableTimeTermsStringsList.size() - 1 < termNum) {
                    termNum = 0;
                }
                if (availableTimeTermsStringsList.size() > 1) {
                    if (availableTimeTermsStringsList.get(termNum).equals(termString)) {
                        termNum++;
                        if (termNum == availableTimeTermsStringsList.size()) {
                            termNum = 0;
                        }
                    }
                }
                // Attempt to lock the term
                WebElement termToLock = terms.get(termNum);
                termString = availableTimeTermsStringsList.get(termNum);
                System.out.println("Trying to lock the term " + termString);
                termToLock.click();
                if (waitForLockResult(driver, 120000)) {
                    termLocked = true;
                    break;
                } else {
                    System.err.println("Failed to lock the term " + termString);
                    terms = getAvailableTerms(driver);
                }
            }
        }
    }

    private static void fillBookingForm(WebDriver driver, Map<String, String> formData) {
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            try {
                WebElement element = driver.findElement(By.name(entry.getKey()));
                element.sendKeys(entry.getValue());
            } catch (Exception e) {
                System.err.println(String.format("Cannot fill the field '%s'", entry.getKey()));
            }
        }
    }

    private static void clickDate(WebDriver driver, String date) {
        driver.findElement(By.xpath(String.format("//td[contains(@id, '%s')]", date))).click();
    }

    private static List<WebElement> getAvailableTerms(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, 10000);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("dateContent")));
        WebElement dateContent = driver.findElement(By.id("dateContent"));
        return dateContent.findElement(By.className("smartColumns")).findElements(By.tagName("li"));
    }

    private static boolean waitForLockResult(WebDriver driver, long timeout) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeout) {
            LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);
            for (LogEntry entry : logEntries) {
                String message = entry.getMessage();
                if (message.contains("OK")) {
                    return true;
                } else if (message.contains("FAIL")) {
                    return false;
                }
                Thread.sleep(100);
            }
        }
        return false;
    }


}
