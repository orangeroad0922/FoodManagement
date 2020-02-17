package com.example.foodmanagement;

import android.app.Application;

public class FoodManagementApplication extends Application {
    private static FoodManagementApplication instance = null;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
    }

    public static FoodManagementApplication getInstance() {
        return instance;
    }
}
