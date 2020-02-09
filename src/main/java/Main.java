import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    private static final String LOGIN = "aldomozhirov@gmail.com";
    private static final String PASSWORD = "VrvCIDO#";
    private static final String LOCATION = "Wrocław";
    private static final String DATE_TO_BOOK = "2020-03-30";
    private static final ServiceType SERVICE_TYPE = ServiceType.WNIOSEK_O_LEGALIZACJĘ_POBYTU;

    public static void main(String[] args) throws Exception {

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.setExperimentalOption("detach", true);
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\aldom\\Desktop\\chromedriver.exe");
        WebDriver driver = new ChromeDriver(options);
        Date dateToBook = Utils.getDate(DATE_TO_BOOK);

        openReservationPage(driver);

        login(driver, LOGIN, PASSWORD);

        selectLocation(driver, LOCATION);

        openTermsPageForService(driver, SERVICE_TYPE);

        selectMonth(driver, Utils.getMonthNumber(dateToBook));

        while (true) {
            List<WebElement> terms;
            while ((terms = getTermsByDate(driver, DATE_TO_BOOK)).isEmpty()) {
                System.out.println("No available dates");
            }

            String timeTermsString = terms.stream().map(e -> e.findElement(By.tagName("a")).getText())
                    .collect(Collectors.joining(", "));
            System.out.println("Available time terms: " + timeTermsString);

            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeAsyncScript("lock", "2020-02-20 13:00:00");
            Thread.sleep(1000);
            List<LogEntry> logEntries = driver.manage().logs().get("browser").getAll();
            for(LogEntry l : logEntries) {
                System.out.println(l.getMessage());
            }

            return;
            //terms.get(0).click();
            //Thread.sleep(10000);
        }

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

    private static void openTermsPageForService(WebDriver driver, ServiceType service) {
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

    private static List<WebElement> getTermsByDate(WebDriver driver, String date) {
        WebDriverWait wait = new WebDriverWait(driver, 10000);
        driver.findElement(By.xpath(String.format("//td[contains(@id, '%s')]", date))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("dateContent")));
        WebElement dateContent = driver.findElement(By.id("dateContent"));
        return dateContent.findElement(By.className("smartColumns")).findElements(By.tagName("li"));
    }

    private static void waitForPageToLoad(WebDriver driver) {
        ExpectedCondition<Boolean> pageLoadCondition = driver1 -> ((JavascriptExecutor) driver1).executeScript("return document.readyState").equals("complete");
        WebDriverWait wait = new WebDriverWait(driver, 30);
        wait.until(pageLoadCondition);
    }

}
