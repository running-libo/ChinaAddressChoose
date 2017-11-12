package com.example.addresschoose.chinaaddresschoose;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.example.addresschoose.chinaaddresschoose.bean.CityEntity;
import com.example.addresschoose.chinaaddresschoose.bean.CountyEntity;
import com.example.addresschoose.chinaaddresschoose.bean.ProvinceEntity;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by libo on 2017/4/27.
 *
 * 地址弹出框选择
 */
public class AddressPopupWindow extends PopupWindow{
    private View view;
    private Context context;
    private RecyclerView recyclerView;
    /** 当前省下显示市 */
    private List<CityEntity> currentCitiesDatas = new ArrayList<>();
    /** 当前市下显示县 */
    private List<CountyEntity> currentCountiesDatas = new ArrayList<>();
    /** 省截取2位代码 */
    private String provinceChargeCode;
    /** 市截取2位代码 */
    private String citiesChargeCode;
    /** 最终确认的地址提交参数 */
    private String saveProvinceCode,saveCityCode,saveCountyCode,saveZipCode;
    private String saveProvinceName,saveCityName,saveCountyName;
    /** 所有省 */
    public static List<ProvinceEntity> provinceDatas;
    /** 所有市 */
    public static List<CityEntity> cityDatas;
    /** 所有县 */
    public static List<CountyEntity> countyDatas;
    private IcallBack callBack;

    public AddressPopupWindow(Context context){
        this.context = context;
        view = ((Activity)context).getLayoutInflater().from(context).inflate(R.layout.address_popupwindow,null);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setBackgroundDrawable(new BitmapDrawable());
        setFocusable(true);
        setOutsideTouchable(true);
        setAnimationStyle(R.style.PopupAnimation);
        setContentView(view);

        selectProvince();
    }

    /**
     * 从资源文件中获取json字符串
     *
     * @return
     */
    private String getAddress() {
        StringBuilder sb = new StringBuilder();
        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.area);
            byte[] buffer = new byte[1024];
            while (inputStream.read(buffer) != -1) {
                sb.append(new String(buffer, "UTF-8"));
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }


    /**
     * 解析出所有省市县
     */
    private void parseArray() {
        try {
            JSONObject object = new JSONObject(getAddress());
            JSONArray provinceArray = object.getJSONArray("province");
            JSONArray cityArray = object.getJSONArray("city");
            JSONArray countyArray = object.getJSONArray("district");
            provinceDatas = JSON.parseArray(provinceArray.toString(), ProvinceEntity.class);
            cityDatas = JSON.parseArray(cityArray.toString(), CityEntity.class);
            countyDatas = JSON.parseArray(countyArray.toString(), CountyEntity.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 选择省
     */
    private void selectProvince() {

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_popup_address);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        parseArray();
        CommonAdapter commonAdapter = new CommonAdapter<ProvinceEntity>(context,R.layout.item_address_textview,provinceDatas) {
            @Override
            protected void convert(ViewHolder holder, ProvinceEntity provinceEntity, int position) {
                holder.setText(R.id.tv_popup_place,provinceEntity.getText());
            }
        };
        recyclerView.setAdapter(commonAdapter);
        commonAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                saveProvinceCode = provinceDatas.get(position).getId();
                saveProvinceName = provinceDatas.get(position).getText();
                provinceChargeCode = saveProvinceCode.substring(0, 2);
                String cityCode;
                for (int i = 0; i < cityDatas.size(); i++) {
                    cityCode = cityDatas.get(i).getId();
                    if (provinceChargeCode.equals(cityCode.substring(0, 2))) {
                        currentCitiesDatas.add(cityDatas.get(i));
                    }
                }
                selectCites();
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return false;
            }
        });

        view.findViewById(R.id.tv_popup_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                view.clearAnimation();
            }
        });
    }

    /**
     * 根据当前省选择市
     */
    private void selectCites(){
        CommonAdapter adapter = new CommonAdapter<CityEntity>(context,R.layout.item_address_textview,currentCitiesDatas) {
            @Override
            protected void convert(ViewHolder holder, CityEntity cityEntity, int position) {
                holder.setText(R.id.tv_popup_place,cityEntity.getText());
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                saveCityCode = currentCitiesDatas.get(position).getId();
                saveCityName = currentCitiesDatas.get(position).getText();
                citiesChargeCode = saveCityCode.substring(2, 4);
                String countyCode;
                for (int i = 0; i < countyDatas.size(); i++) {
                    countyCode = countyDatas.get(i).getId();
                    if (countyCode.substring(0, 2).equals(provinceChargeCode) && countyCode.substring(2,4).equals(citiesChargeCode)) {
                        currentCountiesDatas.add(countyDatas.get(i));
                    }
                }
                selectCounties();
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return false;
            }
        });
    }

    /**
     * 根据当前市选择县
     */
    private void selectCounties(){
        CommonAdapter adapter = new CommonAdapter<CountyEntity>(context,R.layout.item_address_textview,currentCountiesDatas) {
            @Override
            protected void convert(ViewHolder holder, CountyEntity countyEntity, int position) {
                holder.setText(R.id.tv_popup_place,countyEntity.getText());
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                saveCountyCode = currentCountiesDatas.get(position).getId();
                saveCountyName = currentCountiesDatas.get(position).getText();
                saveZipCode = currentCountiesDatas.get(position).getZipcode();
                dismiss();
                view.clearAnimation();

                if(callBack != null){
                    callBack.callBack(saveProvinceName + ";" + saveCityName + ";" + saveCountyName);
                }
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return false;
            }
        });

    }

    public interface IcallBack{
        void callBack(String address);
    }

    public void setCallBack(IcallBack callBack){
        this.callBack = callBack;
    }

}
