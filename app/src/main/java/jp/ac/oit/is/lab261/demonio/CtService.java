package jp.ac.oit.is.lab261.demonio;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class CtService extends Service {
    private static boolean a=false;
    @Override
    public void onCreate(){
        a=true;
    }
    public void onDestroy(){
        a=false;
    }

    public static boolean isA() {
        return a;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent){
        return null;
    }
}
