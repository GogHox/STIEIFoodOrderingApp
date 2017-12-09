package com.example.admin.myapplication.bean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by GogHox on 2017/10/17.
 */

public class ComboBean {
    public String name;                 // combo name
    public int id;
    public double money;
    public String photo;                // picture after base64 encode
    public int combo_available;
    public String[] ingredient;        // combo ingredient
    public String description;         // combo description, according to the ingredient and name

    public ComboBean(){}
    public ComboBean(JSONObject comboItemObj){
        try {
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
        this.ingredient = ingredient;

        this.description = "" + this.name + ": ";
        for(int i = 0; i < ingredient.length; i++) {
            if(i != 0) {
                description += ", ";
            }
            description += ingredient[i];
        }
    }
}
