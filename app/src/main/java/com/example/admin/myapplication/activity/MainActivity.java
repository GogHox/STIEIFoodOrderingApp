package com.example.admin.myapplication.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.admin.myapplication.AppConstant;
import com.example.admin.myapplication.R;
import com.example.admin.myapplication.bean.ComboBean;
import com.example.admin.myapplication.utils.Net;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
/**
 * Created by goghox on 11/7/17.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private RadioGroup rgCombo;
    private LinearLayout llCombo1, llCombo2, llCombo3;
    private TextView tvMoney1, tvMoney2, tvMoney3;
    private RadioButton rbCombo1, rbCombo2, rbCombo3;
    private TextView tvDescription1, tvDescription2, tvDescription3;
    private ImageView ivShowPic1, ivShowPic2, ivShowPic3;

    private final int GET_DATA_SUCCESS = 1;
    private final int NETWORK_ERROR = 2;
    /* arg */
    private int selectComboIndex = 0;
    private String TAG = "MainActivity";
    private ArrayList<TextView> comboMoneyViewList = new ArrayList<>();
    private ArrayList<TextView> comboDescriptionViewList = new ArrayList<>();
    private ArrayList<ImageView> comboShowPicViewList = new ArrayList<>();
    private ArrayList<ComboBean> comboList = new ArrayList<>();
    private ArrayList<LinearLayout> comboLayoutList = new ArrayList<>();
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case GET_DATA_SUCCESS:
                    updateView();
                    break;
                case NETWORK_ERROR:
                    Toast.makeText(getApplication(), "Network Error, Please restart app!", Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        }
    });

    private void updateView() {
            for (int i = 0; i < comboList.size(); i++){
                ComboBean comboBean = comboList.get(i);
                comboMoneyViewList.get(i).setText("￥ " + comboBean.money);
                comboDescriptionViewList.get(i).setText("" + comboBean.description);

            if(comboBean.combo_available == 0){
                comboLayoutList.get(i).setClickable(false);
                comboLayoutList.get(i).setBackgroundColor(Color.GRAY);
            }
            /*if(comboBean.picture != null){
                Glide.with(this)
                        .load(comboBean.picture)
                        .into(comboShowPicViewList.get(i));
            }*/
            if(!comboBean.photo.isEmpty()) {
                byte[] base64Pic = Base64.decode(comboBean.photo, 1);
                Bitmap bitmap = BitmapFactory.decodeByteArray(base64Pic, 0, base64Pic.length);
                comboShowPicViewList.get(i).setImageBitmap(bitmap);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        requestServerToGetInformation();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // reset the RadioButton status
        rbCombo1.setChecked(false);
        rbCombo2.setChecked(false);
        rbCombo3.setChecked(false);
    }

    /*
        * connect the serve
        * free space for server connection integration:
        * server request to show the provided food portfolio
        * */
    private void requestServerToGetInformation() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder()
                        .url(AppConstant.SERVER_COMBO_URL)
                        .get()
                        .build();
                try {
                    Response response = Net.getOkHttpClient().newCall(request).execute();
                    if(response.code() == 200){
                        String jsonStr = response.body().string();
                        Log.i(TAG, "onResponse: --------------------------------" + jsonStr);
                        parseJsonAndUpdateView(jsonStr.trim());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // This function run in child thread.
    private void parseJsonAndUpdateView(String json) {
        try {
            JSONArray comboArr = new JSONArray(json);
            for(int i = 0; i < comboArr.length(); i++){
                JSONObject comboItemObj = comboArr.getJSONObject(i);
                ComboBean comboBean = new ComboBean(comboItemObj);
                comboList.add(comboBean);
                // get the combo ingredient from server
                Request request = new Request.Builder()
                        .url(AppConstant.SERVER_COMBO_INGREDIENT_URL + "/" + comboBean.id)
                        .get()
                        .build();
                Response response = Net.getOkHttpClient().newCall(request).execute();
                String ingredientJson = response.body().source().readUtf8();
                JSONArray ja = new JSONArray(ingredientJson);
                String []comboIngredient = new String[ja.length()];
                for (int j = 0; j < ja.length(); j++) {
                    JSONObject jo = ja.getJSONObject(j);
                    comboIngredient[j] = jo.getString("name");
                }
                comboList.get(i).setComboDesc(comboIngredient);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (comboList.size() >= 0){
            // information main thread update view
            mHandler.sendEmptyMessage(GET_DATA_SUCCESS);
        }
    }

    private void initView() {
        rgCombo = (RadioGroup) findViewById(R.id.rg_combo);
    /* for the lucky day demo software show the three defined combos
    current development status: static -> not integrated a server database request
    for showing the food which is storaged in the database*/

        /* Combo 1 */
        llCombo1 = (LinearLayout) findViewById(R.id.ll_combo_1);
        ivShowPic1 = (ImageView) findViewById(R.id.iv_show_pic_1);
        tvMoney1 = (TextView) findViewById(R.id.tv_money_1);
        rbCombo1 = (RadioButton) findViewById(R.id.rb_combo_1);
        tvDescription1 = (TextView) findViewById(R.id.tv_description_1);
        // add the view to list to management
        comboLayoutList.add(llCombo1);
        comboDescriptionViewList.add(0, tvDescription1);
        comboMoneyViewList.add(0, tvMoney1);
        comboShowPicViewList.add(0, ivShowPic1);

        /* Combo 2 */
        llCombo2 = (LinearLayout) findViewById(R.id.ll_combo_2);
        ivShowPic2 = (ImageView) findViewById(R.id.iv_show_pic_2);
        tvMoney2 = (TextView) findViewById(R.id.tv_money_2);
        rbCombo2 = (RadioButton) findViewById(R.id.rb_combo_2);
        tvDescription2 = (TextView) findViewById(R.id.tv_description_2);
        // add the view to list to management
        comboLayoutList.add(llCombo2);
        comboDescriptionViewList.add(1, tvDescription2);
        comboMoneyViewList.add(1, tvMoney2);
        comboShowPicViewList.add(1, ivShowPic2);

        /* Combo 3 */
        llCombo3 = (LinearLayout) findViewById(R.id.ll_combo_3);
        ivShowPic3 = (ImageView) findViewById(R.id.iv_show_pic_3);
        tvMoney3 = (TextView) findViewById(R.id.tv_money_3);
        rbCombo3 = (RadioButton) findViewById(R.id.rb_combo_3);
        tvDescription3 = (TextView) findViewById(R.id.tv_description_3);
        // add the view to list to management
        comboLayoutList.add(llCombo3);
        comboDescriptionViewList.add(2, tvDescription3);
        comboMoneyViewList.add(2, tvMoney3);
        comboShowPicViewList.add(2, ivShowPic3);

        // set click enable (android internal transport mechanism)
        llCombo1.setOnClickListener(this);
        llCombo2.setOnClickListener(this);
        llCombo3.setOnClickListener(this);

        /* Me button */
        ((Button)findViewById(R.id.btn_me)).setOnClickListener(this);
    }

    /*switch case for selecting food: current status static
    later needed to be implemented：server and database request*/
    @Override
    public void onClick(View v) {
        boolean isSelCombo = false;
        switch (v.getId()) {
            case R.id.ll_combo_1:
                rbCombo1.setChecked(true);
                selectComboIndex = AppConstant.COMBO_1;
                isSelCombo = true;
                break;
            case R.id.ll_combo_2:
                rbCombo2.setChecked(true);
                selectComboIndex = AppConstant.COMBO_2;
                isSelCombo = true;
                break;
            case R.id.ll_combo_3:
                rbCombo3.setChecked(true);
                selectComboIndex = AppConstant.COMBO_3;
                isSelCombo = true;
                break;
            case R.id.btn_me:
                startActivity(new Intent(getApplicationContext(), MeActivity.class));
                isSelCombo = false;
                break;
        }
        if(!isSelCombo)
            return;
        // enter next activity
        Intent intent = new Intent(this, SelTimeActivity.class);
        if(comboList.size() < selectComboIndex+1){
            mHandler.sendEmptyMessage(NETWORK_ERROR);
            onRestart();
            return;
        }
        intent.putExtra(AppConstant.SEL_COMBO_ID_TAG, comboList.get(selectComboIndex).id);
        intent.putExtra(AppConstant.SEL_COMBO_NAME_TAG, comboList.get(selectComboIndex).name);
        intent.putExtra(AppConstant.SEL_COMBO_MONEY_TAG, comboList.get(selectComboIndex).money);
        startActivity(intent);
    }

}

