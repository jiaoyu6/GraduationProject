package com.tencent.qcloud.tim.demo.map;


import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.RequiresApi;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.tencent.qcloud.tim.demo.R;
import com.tencent.qcloud.tim.demo.profile.ProfileLayout;
import com.tencent.qcloud.tim.uikit.base.BaseFragment;
import com.tencent.qcloud.tim.uikit.component.TitleBarLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.view.View.GONE;


public class mapFragment extends BaseFragment {
    private ProfileLayout mProfileLayout;
    private TextView timeText;
    private TextView weather;
    private TextView temperature;
    private TextureMapView mapView = null;
    private BaiduMap baiduMap;
    private TextureMapView trace;
    private boolean isFirstLocate = true;
    private LocationClient mLocationClient;
    private TextView postionText;
    private int serviceId = 219865;
    private Button button_trace;
    private TitleBarLayout mTitleBar;
    public MyLocationListener myListener = new MyLocationListener();
    private String CityName = "北京";
    private String Weather;
    private String Tempeature;

    private AlarmManager alarmManager = null;
    private DbHelper dbhelper;
    private SQLiteDatabase db;
    SimpleCursorAdapter adapter = null;
    ListView lv;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        SDKInitializer.initialize(getActivity().getApplicationContext());
        mLocationClient = new LocationClient(getActivity());
        mLocationClient.registerLocationListener(myListener);
        View rootView = inflater.inflate(R.layout.map_fragment, container, false);
        weather = (TextView) rootView.findViewById(R.id.Weather);
        temperature = (TextView) rootView.findViewById(R.id.Temperature);
        postionText = (TextView) rootView.findViewById(R.id.position_text_view);
        timeText = (TextView) rootView.findViewById(R.id.time_text_view);
        mapView = (TextureMapView) rootView.findViewById(R.id.map);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);//显示当地位置
        mLocationClient.start();
        mTitleBar = rootView.findViewById(R.id.self_info_title_bar);
        mTitleBar.getLeftGroup().setVisibility(GONE);
        mTitleBar.getRightGroup().setVisibility(GONE);
        mTitleBar.setTitle(getResources().getString(R.string.profile), TitleBarLayout.POSITION.MIDDLE);

        initLocation();
        Time();


        alarmManager=(AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);//fragment与Activity的区别
        dbhelper = new DbHelper(getActivity(), "db_bwl", null, 1);
        db = dbhelper.getReadableDatabase();
        Cursor cursor = db.query("tb_bwl", new String[]{"id as _id","title","content","noticeDate","noticeTime"}, null, null, null, null,null);
        lv = (ListView)rootView.findViewById(R.id.lv_bwlList);
        adapter = new SimpleCursorAdapter(getActivity(), R.layout.list_item_bwl, cursor,
                new String[]{"title","noticeDate","noticeTime","content"},
                new int[]{R.id.title,R.id.noticeDate,R.id.noticeTime,R.id.content});
        lv.setAdapter(adapter);
        this.registerForContextMenu(lv);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                String title = ((TextView)view.findViewById(R.id.title)).getText().toString();
                String content = ((TextView)view.findViewById(R.id.content)).getText().toString();
                String noticeDate = ((TextView)view.findViewById(R.id.noticeDate)).getText().toString();
                String noticeTime = ((TextView)view.findViewById(R.id.noticeTime)).getText().toString();

                Intent intent = new Intent();
                intent.setClass(getActivity(),AddActivity.class);
                Bundle bundle = new Bundle();
                bundle.putLong("id", id);
                bundle.putString("title", title);
                bundle.putString("content", content);
                bundle.putString("noticeDate", noticeDate);
                bundle.putString("noticeTime", noticeTime);
                intent.putExtras(bundle);
                startActivity(intent);
            }

        });


        //添加备忘录按钮
        ImageView btnAdd =(ImageView) (rootView).findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),AddActivity.class);
                startActivity(intent);
            }
        });
        return rootView;
    }

    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo){
        menu.setHeaderIcon(R.drawable.alarm);
        menu.add(0,3,0,"修改");
        menu.add(0,4,0,"删除");
    }
    public boolean onContextItemSelected(MenuItem item){
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        switch(item.getItemId()){
            case 3:
                String title = ((TextView)menuInfo.targetView.findViewById(R.id.title)).getText().toString();
                String content = ((TextView)menuInfo.targetView.findViewById(R.id.content)).getText().toString();
                String noticeDate = ((TextView)menuInfo.targetView.findViewById(R.id.noticeDate)).getText().toString();
                String noticeTime = ((TextView)menuInfo.targetView.findViewById(R.id.noticeTime)).getText().toString();

                Intent intent = new Intent();
                intent.setClass(getActivity(),AddActivity.class);
                Bundle bundle = new Bundle();
                bundle.putLong("id", menuInfo.id);
                bundle.putString("title", title);
                bundle.putString("content", content);
                bundle.putString("noticeDate", noticeDate);
                bundle.putString("noticeTime", noticeTime);

                intent.putExtras(bundle);

                startActivity(intent);
                break;
            case 4:
                dbhelper = new DbHelper(getActivity(), "db_bwl", null, 1);
                db = dbhelper.getWritableDatabase();
                int status = db.delete("tb_bwl", "id=?", new String[]{""+menuInfo.id});
                if(status!=-1){
                    //删除后更新listview
                    Cursor cursor = db.query("tb_bwl", new String[]{"id as _id","title","content","noticeDate","noticeTime"}, null, null, null, null,null);
                    adapter.changeCursor(cursor);
//				adapter.notifyDataSetChanged();
                    Toast.makeText(getActivity(), "删除成功", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getActivity(), "删除失败", Toast.LENGTH_LONG).show();
                }

                break;

        }


        return true;
    }
    private void sendRequestWithOkHttp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();//创建一个OkHttp实例
                    Log.d("cccc", CityName);
                    Request request = new Request.Builder().url("https://api.seniverse.com/v3/weather/now.json?key=SrvH71t8JeTOXNLJP&location=" + CityName + "&language=zh-Hans&unit=c").build();//创建Request对象发起请求
                    Response response = client.newCall(request).execute();//创建call对象并调用execute获取返回的数据
                    String responseData = response.body().string();
                    parseJSONWithJSONObject(responseData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void parseJSONWithJSONObject(String jsonData) {//用JSONObect解析JSON数据
        try {
            JSONObject jsonObject = new JSONObject(jsonData);   //response为返回的String型json数据
            JSONArray results = jsonObject.getJSONArray("results");      //得到键为results的JSONArray
            JSONObject now = results.getJSONObject(0).getJSONObject("now");
            //JSONObject location = results.getJSONObject(0).getJSONObject("location");   //得到results数组第一个数据中键为location的JSONObject
            Weather = now.getString("text");
            //CityName = location.getString("name");                      //获得城市名
            Tempeature = now.getString("temperature");                    //获取温度
            Log.d("WEATHER", Weather);
            Log.d("tEMPERATURE", Tempeature);
            showResPonse();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showResPonse() {
        getActivity().runOnUiThread(new Runnable() {//切换到主线程,ui界面的更改不能出现在子线程
            @Override
            public void run() {
                weather.setText(Weather);
                temperature.setText(Tempeature);
            }
        });

    }

    private void Time() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("\"yyyy年MM月dd日 HH时mm分ss秒 EEE\"");// HH:mm:ss
//获取当前时间
        Date date = new Date(System.currentTimeMillis());
        timeText.setText(simpleDateFormat.format(date));

    }

    private void navigateTo(BDLocation location) {
        if (isFirstLocate) {
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);//缩放级别
            baiduMap.animateMapStatus(update);//传入到经纬度
            isFirstLocate = false;//防止多次调用
        }
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        baiduMap.setMyLocationData(locationData);
    }

    private void initLocation() {//设置更新时间的间隔

        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");
        option.setScanSpan(5000);//每隔五秒一次更新
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);//强制GPS
        option.setIsNeedAddress(true);//需要精确的信息
        mLocationClient.setLocOption(option);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
        mLocationClient.stop();
        baiduMap.setMyLocationEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Cursor cursor = db.query("tb_bwl", new String[]{"id as _id", "title", "content", "noticeDate", "noticeTime"}, null, null, null, null, null);
        adapter.changeCursor(cursor);
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    //
    class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation.getLocType() == bdLocation.TypeGpsLocation || bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                navigateTo(bdLocation);
            }
            StringBuilder currentPostion = new StringBuilder();
            currentPostion.append("纬度：").append(bdLocation.getLatitude()).append(" ");
            currentPostion.append("经度：").append(bdLocation.getLongitude()).append(" ");
            currentPostion.append("国家：").append(bdLocation.getCountry()).append(" ");
            currentPostion.append("省：").append(bdLocation.getProvince()).append(" ");
            currentPostion.append("市：").append(bdLocation.getCity()).append(" ");
            CityName = bdLocation.getCity();
            if(CityName.equals(""))
                CityName="北京";
            currentPostion.append("区：").append(bdLocation.getDistrict()).append(" ");
            currentPostion.append("街道：").append(bdLocation.getStreet()).append(" ");
            sendRequestWithOkHttp();
            if (bdLocation.getLocType() == bdLocation.TypeGpsLocation) {
                currentPostion.append("GPS");
            } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                currentPostion.append("网络");
            } else {
                currentPostion.append("无");
            }
            postionText.setText(currentPostion);
        }
    }

    /**
     * 我这里是在google地图取下来的wgs84坐标集合Const.googleWGS84，模拟的运动后获取的坐标集合，
     所以需要转化成百度坐标；实际应该是将定位sdk返回的位置点加入到位置集合中，
     定位sdk需要设置返回坐标为百度坐标：mOption.setCoorType("bd09ll")，这样就直接用，不用转换了。
     */


}