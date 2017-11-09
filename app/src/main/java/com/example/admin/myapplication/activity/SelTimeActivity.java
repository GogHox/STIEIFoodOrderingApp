package com.example.admin.myapplication.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.myapplication.AppConstant;
import com.example.admin.myapplication.R;
import com.example.admin.myapplication.bean.OrderBean;
import com.example.admin.myapplication.bean.ScheduleBean;
import com.example.admin.myapplication.utils.Net;
import com.example.admin.myapplication.view.ScheduleRadioButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SelTimeActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int PAY_FOR_ERROR = 1;
    private static final int PAY_FOR_SUCCESS = 2;
    private static final int UPDATE_VIEW = 3;

    private ArrayList<ScheduleBean> scheduleList = new ArrayList<>();
    private RadioGroup rgSelTime;
    private TextView tvPay;
    private String TAG = "SelTimeActivity";
    private TextView tvShowMoney;
    public double mComboMoney;    // price
    public String mComboName;     // comboName
    private int mComboId;         // comboId

    // data of pay for successful
    private String mOrdersId;
    private OrderBean mCurrentOrder;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_VIEW:
                    for (int i = 0; i < scheduleList.size(); i++) {
                        ScheduleRadioButton button = new ScheduleRadioButton(getApplicationContext());
                        ScheduleBean scheduleBean = scheduleList.get(i);
                        button.setText("" + scheduleBean.pickup_time);
                        if (scheduleBean.available == 0) {
                            button.setEnabled(false);
                        }
                        rgSelTime.addView(button);
                    }
                    break;
                case PAY_FOR_ERROR:
                    break;
                case PAY_FOR_SUCCESS:
                    Toast.makeText(getApplicationContext(),
                            "Pay for successful, please care about the notification.", Toast.LENGTH_LONG).show();
                    sendPINViaNotification();
                    finish();
                    break;
            }
            return false;
        }
    });
    private NotificationManager mNotificationManager;
    private Net net;

    private void sendPINViaNotification() {
        if (mNotificationManager == null)
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
        style.setBigContentTitle("PIN of pick up food");
        /*style.addLine("You pick up food time is: ").
                addLine("2017.10.24 10:15").
                addLine("Thank you for you using our produce");*/

        android.support.v4.app.NotificationCompat.Builder nb = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon_cart)   // must have small icon
                .setContentTitle("PIN of pick up food")
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo))
                .setContentInfo("STIEI")
                .setContentText("Locker:" + mCurrentOrder.lockerNumber + "; PIN: " + mCurrentOrder.PIN)
                .setTicker("scroll information text")
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)  // sound and vibrate
                .setOngoing(false)   // Forever show, can't remove by yourself.
                .setStyle(style);

        mNotificationManager.notify(0, nb.build());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sel_time);

        // get the selected combo from the activity before
        mComboMoney = getIntent().getDoubleExtra(AppConstant.SEL_COMBO_MONEY_TAG, 0);
        mComboName = getIntent().getStringExtra(AppConstant.SEL_COMBO_NAME_TAG);
        mComboId = getIntent().getIntExtra(AppConstant.SEL_COMBO_ID_TAG, -1);
        Log.i(TAG, "onCreate: money is " + mComboMoney + "; name is " + mComboName);

        if (mComboName.isEmpty()) { // error exception (no combo selected)
            // TODO Here should push out a dialog. When click confirm, return to previous page.
            Toast.makeText(this, "Please select a combo, then continue pay.", Toast.LENGTH_SHORT);
        }

        initView();
        initData();
    }

    private void initData() {
        this.net = Net.getInstance();

        // show the price
        tvShowMoney.setText("RMB ￥" + mComboMoney);

        // get the schedule to show
        getScheduleFromServer();

        // get user scheduled food pick up time
        rgSelTime.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int id) {

            }
        });
    }


    private void getScheduleFromServer() {
        net.get(AppConstant.SERVER_SCHEDULE_URL, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: connect error; " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonStr = response.body().string();
                Log.i(TAG, "onResponse: --------------------------------" + jsonStr);
                parseJsonAndUpdateView(jsonStr.trim());
            }
        });
    }

    private void parseJsonAndUpdateView(String json) {
        try {
            JSONArray scheduleArr = new JSONArray(json);

            for (int i = 0; i < scheduleArr.length(); i++) {
                JSONObject scheduleItemObj = scheduleArr.getJSONObject(i);
                ScheduleBean scheduleBean = new ScheduleBean(scheduleItemObj);
                scheduleList.add(scheduleBean);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // TODO parse finish, should update the view
        if (scheduleList.size() > 0) {
            handler.sendEmptyMessage(UPDATE_VIEW);
        } else {
            Toast.makeText(this, "get data error, please try repeat.", Toast.LENGTH_SHORT).show();
        }
    }

    private void initView() {
        rgSelTime = (RadioGroup) findViewById(R.id.rg_sel_time);
        tvPay = (TextView) findViewById(R.id.tv_pay);
        tvShowMoney = (TextView) findViewById(R.id.tv_show_money);

        tvPay.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_pay) {
            // when you click "go to pay"
            if (rgSelTime.getCheckedRadioButtonId() <= 0) {
                Toast.makeText(this, "Please select a pickup time", Toast.LENGTH_LONG).show();
                return;
            }
            // get the time of selected button via checked radio button
            RadioButton checkBtn = findViewById(rgSelTime.getCheckedRadioButtonId());
            requireServerToPayFor(mComboMoney, (String) checkBtn.getText());

        }
    }

    /*
    * Requesting the server with argument that have time and price, and if request success,
    *     will return the id of the box where is pick up food.
    * @arg money double
    * @arg time String
    * @return int, the combo number. If return -1, it mean have error at pay for.
     */
    private void requireServerToPayFor(double money, final String time) {
        final OkHttpClient okHttpClient = Net.getOkHttpClient();

        // time time is "12:00", isn't "12:00:00"
        final String comboTime = time.substring(0, 5);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i(TAG, "requireServerToPayFor: combo_id = " + mComboId + "; pickup_time = " + time);

                    Request request = new Request.Builder()
                            .post(new FormBody.Builder().build())
                            //.addHeader("content-type", "application/json;charset:utf-8")
                            .url(AppConstant.SERVER_ORDER_URL + "?combo_id="+mComboId+"&pickup_time="+comboTime)
                            .build();

                    Response response = okHttpClient.newCall(request).execute();
                    String result = response.body().string();
                    Log.i(TAG, "onResponse: " + result);
                    if(result != null) {
                        JSONObject resObj = new JSONObject(result);
                        mOrdersId = resObj.getString("orders_id");

                        if(mOrdersId != null && !mOrdersId.isEmpty()) {
                            // pay for success.
                            // save the order id to native
                            //SharedPreferences sp = getApplication().getSharedPreferences(AppConstant.USER_ORDERS, MODE_PRIVATE);
                            //sp.edit().putString(AppConstant.ORDERS_ID, mOrdersId).apply();
                            // TODO don't test
                            FileOutputStream fos = getApplication().openFileOutput(AppConstant.USER_ORDERS, MODE_APPEND);
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
                            bw.write(mOrdersId);
                            bw.newLine();
                            bw.close();
                            fos.close();

                            // get the order info via order_id
                            Net.getInstance().get(AppConstant.SERVER_ORDER_URL + "/" + mOrdersId, new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    handler.sendEmptyMessage(PAY_FOR_ERROR);
                                }
                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    String orderJson = response.body().string();
                                    Log.i(TAG, "onResponse: " + orderJson);
                                    mCurrentOrder = new OrderBean(orderJson);
                                    handler.sendEmptyMessage(PAY_FOR_SUCCESS);
                                }
                            });
                        }else {
                            handler.sendEmptyMessage(PAY_FOR_ERROR);
                        }
                    }
                    // parse result(json)

                } catch (IOException e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(PAY_FOR_ERROR);
                } catch (JSONException e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(PAY_FOR_ERROR);
                }
            }
        }).start();

    }
}
