package com.test.sokettestserver;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "SimpleTest";
    TextView ipText, portText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ipText = findViewById(R.id.ip);
        portText = findViewById(R.id.port);


        //내 아이피 확인 및 세팅
        try {
            ipText.setText(getLocalAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        if(!foregroundServiceRunning()){
            Intent serviceIntent = new Intent(this, MyForegroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                startForegroundService(serviceIntent);
            }
        }
        getPackageList();

    }

    //포그라운드 서비스가 실행중인지 확인하는 메소드
    public boolean foregroundServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)){
            if (MyForegroundService.class.getName().equals(service.service.getClassName())){
                return true;
            }
        }
        return false;
    }

    //내 IP얻기
    private String getLocalAddress() throws UnknownHostException{
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        assert wifiManager != null;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();
        return InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()).getHostAddress();
    }

    /*
    public void ServerSocketOpen(View view){
        if(portText.getText().toString() == null){
            Toast.makeText(this, "포트번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,"Socket Open", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MyForegroundService.class);
            intent.putExtra("PortNumber", portText.getText().toString());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startService(intent);
        }
    }*/

    //다른 앱을 실행시켜주는 메소드
    public void getPackageList() {
        //SDK30이상은 Manifest권한 추가가 필요 출처:https://inpro.tistory.com/214
        PackageManager pkgMgr = getPackageManager();
        List<ResolveInfo> mApps;
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mApps = pkgMgr.queryIntentActivities(mainIntent, 0);

        try {
            for (int i = 0; i < mApps.size(); i++) {
                if(mApps.get(i).activityInfo.packageName.startsWith("com.test.sockettestclient")){
                    Log.d("start", "실행시킴");
                    break;
                }
            }
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.test.sockettestclient");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    //클라이언트 단에서 앱을 실행시켜주면 이 부분 실행
    @Override
    protected void onResume() {
        super.onResume();
        getPackageList();
        Log.d(TAG, "onResume: ");
    }

    //뒤로가기 버튼누르면 이 부분 실행
    @Override
    protected void onPause() {
        super.onPause();
        getPackageList();
        Log.d(TAG, "onPause: ");
    }

}