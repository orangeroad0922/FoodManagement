package com.example.foodmanagement.database;

import android.provider.BaseColumns;

public final class FoodExpirationContract {
    private FoodExpirationContract() {
    }

    public static class FoodExpiration implements BaseColumns {
        public static final String TABLE_NAME = "food_expiration";
        public static final String COLUMN_NAME_TITLE = "foodName";
        public static final String COLUMN_NAME_EXPIRATION = "expiration";
    }

}
