package com.highspring.shopping.config;

import com.highspring.shopping.entity.*;
import com.highspring.shopping.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final TaxRepository taxRepository;
    private final ItemRepository itemRepository;

    @Override
    public void run(String... args) {
        Category electronics = new Category();
        electronics.setName("Electronics");
        categoryRepository.save(electronics);

        Category food = new Category();
        food.setName("Food");
        categoryRepository.save(food);

        Tax gst = new Tax();
        gst.setName("GST");
        gst.setPercent(new BigDecimal("10.00"));
        taxRepository.save(gst);

        Tax vat = new Tax();
        vat.setName("VAT");
        vat.setPercent(new BigDecimal("5.00"));
        taxRepository.save(vat);

        Item laptop = new Item();
        laptop.setName("Laptop");
        laptop.setUnitPrice(new BigDecimal("999.99"));
        laptop.setTaxes(Set.of(gst));
        laptop.setCategories(Set.of(electronics));
        itemRepository.save(laptop);

        Item phone = new Item();
        phone.setName("Smartphone");
        phone.setUnitPrice(new BigDecimal("599.99"));
        phone.setTaxes(Set.of(gst, vat));
        phone.setCategories(Set.of(electronics));
        itemRepository.save(phone);

        Item apple = new Item();
        apple.setName("Apple");
        apple.setUnitPrice(new BigDecimal("0.99"));
        apple.setCategories(Set.of(food));
        itemRepository.save(apple);

        Item bread = new Item();
        bread.setName("Bread");
        bread.setUnitPrice(new BigDecimal("2.49"));
        bread.setTaxes(Set.of(vat));
        bread.setCategories(Set.of(food));
        itemRepository.save(bread);
    }
}
