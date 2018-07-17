package jp.ac.oit.is.lab261.demonio;

import android.app.Service;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MainActivity","onCreate()");
        Intent test = new Intent(getApplication(), TestService.class);
        startService(test);
        if(ControlService.isA()){
            //起動している
        }else{
            //起動していない
            Intent intent=new Intent(getApplicationContext(),ControlService.class);
            startService(intent);
        }
    }

}