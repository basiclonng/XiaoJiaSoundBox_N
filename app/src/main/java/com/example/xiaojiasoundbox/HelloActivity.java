package com.example.xiaojiasoundbox;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class HelloActivity extends AppCompatActivity {

	private final String TAG = "HelloActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.i(TAG, "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$"+TAG);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainActivity = new Intent(HelloActivity.this, MainActivity.class);
                startActivity(mainActivity);
                HelloActivity.this.finish();
            }
        }, 3000);
    }
}
