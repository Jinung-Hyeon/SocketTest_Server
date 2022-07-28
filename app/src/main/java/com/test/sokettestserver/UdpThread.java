package com.test.sokettestserver;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UdpThread extends Thread{
    private static final String TAG = "msgmsg";

    final int port = 5001;

    String msg;
    MyForegroundService mf = new MyForegroundService();


    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(port);

            while (true){
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                msg = new String(packet.getData(), 0, packet.getLength());
                mf.clientSignal = Integer.parseInt(msg);
                Log.d(TAG, "클라이언트 메시지 : " + msg + " signal : " + mf.clientSignal);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
