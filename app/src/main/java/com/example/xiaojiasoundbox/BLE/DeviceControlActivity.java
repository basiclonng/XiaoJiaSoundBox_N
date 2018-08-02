package com.example.xiaojiasoundbox.BLE;

import com.example.xiaojiasoundbox.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceControlActivity extends Activity {

    private final String TAG = "DeviceControlActivity";
    private final int REQUEST_WIFISCANACTIVITY = 1;
    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;

    private ProgressBar mProgressBar;
    private TextView mTextViewConnecting;
    private TextView mTextViewDevicename;
    private Button mButtonSetWifi;
    private Button mButtonSetVolume;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder)service).getService();
            if(!mBluetoothLeService.initialize()){
                Log.e(TAG, "Unable to initialize BluetoothService");
                finish();
            }
            Log.i(TAG, "new ServiceConnection connect :" + mDeviceAddress + " taskid: "+android.os.Process.myTid());
            Toast.makeText(DeviceControlActivity.this, "new ServiceConnection connect", Toast.LENGTH_SHORT).show();
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            Log.i(TAG, "BroadcastReceiver : " + action);

            if(BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)){

                Toast.makeText(DeviceControlActivity.this, "connected", Toast.LENGTH_SHORT).show();
                updateUI(true);

            }else if(BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)){

                //updateUI(false);

            }else if(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)){

            }else if(BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)){

                Toast.makeText(DeviceControlActivity.this, "!!==ACTION_DATA_AVAILABLE==!!", Toast.LENGTH_SHORT).show();

            }
            else if(BluetoothLeService.ACTION_GATT_133.equals(action)){
                //Toast.makeText(DeviceControlActivity.this, "E133 please restart your bluetooth device!!", Toast.LENGTH_SHORT).show();


                if (mBluetoothManager == null) {
                    mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                    if (mBluetoothManager == null) {
                        Log.e(TAG, "Unable to initialize BluetoothManager.");
                        //return false;
                    }
                }

                mBluetoothAdapter = mBluetoothManager.getAdapter();
                if (mBluetoothAdapter == null) {
                    Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
                    //return false;
                }

                Toast.makeText(DeviceControlActivity.this, "请重新打开蓝牙~.~", Toast.LENGTH_LONG).show();

                Log.e(TAG, "mBluetoothAdapter 11111111");
                mBluetoothAdapter.disable();
                Log.e(TAG, "mBluetoothAdapter 22222222");
                mBluetoothAdapter.enable();
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 2);
                Log.e(TAG, "mBluetoothAdapter 33333333");

                finish();

//                AlertDialog dialog = new AlertDialog.Builder(DeviceControlActivity.this)
//                        //.setIcon(R.mipmap.icon)//设置标题的图片
//                        .setTitle("E133 BLE蓝牙设备系统错误")//设置对话框的标题
//                        .setMessage("1.进入手机系统设置\n2.重启手机蓝牙\n3.重新尝试连接BLE设备")//设置对话框的内容
//                        //设置对话框的按钮
//                        //.setNegativeButton("取消", new DialogInterface.OnClickListener(){
//                        //    @Override
//                        //    public void onClick(DialogInterface dialog, int which) {
//                        //        Toast.makeText(DeviceControlActivity.this, "点击了取消按钮", Toast.LENGTH_SHORT).show();
//                        //        dialog.dismiss();
//                        //        finish();
//                         //   }
//                        //})
//                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                Toast.makeText(DeviceControlActivity.this, "请进系统设置重启蓝牙!", Toast.LENGTH_LONG).show();
//                                dialog.dismiss();
//                                finish();
//                            }
//                        }).create();
//                dialog.show();
            }
        }
    };

