package com.example.admin.myapplication;

/**
 * Created by ADMIN on 2017/10/10.
 */

public class AppConstant {
    // ----------------- APP
    public static final int COMBO_NUMBER = 3;

    // ----------------- Combo
    public static final String SEL_COMBO_NAME_TAG = "COMBO_NAME";
    public static final String SEL_COMBO_TIME_TAG = "COMBO_TIME";
    public static final String SEL_COMBO_MONEY_TAG = "COMBO_MONEY";
    public static final String SEL_COMBO_ID_TAG = "COMBO_ID";

    public static final int COMBO_1 = 0;
    public static final int COMBO_2 = 1;
    public static final int COMBO_3 = 2;
    // ---------------- Server
    public static final String SERVER_URL = "http://192.168.1.105:8080";
    public static final String SERVER_COMBO_URL = SERVER_URL + "/combo";
    public static final String SERVER_SCHEDULE_URL = SERVER_URL + "/timeschedule";
    public static final String SERVER_ORDER_URL = SERVER_URL + "/order";
    public static final String SERVER_COMBO_INGREDIENT_URL = SERVER_URL + "/combo/ingredient";

    // ---------------- SharedPreferences
    public static final String USER_ORDERS = "USER_ORDERS";
    public static final String ORDERS_ID = "ORDERS_ID";
}
