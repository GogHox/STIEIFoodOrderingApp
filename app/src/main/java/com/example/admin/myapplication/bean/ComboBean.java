package com.example.admin.myapplication.bean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by GogHox on 2017/10/17.
 */

public class ComboBean {
    public String name;
    public int id;
    public double money;
    public String photo;
    public int combo_available;
    public String description;
    public String picture;

    public ComboBean(){}
    public ComboBean(JSONObject comboItemObj){
        try {
            /* //old api
            this.name = comboItemObj.getString("name");
            this.money = comboItemObj.getDouble("money");
            this.status = comboItemObj.getInt("status");
            this.description = comboItemObj.getString("description");
            this.picture = comboItemObj.getString("picture");
            */
            this.id = comboItemObj.getInt("id");
            this.name = comboItemObj.getString("name");
            this.money = comboItemObj.getDouble("price");
            this.combo_available = comboItemObj.getInt("combo_available");
            this.photo = comboItemObj.getString("photo");
            this.description = "Combo Name: " + name;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setComboDesc(String[] ingredient) {

        this.description = "" + this.name + ": ";
        for(int i = 0; i < ingredient.length; i++) {
            if(i != 0) {
                description += ", ";
            }
            description += ingredient[i];
        }
    }
}
