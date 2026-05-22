package com.highspring.shopping.selenium.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class CartListPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public CartListPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    public CartListPage waitUntilVisible() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cart-list")));
        return this;
    }

    public CartListPage createCart(String name) {
        driver.findElement(By.cssSelector(".create-form input")).sendKeys(name);
        driver.findElement(By.cssSelector(".create-form button")).click();
        wait.until(d -> getCartNames().contains(name));
        return this;
    }

    public List<String> getCartNames() {
        return driver.findElements(By.cssSelector(".cart-card-info strong"))
            .stream().map(WebElement::getText).toList();
    }

    public CartDetailPage openCart(String name) {
        cartCardByName(name)
            .findElement(By.xpath(".//button[normalize-space()='Open']"))
            .click();
        return new CartDetailPage(driver).waitUntilVisible();
    }

    public CartListPage deleteCart(String name) {
        cartCardByName(name).findElement(By.cssSelector("button.danger")).click();
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();
        wait.until(d -> !getCartNames().contains(name));
        return this;
    }

    private WebElement cartCardByName(String name) {
        return driver.findElements(By.cssSelector(".cart-card")).stream()
            .filter(card -> card.findElement(By.cssSelector(".cart-card-info strong")).getText().equals(name))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Cart not found: " + name));
    }
}
