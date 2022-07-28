package com.test.sokettestserver;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import com.test.sokettestserver.MainActivity;

public class MyForegroundService extends Service {

    private static final String TAG = "ServerTest";

    // 사용자가 임의로 껐다는 신호를 구분하기위한 변수
    public static int clientSignal = 0;

    ServerSocket serverSocket;
    Socket socket;
    DataInputStream is;
    DataOutputStream os;

    UdpThread udpThread;


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
            udpThread = new UdpThread();
            udpThread.start();
            ServerSocketOpen(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return START_STICKY;

    }



    public void ServerSocketOpen(String port) throws IOException {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //서버 소켓 생성
                            serverSocket = new ServerSocket(Integer.parseInt(port));
                            //서버에 접속하는 클라이언트 소켓 얻어오기(클라이언트가 접속하면 클라이언트 소켓 리턴)
                            socket = serverSocket.accept(); //서버는 클라이언트가 접속할 때까지 여기서 대기. 접속하면 다음 코드로 넘어감
                            Log.d(TAG, "클라이언트가 접속했습니다.");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        while (true) {
                            try{
                                //클라이언트와 데이터를 주고 받기 위한 통로 구축
                                is = new DataInputStream(socket.getInputStream()); //클라이언트로부터 메세지를 받기 위한 통로
                                os = new DataOutputStream(socket.getOutputStream()); //클라이언트로부터 메세지를 보내기 위한 통로

                                int signal = is.read();
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                //Log.d(TAG, "consignal: " + signal);
                                if (signal == -1){  // is.read()에서 블록킹 상태로 기다리다가 클라이언트쪽 연결이 끊기면 -1을 리턴받음.(이걸로 클라이언트 단이 끊긴걸 알 수 있음)


                                    Log.d(TAG, "클라이언트의 접속이 끊겼습니다.");
                                    socket.close();

                                    switch (clientSignal){
                                        case 0: // 앱이 강제로 종료되었을때
                                            Log.d(TAG, "DID앱이 강제로 종료되었습니다. 다시 앱을 실행합니다.");
                                            getPackageList();
                                            break;
                                        case 1: // 관리자가 앱을 종료 시켰을때
                                            Log.d(TAG, "관리자가 앱을 종료했습니다. 다시 실행시키지 않습니다. clientSignal : " + clientSignal);
                                            break;
                                        case 2: // DID화면이 screen off가 되었을때
                                            Log.d(TAG, "DID화면이 꺼졌습니다. : " + clientSignal);
                                            break;
                                    }

                                    socket = serverSocket.accept(); //서버는 클라이언트가 접속할 때까지 여기서 대기. 접속하면 다음 코드로 넘어감
                                    clientSignal = 0;
                                    Log.d(TAG, "클라이언트가 다시 연결되었습니다. clientSignal : " + clientSignal);
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        //Thread.interrupted();
                    }



                }).start();
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

    public long goToSleepTime(){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 17);
        c.set(Calendar.MINUTE, 24);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTimeInMillis();
    }
}