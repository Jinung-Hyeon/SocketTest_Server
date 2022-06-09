package com.test.sokettestserver;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class MyService extends Service {

    ServerSocket serverSocket;
    Socket socket;
    DataInputStream is;
    DataOutputStream os;
    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String port = intent.getStringExtra("PortNumber");
        try {
            ServerSocketOpen(port);
            getPackageList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return START_STICKY;
    }

    public void ServerSocketOpen(String port) throws IOException {
        //Android API14 버전 이상부터 네트워크 작업은 무조건 별도의 Thread에서 실행 해야함.
        new Thread((Runnable) () -> {
            try {
                //서버 소켓 생성
                serverSocket = new ServerSocket(Integer.parseInt(port));
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                while (true){
                    int toggle = 0;
                    int signal;
                    //서버에 접속하는 클라이언트 소켓 얻어오기(클라이언트가 접속하면 클라이언트 소켓 리턴)
                    if(toggle == 0){
                        socket = serverSocket.accept(); //서버는 클라이언트가 접속할 때까지 여기서 대기. 접속하면 다음 코드로 넘어감
                        //클라이언트와 데이터를 주고 받기 위한 통로 구축
                        is = new DataInputStream(socket.getInputStream()); //클라이언트로부터 메세지를 받기 위한 통로
                        os = new DataOutputStream(socket.getOutputStream()); //클라이언트로부터 메세지를 보내기 위한 통로
                        Log.d("connnnn", "연결완료");
                        toggle++;
                    }
                    signal = is.read();
                    if (signal == -1) {
                        Log.d("connnnn", "연결해제");
                        toggle = 0;
                        getPackageList();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    //다른 앱을 실행시켜주는 메소드
    public void getPackageList() {
        boolean isExist = false;

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
}