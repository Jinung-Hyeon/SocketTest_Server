package com.test.sokettestserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
            //Toast.makeText(context,"\"마을회관 DID\"가 구동 되었습니다.", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(context, MyForegroundService.class);
            //i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(i);
            }
        }
    }


}