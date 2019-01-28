package jp.ac.oit.is.lab261.demonio;

//import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
//import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.bluetooth.BluetoothAdapter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends  AppCompatActivity implements Runnable, View.OnClickListener {
    private static final String TAG = "LocationData";
    private static final int REQUEST_ENABLEBLUETOOTH = 1; // Bluetooth機能の有効化要求時の識別コード
    private BluetoothAdapter mBluetoothAdapter;    // BluetoothAdapter : Bluetooth処理で必要
    private static final int REQUEST_CODE_PERMISSION = 2;
    private BluetoothDevice mBluetoothDevice;
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final String DEVICE_NAME = "HC-06";
    private BluetoothSocket mBluetoothSocket;

    private Button startButton;
    private Button stopButton;
    private TextView mStatusTextView;
    private TextView mInputTextView;
    private static final int VIEW_STATUS = 0;
    private static final int VIEW_INPUT = 1;
    private boolean connectFlg = false;
    private boolean isRunning = false;
    OutputStream mmOutputStream = null;
    String Data = "";
    public static final int PREFERENCE_INIT = 0;
    public static final int PREFERENCE_BOOTED = 1;
    SharedPreferences USER_DATA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       // if (ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        //    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
          //      Toast.makeText(this, "パーミッションがOFFになっています。", Toast.LENGTH_SHORT).show();

            //} else {
              //  ActivityCompat.requestPermissions(this,
                //        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                  //      REQUEST_CODE_PERMISSION);
           // }
            //return;
       // }

        mInputTextView = findViewById(R.id.inputValue);
        mStatusTextView = findViewById(R.id.statusValue);

        startButton = findViewById(R.id.start);
        startButton.setOnClickListener(this);

        stopButton = findViewById(R.id.stop);
        stopButton.setOnClickListener(this);

        Intent noti = new Intent(getApplication(), NotiService.class);
        startService(noti);

        // Bluetoothアダプタの取得
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (null == mBluetoothAdapter) {    // Android端末がBluetoothをサポートしていない
            Toast.makeText(this, R.string.bluetooth_is_not_supported, Toast.LENGTH_SHORT).show();
            finish();    // アプリ終了宣言
            return;
        }

        if (CtService.isA()) {
            //起動している
            Log.d("MainActivity", "CtService");
        } else {
            //起動していない
            Intent intent = new Intent(getApplicationContext(), CtService.class);
            startService(intent);
        }

        Log.d("MainActivity", "onCreate()");
    }

    // Android端末のBluetooth機能の有効化要求
    private void requestBluetoothFeature() {
        Log.d("MainActivity", "requestBluetoothFeature()");
        if (mBluetoothAdapter.isEnabled()) return;
        // デバイスのBluetooth機能が有効になっていないときは、有効化要求（ダイアログ表示）
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLEBLUETOOTH);
    }

    // 機能の有効化ダイアログの操作結果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("MainActivity", "onActivityResult()");
        switch (requestCode) {
            case REQUEST_ENABLEBLUETOOTH: // Bluetooth有効化要求
                if (Activity.RESULT_CANCELED == resultCode) {    // 有効にされなかった
                    Toast.makeText(this, R.string.bluetooth_is_not_working, Toast.LENGTH_SHORT).show();
                    finish();    // アプリ終了宣言
                    return;
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    // 初回表示時、および、ポーズからの復帰時
    @Override
    protected void onResume() {
        super.onResume();

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        final EditText editView = new EditText(this);
        USER_DATA = getSharedPreferences("USER_DATA", MODE_PRIVATE);


        // ダイアログの設定
        alertDialog.setTitle("ユーザ登録");          //タイトル
        alertDialog.setMessage("ユーザ名を入力してください");      //内容
        alertDialog.setView(editView);
        alertDialog.setIcon(android.R.drawable.ic_menu_info_details);

        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // エディットテキストのテキストを取得
                String text = editView.getText().toString();
                // 入力文字列を"inputUser_id"に書き込む
                SharedPreferences.Editor editor = USER_DATA.edit();
                editor.putString("inputUser_id", text);
                editor.apply();
                Log.d("USER_DATA=", text);
                //初回表示完了
                setState(PREFERENCE_BOOTED);

            }
        });

        // ダイアログの作成と表示
        if(PREFERENCE_INIT == getState() ){
            //初回起動時のみ表示する
            alertDialog.create();
            alertDialog.show();
        }


            Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : devices) {

                if (device.getName().equals(DEVICE_NAME)) {
                    mBluetoothDevice = device;
                }
            }

            Log.d("MainActivity", "onResume()");
            // Android端末のBluetooth機能の有効化要求
            requestBluetoothFeature();

    }

    @Override
    protected void onPause() {
        super.onPause();

            Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : devices) {

                if (device.getName().equals(DEVICE_NAME)) {
                    mBluetoothDevice = device;
                }
            }
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();

    }

    @Override
    public void run() {
        InputStream mmInStream = null;

        Message valueMsg = new Message();
        valueMsg.what = VIEW_STATUS;
        valueMsg.obj = "接続しています...";
        mHandler.sendMessage(valueMsg);

        SharedPreferences USER_DATA = getSharedPreferences("USER_DATA", MODE_PRIVATE);
        String id_Load = USER_DATA.getString("inputUser_id", null);

        try {

            // 取得したデバイス名を使ってBluetoothでSocket接続
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
            mBluetoothSocket.connect();
            mmInStream = mBluetoothSocket.getInputStream();
            mmOutputStream = mBluetoothSocket.getOutputStream();

            // InputStreamのバッファを格納
            byte[] buffer = new byte[1024];

            // 取得したバッファのサイズを格納
            int bytes;
            valueMsg = new Message();
            valueMsg.what = VIEW_STATUS;
            valueMsg.obj = "接続中";
            mHandler.sendMessage(valueMsg);

            connectFlg = true;
            String sensorData = "";

            while (isRunning) {
                // InputStreamの読み込み
                bytes = mmInStream.read(buffer);
                Log.i(TAG, "bytes=" + bytes);
                // String型に変換
                String readMsg = new String(buffer, 0, bytes);

                if (!readMsg.startsWith("$") || sensorData.equals("")) {
                    sensorData = sensorData.concat(readMsg);
                    Log.d("sensorData" , sensorData);
                } else if (readMsg.startsWith("$") && !sensorData.equals("")) {

                    valueMsg = new Message();
                    valueMsg.what = VIEW_INPUT;
                    valueMsg.obj = readMsg;
                    Data = id_Load + "," + sensorData;
                    Log.d("Data=", Data);

                    try {
                        HttpPostTask httpPostTask = new HttpPostTask(this);
                        httpPostTask.setLatitude(Data);
                        httpPostTask.execute(new URL("http://150.89.240.238/android/dbInsert.php"));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    sensorData = "";
                }
            }
        }

        // エラー処理
        catch (Exception e) {
            valueMsg = new Message();
            valueMsg.what = VIEW_STATUS;
            //valueMsg.obj = "Error1:" + e;
            valueMsg.obj = "停止しています";
            mHandler.sendMessage(valueMsg);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.equals(startButton)) {
            Thread mThread;
            // 接続されていない場合のみ
            if (!connectFlg) {
                mThread = new Thread(this);
                // Threadを起動し、Bluetooth接続
                isRunning = true;
                mThread.start();
            }
        }
        if (v.equals(stopButton)) {
            isRunning = false;
            connectFlg = false;
            try {
                mBluetoothSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Toast.makeText(this, R.string.stop2, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Stop");
        }
    }



    //データ保存
    private void setState(int state) {
        // SharedPreferences設定を保存
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().putInt("InitState", state).apply();

    }

    //データ読み出し
    private int getState() {
        // 読み込み
        int state;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        state = sp.getInt("InitState", PREFERENCE_INIT);

        return state;
    }



    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int action = msg.what;
            String msgStr = (String) msg.obj;
            if (action == VIEW_INPUT) {
                mInputTextView.setText(msgStr);
            } else if (action == VIEW_STATUS) {
                mStatusTextView.setText(msgStr);
            }
        }
    };

}