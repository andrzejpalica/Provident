package com.automationpractice.task2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PageTest {
    private static WebDriver driver;
    private static Actions action;
    private static WebDriverWait wait;

    @BeforeEach
    void doBeforeTest() {
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().window().setSize(new Dimension(2062, 1047));
        driver.manage().window().setPosition(new Point(-7, 1039));
        driver.get("http://automationpractice.com/index.php");
    }

    @AfterEach
    void doAfterTest() {
        driver.close();
        driver.quit();
    }

    @Test
    void test1() {
        int numberOfProductToAdd = 3;

        double productsCost = 0.0;
        double cartProductsTotal = 0.0;
        double shippingCosts = 0.0;
        double cardTotal = 0.0;

        for (int i = 1; i < numberOfProductToAdd + 1; i++) {
            // Find product n on main page and move to it for display 'Add to card' button
            action = new Actions(driver);
            action.moveToElement(findElementByXPath(".//ul[@id='homefeatured']//li[" + i + "]"));
            action.perform();

            // Find product price and get it
            double productPrice = parsePrice(findElementByXPath(".//ul[@id='homefeatured']//li[" + i + "]//div[@class='product-image-container']//span[@itemprop='price']"));
            productsCost += productPrice;

            // Find 'Add to card' button and click on it
            findElementByXPath(".//ul[@id='homefeatured']//li[" + i + "]//a[@data-id-product='" + i + "']").click();

            // Find 'Continue shopping' button and click on it
            wait = new WebDriverWait(driver, 30);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(".//span[@title='Continue shopping']")));
            // In last iteration find 'Total' value in card and get it
            if (i == numberOfProductToAdd) {
                cartProductsTotal = parsePrice(findElementByXPath("//span[@class='ajax_block_products_total']"));
                shippingCosts = parsePrice(findElementByXPath("//span[@class='ajax_cart_shipping_cost']"));
                cardTotal = parsePrice(findElementByXPath("//span[@class='ajax_block_cart_total']"));
            }
            findElementByXPath(".//span[@title='Continue shopping']").click();

        }
        // Check if values are proper
        Assertions.assertEquals(productsCost, cartProductsTotal, "Wrong 'Total products' value at cart");
        Assertions.assertEquals((productsCost + shippingCosts), cardTotal, "Wrong 'Total' value at cart");
    }

    @Test
    void test2() {
        findElementByXPath(".//div[@id='block_top_menu']/ul/li[2]/a").click();
        WebDriverWait wait = new WebDriverWait(driver, 30);
        wait.until(ExpectedConditions.visibilityOfAllElements(driver.findElements(By.xpath(".//div[@id='center_column']/ul/li[*]"))));

        String allProductPricesSpan = ".//div[@id='center_column']/ul/li[*]//div[@class='right-block']//span[@itemprop='price']";
        List<WebElement> elements = driver.findElements(By.xpath(allProductPricesSpan));
        ArrayList<Double> expectedProductsOrderAfterAscendingSort = new ArrayList<>();
        for (WebElement element : elements) {
            expectedProductsOrderAfterAscendingSort.add(parsePrice(element));
        }
        Collections.sort(expectedProductsOrderAfterAscendingSort);

        Select selectSorting = new Select(findElementByXPath(".//select[@id='selectProductSort']"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//option")));
        selectSorting.selectByValue("price:asc");

        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        elements = driver.findElements(By.xpath(allProductPricesSpan));
        ArrayList<Double> currentProductsOrderAfterAscendingSort = new ArrayList<>();

        for (WebElement element : elements) {
            currentProductsOrderAfterAscendingSort.add(parsePrice(element));
        }

        Assertions.assertEquals(expectedProductsOrderAfterAscendingSort, currentProductsOrderAfterAscendingSort, "Sorting by ascending price does not work!");

    }

    double parsePrice(WebElement webElement) {
        String sPrice = webElement.getText().substring(1);
        return Double.parseDouble(sPrice);
    }

    WebElement findElementByXPath(String xPath) {
        return driver.findElement(By.xpath(xPath));
    }

}
