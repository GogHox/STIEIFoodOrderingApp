package com.example.admin.myapplication.activity;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.myapplication.AppConstant;
import com.example.admin.myapplication.R;
import com.example.admin.myapplication.bean.ComboBean;
import com.example.admin.myapplication.bean.OrderBean;
import com.example.admin.myapplication.utils.Net;
import com.example.admin.myapplication.utils.OrderDBHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by goghox on 11/7/17.
 */

public class MeActivity extends AppCompatActivity {

    private ListView lvOrder;
    private TextView tvNoDataHint;
    private SwipeRefreshLayout srl;
    private ArrayList<OrderBean> orderList = new ArrayList<>();
    private ArrayList<ComboBean> comboMsgList = new ArrayList<>();
    private int orderNum = 0;
    private static final int UPDATE_VIEW = 0;
    private static final int NETWORK_ERROR = 1;
    private String TAG = "MeActivity";
    private OrderAdapter mAdapter;
    private long mTempStartTime = 0;
    private long mTempEndTime = 0;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what){
                case UPDATE_VIEW:
                    if(orderList.size() <= 0){
                        tvNoDataHint.setVisibility(View.VISIBLE);
                        lvOrder.setVisibility(View.GONE);
                    }else{
                        tvNoDataHint.setVisibility(View.INVISIBLE);
                        lvOrder.setVisibility(View.VISIBLE);
                    }
                    mAdapter.notifyDataSetChanged();
                    mTempEndTime = SystemClock.currentThreadTimeMillis();
                    break;
                case NETWORK_ERROR:
                    Toast.makeText(getBaseContext(), "Network error, please retry.", Toast.LENGTH_LONG).show();
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        initView();
        initData();
    }

    private void initData() {
        mAdapter = new OrderAdapter(this, R.layout.item_order, orderList);
        lvOrder.setAdapter(mAdapter);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mTempStartTime = SystemClock.currentThreadTimeMillis();
                        getOrdersFromServer();
                        if(mTempStartTime - mTempEndTime < 1500) {
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    srl.setRefreshing(false);
                                }
                            }, 1000);
                        }else{
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    srl.setRefreshing(false);
                                }
                            });
                        }
                    }
                }).start();
            }
        });
        // get data of ListView from server.
        new Thread(new Runnable() {
            @Override
            public void run() {
                getComboMsgFromServer();
                getOrdersFromServer();
            }
        }).start();
    }

    private void getComboMsgFromServer() {
        try {
            Request request = new Request.Builder().url(AppConstant.SERVER_COMBO_URL).get().build();
            Response response = Net.getOkHttpClient().newCall(request).execute();
            if(response.code() > 400) {
                mHandler.sendEmptyMessage(NETWORK_ERROR);
            }
            String comboMsgJson = response.body().source().readUtf8();
            JSONArray ja = new JSONArray(comboMsgJson);
            for(int i = 0; i < ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);
                ComboBean bean = new ComboBean(jo);
                comboMsgList.add(bean);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        tvNoDataHint = (TextView) findViewById(R.id.tv_no_data_hint);
        lvOrder = (ListView) findViewById(R.id.lv_order);
        srl = (SwipeRefreshLayout) findViewById(R.id.srl);

        srl.setColorSchemeResources(R.color.colorAccent);
        tvNoDataHint.setVisibility(View.VISIBLE);
        lvOrder.setVisibility(View.GONE);
    }

    public void getOrdersFromServer() {
        FileInputStream fis = null;
        try {
            // 1. get the order id from native database
            OrderDBHelper dbHelper = new OrderDBHelper(this);
            Cursor cursor = dbHelper.queryOrder();
            orderNum = cursor.getCount();
            if(cursor == null){
                return;
            }
            // first, clear the order list.
            orderList.clear();
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String orderId = cursor.getString(cursor.getColumnIndex("order_id"));
                String token = cursor.getString(cursor.getColumnIndex("token"));

                // 2. request server to get the order information.
                Request request = new Request.Builder()
                        .url(AppConstant.SERVER_ORDER_URL + "/" + orderId)
                        .get()
                        .addHeader("token", token)
                        .build();
                Response response = Net.getOkHttpClient().newCall(request).execute();
                if(response.code() > 400) {
                    continue;
                }

                if(response.code() == 200) {
                    // add the bean obj to list
                    String resJson = response.body().source().readUtf8();
                    OrderBean bean = new OrderBean(orderId);
                    bean.setData(resJson, OrderBean.SENCOND_REQUEST);
                    if (bean.comboId > 0) {
                        orderList.add(bean);
                    } else {
                        // TODO delete the order item in local.
                        dbHelper.deleteOrder(id);
                    }
                }
            }
            cursor.close();

            //
            /*fis = getApplication().openFileInput(AppConstant.USER_ORDERS);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String orderId;

            // first, clear the order list.
            orderList.clear();
            while((orderId = br.readLine()) != null){
                if(orderId.isEmpty())
                    break;

                String token = br.readLine();
                Log.i(TAG, ": " + orderId);
                orderNum++;
                // 2. request server to get the order information.
                Request request = new Request.Builder()
                        .url(AppConstant.SERVER_ORDER_URL + "/" + orderId)
                        .get()
                        .addHeader("token", token)
                        .build();

                Response response = Net.getOkHttpClient().newCall(request).execute();
                if(response.code() > 400) {
                    continue;
                }

                // add the bean obj to list
                String resJson = response.body().source().readUtf8();
                OrderBean bean = new OrderBean(orderId);
                bean.setData(resJson, OrderBean.SENCOND_REQUEST);
                if(bean.comboId > 0) {
                    orderList.add(bean);
                }else {
                    // TODO delete the order item in local.
                }
            }*/

            mHandler.sendEmptyMessage(UPDATE_VIEW);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ;
    }

    class OrderAdapter extends ArrayAdapter<OrderBean>{

        public OrderAdapter(@NonNull Context context, int resource) {
            super(context, resource);
        }
        public OrderAdapter(@NonNull Context context, int resource, @NonNull List<OrderBean> objects) {
            super(context, resource, objects);
        }

        private boolean haveFinishItem = false;
        ViewHolder holder;
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView == null){
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_order, parent, false);
                holder = new ViewHolder();

                holder.tvComboName = convertView.findViewById(R.id.tv_combo_name);
                holder.tvComboStatus = convertView.findViewById(R.id.tv_order_status);
                holder.tvOrderId = convertView.findViewById(R.id.tv_order_id);
                holder.tvPickupTime = convertView.findViewById(R.id.tv_pickup_time);
                holder.tvLockerNumber = convertView.findViewById(R.id.tv_locker_number);
                holder.tvPIN = convertView.findViewById(R.id.tv_pin);
                holder.ivIcon = convertView.findViewById(R.id.good_icon);
                holder.tvInformation = convertView.findViewById(R.id.tv_information);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }

            OrderBean bean = orderList.get(position);
            holder.tvComboName.setText("Combo ID: " + bean.comboId);
            if(bean.served == 0){
                // finish?
                holder.tvComboStatus.setText("unseved");
                holder.tvComboStatus.setTextColor(Color.RED);

                holder.tvPIN.setVisibility(View.GONE);
                holder.tvInformation.setText("You'll receive your PIN, when your order is served.");
            }else{
                holder.tvComboStatus.setText("served");
                holder.tvComboStatus.setTextColor(Color.BLUE);

                holder.tvInformation.setText("Please type the PIN in to the locker.");
            }
            if(comboMsgList.size() > 0 && bean.comboId > 0) {
                byte[] base64Pic = Base64.decode(comboMsgList.get(bean.comboId - 1).photo, 0);
                Bitmap bitmap = BitmapFactory.decodeByteArray(base64Pic, 0, base64Pic.length);
                holder.ivIcon.setImageBitmap(bitmap);
            }

            holder.tvOrderId.setText("Order id: " + bean.id);
            holder.tvPickupTime.setText("Pickup time: " + bean.pickupTime);
            holder.tvLockerNumber.setText("Locker number: " + bean.lockerNumber);
            holder.tvPIN.setText("PIN: " + bean.PIN);

            return convertView;
        }
        class ViewHolder {
            TextView tvComboName;
            TextView tvComboStatus;
            TextView tvOrderId;
            TextView tvPickupTime;
            TextView tvLockerNumber;
            TextView tvPIN;
            ImageView ivIcon;
            TextView tvInformation;
        }
    }
}
