package jp.ac.oit.is.lab261.demonio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        //端末起動時にサービスを起動する
        Log.d("MyReceiver", "onReceive");
        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
            context.startService(new Intent(context, TestService.class));
        }
    }
}
