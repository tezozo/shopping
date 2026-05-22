package com.highspring.shopping.selenium;

import com.highspring.shopping.entity.Category;
import com.highspring.shopping.entity.Item;
import com.highspring.shopping.entity.Tax;
import com.highspring.shopping.repository.CategoryRepository;
import com.highspring.shopping.repository.ItemRepository;
import com.highspring.shopping.repository.ShoppingCartRepository;
import com.highspring.shopping.repository.TaxRepository;
import com.highspring.shopping.selenium.page.CartDetailPage;
import com.highspring.shopping.selenium.page.CartListPage;
import com.highspring.shopping.selenium.page.OwnerInputPage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("selenium")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ShoppingCartSeleniumTest {

    @LocalServerPort int port;
    @Autowired CategoryRepository categoryRepository;
    @Autowired TaxRepository taxRepository;
    @Autowired ItemRepository itemRepository;
    @Autowired ShoppingCartRepository cartRepository;

    private WebDriver driver;
    private String baseUrl;

    @BeforeAll
    void seedReferenceData() {
        Category electronics = new Category(); electronics.setName("Electronics");
        Category food        = new Category(); food.setName("Food");
        Category kitchen     = new Category(); kitchen.setName("Kitchen");
        categoryRepository.saveAll(List.of(electronics, food, kitchen));

        Tax gst    = new Tax(); gst.setName("GST");    gst.setPercent(new BigDecimal("5.00"));
        Tax exempt = new Tax(); exempt.setName("Exempt"); exempt.setPercent(new BigDecimal("0.00"));
        taxRepository.saveAll(List.of(gst, exempt));

        Item laptop = new Item();
        laptop.setName("Laptop"); laptop.setUnitPrice(new BigDecimal("999.99"));
        laptop.setTaxes(Set.of(gst)); laptop.setCategories(Set.of(electronics));

        Item apple = new Item();
        apple.setName("Apple"); apple.setUnitPrice(new BigDecimal("0.99"));
        apple.setTaxes(Set.of(exempt)); apple.setCategories(Set.of(food));

        Item pan = new Item();
        pan.setName("Pan"); pan.setUnitPrice(new BigDecimal("18.99"));
        pan.setTaxes(Set.of(gst)); pan.setCategories(Set.of(kitchen));

        itemRepository.saveAll(List.of(laptop, apple, pan));
    }

    @BeforeEach
    void startBrowser() {
        baseUrl = "http://localhost:" + port;
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage",
            "--disable-gpu", "--window-size=1280,800");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));

        // make sure each test starts with a clean slate (no carts, no localStorage)
        cartRepository.deleteAll();
        driver.get(baseUrl);
        ((JavascriptExecutor) driver).executeScript("window.localStorage.clear();");
        driver.navigate().refresh();
    }

    @AfterEach
    void stopBrowser() {
        if (driver != null) driver.quit();
    }

    @AfterAll
    void cleanUp() {
        cartRepository.deleteAll();
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    @Test
    void loggingInNavigatesToCartList() {
        CartListPage list = new OwnerInputPage(driver).waitUntilVisible().loginAs("alice");

        assertThat(list.getCartNames()).isEmpty();
    }

    @Test
    void newlyCreatedCartAppearsInList() {
        CartListPage list = new OwnerInputPage(driver).waitUntilVisible()
            .loginAs("alice")
            .createCart("Weekend Shopping");

        assertThat(list.getCartNames()).containsExactly("Weekend Shopping");
    }

    @Test
    void deletedCartDisappearsFromList() {
        CartListPage list = new OwnerInputPage(driver).waitUntilVisible()
            .loginAs("alice")
            .createCart("To Be Deleted")
            .createCart("Keep Me");

        list.deleteCart("To Be Deleted");

        assertThat(list.getCartNames()).containsExactly("Keep Me");
    }

    @Test
    void openingCartShowsCartDetailWithSeededItems() {
        CartDetailPage detail = new OwnerInputPage(driver).waitUntilVisible()
            .loginAs("alice")
            .createCart("My Cart")
            .openCart("My Cart");

        // seeded 3 items should appear as available
        List<String> availableNames = driver.findElements(
                org.openqa.selenium.By.cssSelector(".item-card .item-name"))
            .stream().map(org.openqa.selenium.WebElement::getText).toList();

        assertThat(availableNames).containsExactlyInAnyOrder("Laptop", "Apple", "Pan");
        assertThat(detail.getCartItemNames()).isEmpty();
    }

    @Test
    void savingCartWithFewerThanThreeCategoriesShowsValidationError() {
        CartDetailPage detail = new OwnerInputPage(driver).waitUntilVisible()
            .loginAs("alice")
            .createCart("Bad Cart")
            .openCart("Bad Cart")
            .addAvailableItem("Laptop")   // Electronics only — 1 category
            .addAvailableItem("Apple")    // + Food — 2 categories total
            .clickSave();

        assertThat(detail.getSaveErrorText())
            .contains("three different categories");
    }

    @Test
    void savingCartWithThreeCategoriesSucceeds() {
        CartDetailPage detail = new OwnerInputPage(driver).waitUntilVisible()
            .loginAs("alice")
            .createCart("Good Cart")
            .openCart("Good Cart")
            .addAvailableItem("Laptop")   // Electronics
            .addAvailableItem("Apple")    // Food
            .addAvailableItem("Pan")      // Kitchen — 3 categories
            .clickSave()
            .waitForSaveComplete();

        assertThat(detail.hasSaveError()).isFalse();
        assertThat(detail.getCartItemNames())
            .containsExactlyInAnyOrder("Laptop", "Apple", "Pan");
    }
}
