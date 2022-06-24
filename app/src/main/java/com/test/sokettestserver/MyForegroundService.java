package com.test.sokettestserver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import com.test.sokettestserver.MainActivity;

public class MyForegroundService extends Service {

    private static final String TAG = "MyMsg";
    ServerSocket serverSocket;
    Socket socket;
    DataInputStream is;
    DataOutputStream os;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.d(TAG, "onStartCommand  : 리시버타고 넘어옴");
        getPackageList();
        final String port = "5001";

        final String CHANNEL_ID = "Foreground Service ID";
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_ID,
                NotificationManager.IMPORTANCE_LOW
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getSystemService(NotificationManager.class).createNotificationChannel(channel);
                Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID)
                        .setContentText("Service is running")
                        .setContentTitle("Service enabled")
                        .setSmallIcon(R.drawable.ic_launcher_background);
                startForeground(1001, notification.build());
            }
        }

        try {
            ServerSocketOpen(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return super.START_STICKY;
    }


    public void ServerSocketOpen(String port) throws IOException {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //서버 소켓 생성
                            serverSocket = new ServerSocket(Integer.parseInt(port));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        while (true) {
                            try{
                                int toggle = 0;
                                //서버에 접속하는 클라이언트 소켓 얻어오기(클라이언트가 접속하면 클라이언트 소켓 리턴)
                                if(toggle == 0){
                                    socket = serverSocket.accept(); //서버는 클라이언트가 접속할 때까지 여기서 대기. 접속하면 다음 코드로 넘어감
                                    //클라이언트와 데이터를 주고 받기 위한 통로 구축
                                    is = new DataInputStream(socket.getInputStream()); //클라이언트로부터 메세지를 받기 위한 통로
                                    os = new DataOutputStream(socket.getOutputStream()); //클라이언트로부터 메세지를 보내기 위한 통로
                                    int signal = is.read();
                                    Log.d("signal", "consignal: " + signal);
                                    Log.d("connnnn", "연결완료");
                                    toggle++;
                                    if (signal == -1) {
                                        Log.d("signal", "signal: " + signal);
                                        Log.d("connnnn", "연결해제");
                                        toggle = 0;
                                        getPackageList();
                                        //runAppPackage("com.test.sockettestclient/.MainActivity ");
                                    } else if (signal == 1) {
                                        serverSocket.close();
                                        MainActivity.signal = 1;
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            stopForeground(true);
                                            stopSelf();
                                        }
                                        break;
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        Thread.interrupted();
                    }
                }
        ).start();
    }


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
                    Log.d(TAG, "실행시킴");
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