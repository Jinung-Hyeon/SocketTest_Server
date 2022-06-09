package com.test.sokettestserver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public class MainActivity extends AppCompatActivity {
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

    }

    //내 IP얻기
    private String getLocalAddress() throws UnknownHostException{
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        assert wifiManager != null;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();
        return InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()).getHostAddress();
    }

    public void ServerSocketOpen(View view){
        if(portText.getText().toString() == null){
            Toast.makeText(this, "포트번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,"Socket Open", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MyService.class);
            intent.putExtra("PortNumber", portText.getText().toString());
            startService(intent);
        }
    }


}