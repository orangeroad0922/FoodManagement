package com.example.foodmanagement.model;

import java.util.Date;

import lombok.Data;

@Data
public class FoodData {
    private String foodName;
    private Date expiration;
}

