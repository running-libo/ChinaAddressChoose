package com.example.addresschoose.chinaaddresschoose;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private View rootView;
    private TextView tvShowAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = LayoutInflater.from(this).inflate(R.layout.address_popupwindow,null);
        setContentView(R.layout.activity_main);

        tvShowAddress = (TextView) findViewById(R.id.tv_show_address);
    }

    public void onclick(View view){
        AddressPopupWindow addressPopupWindow = new AddressPopupWindow(this);
        addressPopupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
        addressPopupWindow.setCallBack(new AddressPopupWindow.IcallBack() {
            @Override
            public void callBack(String address) {
                tvShowAddress.setText(address);
            }
        });
    }
}
