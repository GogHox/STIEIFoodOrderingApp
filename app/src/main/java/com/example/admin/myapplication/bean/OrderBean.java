package com.example.admin.myapplication.bean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by goghox on 11/8/17.
 */

public class OrderBean {
    public String id;
    public int comboId;
    public String orderedAt;
    public String pickupTime;
    public int served;   // true == 1, false == 0
    public int lockerNumber;
    public int PIN;

    public OrderBean(){}

    public OrderBean (String json){
        try {
            JSONObject jo = new JSONObject(json);

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
    }
}
