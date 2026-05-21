package com.highspring.shopping.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "discounts")
@Getter @Setter @NoArgsConstructor
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private BigDecimal percent;
    private ZonedDateTime start;

    @Column(name = "end_date")
    private ZonedDateTime end;

    private Boolean cumulative;
}
