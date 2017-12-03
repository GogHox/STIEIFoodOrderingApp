package com.example.admin.myapplication.bean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by goghox on 11/8/17.
 */

public class OrderBean {
    public static final int FIRSET_REQUEST = 1;
    public static final int SENCOND_REQUEST = 2;

    public String pickupTime;
    public int comboId;
    public String ordersId;
    public String name;
    public String token;

    public String id;
    public String orderedAt;
    public int served;   // true == 1, false == 0
    public int lockerNumber;
    public int PIN;


    public OrderBean() {}
    public OrderBean(String ordersId) {
        this.ordersId = ordersId;
    }
    public void setData(String json, int which) {
        switch (which) {
            case FIRSET_REQUEST:
// {"pickup_time":"11:45","locker_nr":1002,"orders_id":"ba5ab10c-6b15-4bd9-911e-c90eb544ad8b","name":"User","token":".."}
                try {
                    JSONObject jo = new JSONObject(json);
                    this.pickupTime = jo.getString("pickup_time");
                    this.lockerNumber = jo.getInt("locker_nr");
                    this.ordersId = jo.getString("orders_id");
                    this.name = jo.getString("name");
                    this.token = jo.getString("token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case SENCOND_REQUEST:
                try {
                    JSONArray ja = new JSONArray(json);
                    JSONObject jo = ja.getJSONObject(0);
                    this.id = jo.getString("id");
                    this.comboId = jo.getInt("combo_id");
                    this.orderedAt = jo.getString("ordered_at");
                    this.pickupTime = jo.getString("pickup_time");
                    this.served = jo.getInt("served");
                    this.lockerNumber = jo.getInt("locker_nr");
                    this.PIN = jo.getInt("PIN");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }

    }
}
