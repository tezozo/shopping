package com.highspring.shopping.config;

import com.highspring.shopping.entity.*;
import com.highspring.shopping.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final TaxRepository taxRepository;
    private final ItemRepository itemRepository;
    private final DiscountRepository discountRepository;
    private final CategoryDiscountRepository categoryDiscountRepository;
    private final ShoppingCartRepository shoppingCartRepository;

    @Override
    @Transactional
    public void run(String... args) {
        boolean requested = java.util.Arrays.asList(args).contains("--runDataInitializer");
        if (!requested) return;

        // clear existing data in dependency order
        shoppingCartRepository.deleteAll();
        categoryDiscountRepository.deleteAll();
        itemRepository.deleteAll();
        taxRepository.deleteAll();
        discountRepository.deleteAll();
        categoryRepository.deleteAll();

        // discounts
        Discount tenOffMay = new Discount();
        tenOffMay.setName("10% off May");
        tenOffMay.setPercent(new BigDecimal("10.00"));
        tenOffMay.setStart(ZonedDateTime.of(
                LocalDate.of(2026, 5, 1),
                LocalTime.MIDNIGHT,
                ZoneId.of("America/Montreal")));
        tenOffMay.setEnd(ZonedDateTime.of(
                LocalDate.of(2026, 5, 31),
                LocalTime.MIDNIGHT,
                ZoneId.of("America/Montreal")));
        tenOffMay.setCumulative(true);
        discountRepository.save(tenOffMay);

        Discount fiveOffPreSummer = new Discount();
        fiveOffPreSummer.setName("5% off pre-summer");
        fiveOffPreSummer.setPercent(new BigDecimal("5.00"));
        fiveOffPreSummer.setStart(ZonedDateTime.of(
                LocalDate.of(2026, 4, 1),
                LocalTime.MIDNIGHT,
                ZoneId.of("America/Montreal")));
        fiveOffPreSummer.setEnd(ZonedDateTime.of(
                LocalDate.of(2026, 6, 30),
                LocalTime.MIDNIGHT,
                ZoneId.of("America/Montreal")));
        fiveOffPreSummer.setCumulative(true);
        discountRepository.save(fiveOffPreSummer);

        Discount nineteenOffElectronics = new Discount();
        nineteenOffElectronics.setName("19% off Electronics");
        nineteenOffElectronics.setPercent(new BigDecimal("19.00"));
        nineteenOffElectronics.setStart(ZonedDateTime.of(
                LocalDate.of(2026, 1, 1),
                LocalTime.MIDNIGHT,
                ZoneId.of("America/Montreal")));
        nineteenOffElectronics.setEnd(ZonedDateTime.of(
                LocalDate.of(2026, 6, 30),
                LocalTime.MIDNIGHT,
                ZoneId.of("America/Montreal")));
        nineteenOffElectronics.setCumulative(false);
        discountRepository.save(nineteenOffElectronics);

        // categories
        Category electronics = new Category();
        electronics.setName("Electronics");
        categoryRepository.save(electronics);

        Category food = new Category();
        food.setName("Food");
        categoryRepository.save(food);

        Category kitchen = new Category();
        kitchen.setName("Kitchen");
        categoryRepository.save(kitchen);

        Category hardware = new Category();
        hardware.setName("Hardware");
        categoryRepository.save(hardware);


        // category discounts
        CategoryDiscount categoryDiscount = new CategoryDiscount();
        categoryDiscount.setCategory(electronics);
        categoryDiscount.setDiscount(nineteenOffElectronics);
        categoryDiscountRepository.save(categoryDiscount);

        categoryDiscount = new CategoryDiscount();
        categoryDiscount.setCategory(electronics);
        categoryDiscount.setDiscount(tenOffMay);
        categoryDiscountRepository.save(categoryDiscount);

        categoryDiscount = new CategoryDiscount();
        categoryDiscount.setCategory(electronics);
        categoryDiscount.setDiscount(fiveOffPreSummer);
        categoryDiscountRepository.save(categoryDiscount);

        categoryDiscount = new CategoryDiscount();
        categoryDiscount.setCategory(hardware);
        categoryDiscount.setDiscount(tenOffMay);
        categoryDiscountRepository.save(categoryDiscount);

        categoryDiscount = new CategoryDiscount();
        categoryDiscount.setCategory(kitchen);
        categoryDiscount.setDiscount(fiveOffPreSummer);
        categoryDiscountRepository.save(categoryDiscount);

        categoryDiscount = new CategoryDiscount();
        categoryDiscount.setCategory(kitchen);
        categoryDiscount.setDiscount(tenOffMay);
        categoryDiscountRepository.save(categoryDiscount);

        // taxes
        Tax gst = new Tax();
        gst.setName("GST");
        gst.setPercent(new BigDecimal("5.00"));
        taxRepository.save(gst);

        Tax qst = new Tax();
        qst.setName("QST");
        qst.setPercent(new BigDecimal("9.975"));
        taxRepository.save(qst);

        Tax exempt = new Tax();
        exempt.setName("Exempt");
        exempt.setPercent(new BigDecimal("0.00"));
        taxRepository.save(exempt);

        // items
        // electronics
        Item laptop = new Item();
        laptop.setName("Laptop");
        laptop.setUnitPrice(new BigDecimal("999.99"));
        laptop.setTaxes(Set.of(gst,qst));
        laptop.setCategories(Set.of(electronics));
        itemRepository.save(laptop);

        Item phone = new Item();
        phone.setName("Smartphone");
        phone.setUnitPrice(new BigDecimal("599.99"));
        phone.setTaxes(Set.of(gst,qst));
        phone.setCategories(Set.of(electronics));
        itemRepository.save(phone);

        // food
        Item apple = new Item();
        apple.setName("Apple");
        apple.setUnitPrice(new BigDecimal("0.99"));
        apple.setTaxes(Set.of(exempt));
        apple.setCategories(Set.of(food));
        itemRepository.save(apple);

        Item bread = new Item();
        bread.setName("Bread");
        bread.setUnitPrice(new BigDecimal("2.49"));
        bread.setTaxes(Set.of(exempt));
        bread.setCategories(Set.of(food));
        itemRepository.save(bread);

        // kitchen
        Item pan = new Item();
        pan.setName("Pan");
        pan.setUnitPrice(new BigDecimal("18.99"));
        pan.setTaxes(Set.of(gst));
        pan.setCategories(Set.of(kitchen));
        itemRepository.save(pan);

        Item utensils = new Item();
        utensils.setName("Utensils");
        utensils.setUnitPrice(new BigDecimal("12.99"));
        utensils.setTaxes(Set.of(gst));
        utensils.setCategories(Set.of(kitchen));
        itemRepository.save(utensils);

        // hardware
        Item drill = new Item();
        drill.setName("Drill");
        drill.setUnitPrice(new BigDecimal("129.99"));
        drill.setTaxes(Set.of(gst,qst));
        drill.setCategories(Set.of(hardware));
        itemRepository.save(drill);

        Item wrench = new Item();
        wrench.setName("Wrench");
        wrench.setUnitPrice(new BigDecimal("9.99"));
        wrench.setTaxes(Set.of(gst,qst));
        wrench.setCategories(Set.of(hardware));
        itemRepository.save(wrench);

        Item hammer = new Item();
        hammer.setName("Hammer");
        hammer.setUnitPrice(new BigDecimal("8.99"));
        hammer.setTaxes(Set.of(gst,qst));
        hammer.setCategories(Set.of(hardware));
        itemRepository.save(hammer);
    }
}