//    private void getBatteryCharacteristic() {
//        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mBeaconBatteryStateReader = new BeaconBatteryStateReader(
//                        BeaconDetailsActivity.this,
//                        mBeacon.getMacAddress());
//                mBeaconBatteryStateReader.readBatteryState(BeaconDetailsActivity.this);
//            }
//        }, 100);
//    }
    private final Handler mStartGattHandler = new Handler();
    private final Runnable mStartGattRunnable = new Runnable() {
        @Override
        public void run() {
            Intent gattServiceIntent = new Intent(DeviceControlActivity.this, BluetoothLeService.class);
            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        }
    };

    private final Runnable mWaitGattServiceCharacteristic = new Runnable() {
        int cnt = 0;
        byte[] ch = new byte[100];

        @Override
        public void run() {

            Log.i(TAG, "mWaitGattServiceCharacteristic  cnt: "+cnt + " taskid: " + android.os.Process.myTid());
            cnt++;
            mStartGattHandler.postDelayed(this, 20000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.i(TAG, "onCreate$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$"+TAG);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);
        mDeviceName = getIntent().getStringExtra(CastpalBLEConstants.DEVICE_NAME);
        mDeviceAddress = getIntent().getStringExtra(CastpalBLEConstants.DEVICE_ADDRESS);

        mProgressBar = findViewById(R.id.device_control_progressBar);
        mTextViewConnecting = findViewById(R.id.device_control_connecting);
        mTextViewDevicename = findViewById(R.id.device_control_devicename);
        if(mDeviceName != null){
            mTextViewDevicename.setText(mDeviceName);
        }else{
            mTextViewDevicename.setText(R.string.default_device_name);
        }
        mButtonSetWifi = findViewById(R.id.device_control_setwifi);
        mButtonSetVolume = findViewById(R.id.device_control_setvolumn);
        updateUI(false);
        mButtonSetWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeviceControlActivity.this, WifiScanActivity.class);
                startActivityForResult(intent, REQUEST_WIFISCANACTIVITY);
            }
        });

//        Intent intentGATTService = new Intent(this, BluetoothLeService.class);
//        bindService(intentGATTService, mServiceConnection, BIND_AUTO_CREATE);
        mStartGattHandler.postDelayed(mStartGattRunnable, 500);
        //mStartGattHandler.postDelayed(mWaitGattServiceCharacteristic, 2000);
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$"+TAG);
        super.onResume();
        registerReceiver(mBroadcastReceiver, makeGattUpdateIntentFilter());
        if(mBluetoothLeService != null){
            Log.i(TAG, "onResume connect :" + mDeviceAddress);
            Toast.makeText(DeviceControlActivity.this, "onResume connect", Toast.LENGTH_SHORT).show();
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "connect result " + result);
        }
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$"+TAG);
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$"+TAG);
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        mStartGattHandler.removeCallbacks(mStartGattRunnable);
        mStartGattHandler.removeCallbacks(mWaitGattServiceCharacteristic);
    }

    private static IntentFilter makeGattUpdateIntentFilter(){
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_133);
        return intentFilter;
    }

    private void updateUI(boolean contected){
        mProgressBar = findViewById(R.id.device_control_progressBar);
        mTextViewConnecting = findViewById(R.id.device_control_connecting);
        mTextViewDevicename = findViewById(R.id.device_control_devicename);
        mButtonSetWifi = findViewById(R.id.device_control_setwifi);
        mButtonSetVolume = findViewById(R.id.device_control_setvolumn);

        if(contected == true){
            mProgressBar.setVisibility(View.GONE);
            mTextViewConnecting.setVisibility(View.GONE);
            mTextViewDevicename.setVisibility(View.VISIBLE);
            mButtonSetWifi.setVisibility(View.VISIBLE);
            mButtonSetVolume.setVisibility(View.VISIBLE);
        }else{
            mProgressBar.setVisibility(View.VISIBLE);
            mTextViewConnecting.setVisibility(View.VISIBLE);
            mTextViewDevicename.setVisibility(View.GONE);
            mButtonSetWifi.setVisibility(View.GONE);
            mButtonSetVolume.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(REQUEST_WIFISCANACTIVITY == requestCode){
            if(RESULT_OK == resultCode){
                final String ssid = data.getStringExtra(CastpalBLEConstants.SSID);
                final String pwd = data.getStringExtra(CastpalBLEConstants.PWD);
                byte[] ch = new byte[85];
                ch[0] = 0x01;
                char[] str = "amlogicblewifisetup".toCharArray();

                for (int i = 0; i < 19; i++) {
                    ch[i + 1] = (byte) str[i];
                }

                for (int i = 0; i < 32; i++) {
                    if (i < ssid.length()) {
                        ch[i + 1 + 19] = (byte) ssid.charAt(i);
                    } else
                        ch[i + 1 + 19] = 0;
                }
                for (int i = 0; i < 32; i++) {
                    if (i < pwd.length()) {
                        ch[i + 1 + 19 + 32] = (byte) pwd.charAt(i);
                    } else
                        ch[i + 1 + 19 + 32] = 0;
                }
                ch[19 + 32 + 32 + 1] = 0x04;
                Log.w(TAG, "--write wifi info>>> ");
                mBluetoothLeService.writeCharacteristic(SampleGattAttributes.BLE_UUID_BLE_SERVICE, SampleGattAttributes.BLE_UUID_BLE_SERVICE_WIFI_CHAR,ch );
                Log.w(TAG, "--write wifi info<<< ");
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
