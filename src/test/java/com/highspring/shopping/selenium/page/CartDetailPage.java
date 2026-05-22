package com.highspring.shopping.selenium.page;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class CartDetailPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public CartDetailPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    public CartDetailPage waitUntilVisible() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cart-detail")));
        return this;
    }

    public CartDetailPage addAvailableItem(String itemName) {
        WebElement card = driver.findElements(By.cssSelector(".item-card")).stream()
            .filter(c -> c.findElement(By.cssSelector(".item-name")).getText().equals(itemName))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Available item not found: " + itemName));
        card.findElement(By.cssSelector("button")).click();
        wait.until(d -> getCartItemNames().contains(itemName));
        return this;
    }

    public List<String> getCartItemNames() {
        return driver.findElements(By.cssSelector("table tbody tr td:first-child"))
            .stream().map(WebElement::getText).toList();
    }

    public CartDetailPage clickSave() {
        driver.findElement(
            By.xpath("//div[contains(@class,'cart-header')]//button[not(contains(@class,'secondary'))]")
        ).click();
        return this;
    }

    public String getSaveErrorText() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".save-error"))).getText();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean hasSaveError() {
        try {
            return driver.findElement(By.cssSelector(".save-error")).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public CartDetailPage waitForSaveComplete() {
        wait.until(d -> {
            WebElement btn = d.findElement(
                By.xpath("//div[contains(@class,'cart-header')]//button[not(contains(@class,'secondary'))]"));
            return btn.getText().equals("Save Cart");
        });
        return this;
    }
}
