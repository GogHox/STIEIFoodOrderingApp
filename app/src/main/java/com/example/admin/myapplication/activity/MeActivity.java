package com.example.admin.myapplication.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.admin.myapplication.AppConstant;
import com.example.admin.myapplication.R;
import com.example.admin.myapplication.bean.OrderBean;
import com.example.admin.myapplication.utils.Net;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by goghox on 11/7/17.
 */

public class MeActivity extends AppCompatActivity {

    private ListView lvOrder;
    private ArrayList<OrderBean> orderList = new ArrayList<>();
    private int orderNum = 0;

    private final int UPDATE_VIEW = 0;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what){
                case UPDATE_VIEW:
                    break;
            }
            return false;
        }
    });
    private TextView tvNoDataHint;
    private String TAG = "MeActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        initView();
        initData();
    }

    private void initData() {
        getOrdersFromServer();
        //getData();
        initList();
    }

    private void initList() {
        //SystemClock.sleep(500);
        OrderAdapter adapter = new OrderAdapter(this, R.layout.item_order, orderList);
        lvOrder.setAdapter(adapter);

        if(orderList.size() <= 0){
            tvNoDataHint.setVisibility(View.VISIBLE);
        }else{
            tvNoDataHint.setVisibility(View.INVISIBLE);
        }
    }

    private void initView() {
        tvNoDataHint = (TextView) findViewById(R.id.tv_no_data_hint);
        lvOrder = (ListView) findViewById(R.id.lv_order);
    }

    public void getOrdersFromServer() {
        FileInputStream fis = null;
        try {
            // 1. get the order id from native file
            fis = getApplication().openFileInput(AppConstant.USER_ORDERS);

            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            String orderId;
            while((orderId = br.readLine()) != null){
                Log.i(TAG, "getOrdersFromServer: " + orderId);
                orderNum++;
                // 2. request server to get the order information.
                Net.getInstance().get(AppConstant.SERVER_ORDER_URL + "/" + orderId, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        // add the bean obj to list
                        String resJson = response.body().string();
                        OrderBean bean = new OrderBean(resJson);
                        orderList.add(bean);

                        // TODO have a problem, when should let the ListView update view.
                        //mHandler.sendEmptyMessage(UPDATE_VIEW);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ;
    }

    public void getData() {
        for(int i = 0; i < 40; i++){
            OrderBean orderBean = new OrderBean();
            orderBean.id = "f;sajfal" + i;
            orderBean.PIN = 1234 + i;
            orderBean.lockerNumber = 12 + i;
            orderBean.served = 0;
            orderBean.pickupTime = "12:30";
            orderBean.comboId = 1 + i;

            orderList.add(orderBean);
        }
    }

    class OrderAdapter extends ArrayAdapter<OrderBean>{

        public OrderAdapter(@NonNull Context context, int resource) {
            super(context, resource);
        }

        public OrderAdapter(@NonNull Context context, int resource, @NonNull List<OrderBean> objects) {
            super(context, resource, objects);
        }

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
                holder.tvMoneyValue = convertView.findViewById(R.id.tv_money_value);
                holder.tvPIN = convertView.findViewById(R.id.tv_pin);
                convertView.setTag(holder);

            }else{
                holder = (ViewHolder) convertView.getTag();
            }


            OrderBean bean = orderList.get(position);
            holder.tvComboName.setText("" + bean.comboId);
            holder.tvComboStatus.setText("" + bean.served);
            holder.tvOrderId.setText("" + bean.id);
            holder.tvPickupTime.setText("" +bean.pickupTime);
            //tvMoneyValue.setText(bean.served);
            holder.tvPIN.setText("" +bean.PIN);

            return convertView;
            //return super.getView(position, convertView, parent);
        }
        class ViewHolder {
            TextView tvComboName;
            TextView tvComboStatus;
            TextView tvOrderId;
            TextView tvPickupTime;
            TextView tvMoneyValue;
            TextView tvPIN;
        }
    }
}
