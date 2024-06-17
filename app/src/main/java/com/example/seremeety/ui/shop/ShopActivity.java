package com.example.seremeety.ui.shop;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import kr.co.bootpay.android.*;
import kr.co.bootpay.android.events.BootpayEventListener;
import kr.co.bootpay.android.models.BootExtra;
import kr.co.bootpay.android.models.BootItem;
import kr.co.bootpay.android.models.BootUser;
import kr.co.bootpay.android.models.Payload;

import com.example.seremeety.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ShopActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        Button notes15 = findViewById(R.id.notes_15);
        Button notes55 = findViewById(R.id.notes_55);
        Button notes115 = findViewById(R.id.notes_115);
        Button notes355 = findViewById(R.id.notes_355);

        notes15.setOnClickListener(v -> {
            PaymentTest(v, "세레미티 15음표", "SEREMEETY_15NOTES",3000d);
        });

        notes55.setOnClickListener(v -> {
            PaymentTest(v, "세레미티 55음표", "SEREMEETY_55NOTES",9900d);
        });

        notes115.setOnClickListener(v -> {
            PaymentTest(v, "세레미티 115음표", "SEREMEETY_115NOTES",18400d);
        });

        notes355.setOnClickListener(v -> {
            PaymentTest(v, "세레미티 355음표", "SEREMEETY_355NOTES",49700d);
        });
    }

    public void PaymentTest(View v, String productName, String productId, double price) {
        BootUser user = new BootUser().setPhone("010-1234-5678"); // 구매자 정보

        BootExtra extra = new BootExtra()
                .setCardQuota("0,2,3"); // 일시불, 2개월, 3개월 할부 허용, 할부는 최대 12개월까지 사용됨 (5만원 이상 구매시 할부허용 범위)


        List<BootItem> items = new ArrayList<>();
        BootItem item1 = new BootItem().setName(productName).setId(productId).setQty(1).setPrice(price);
        items.add(item1);

        Payload payload = new Payload();
        payload.setApplicationId("5b8f6a4d396fa665fdc2b5e8")
                .setOrderName(productName)
                .setPg("나이스페이")
                .setMethod("네이버페이")
                .setOrderId("1234")
                .setPrice(price)
                .setUser(user)
                .setExtra(extra)
                .setItems(items);

        Map<String, Object> map = new HashMap<>();
        map.put("1", "abcdef");
        map.put("2", "abcdef55");
        map.put("3", 1234);
        payload.setMetadata(map);
//        payload.setMetadata(new Gson().toJson(map));

        Bootpay.init(getSupportFragmentManager(), getApplicationContext())
                .setPayload(payload)
                .setEventListener(new BootpayEventListener() {
                    @Override
                    public void onCancel(String data) {
                        Log.d("bootpay", "cancel: " + data);
                    }

                    @Override
                    public void onError(String data) {
                        Log.d("bootpay", "error: " + data);
                    }

                    @Override
                    public void onClose() {
                        Bootpay.removePaymentWindow();
                    }

                    @Override
                    public void onIssued(String data) {
                        Log.d("bootpay", "issued: " +data);
                    }

                    @Override
                    public boolean onConfirm(String data) {
                        Log.d("bootpay", "confirm: " + data);
//                        Bootpay.transactionConfirm(data); //재고가 있어서 결제를 진행하려 할때 true (방법 1)
                        return true; //재고가 있어서 결제를 진행하려 할때 true (방법 2)
//                        return false; //결제를 진행하지 않을때 false
                    }

                    @Override
                    public void onDone(String data) {
                        Log.d("done", data);
                    }
                }).requestPayment();
    }
}
