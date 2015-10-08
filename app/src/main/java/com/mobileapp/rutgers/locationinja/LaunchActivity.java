package com.mobileapp.rutgers.locationinja;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.sql.Time;

public class LaunchActivity extends Activity {

    private Handler mHandler = new Handler();
    //test
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        Log.e("LocatioNinja", "In oncreate of launch activity");

        mHandler.postDelayed(new Runnable() {
            public void run() {
                Toast.makeText(LaunchActivity.this, "WELCOME!", Toast.LENGTH_SHORT).show();
                Log.e("LocatioNinja", "Out of launch activity");
                Intent mainIntent = new Intent(LaunchActivity.this, MapsActivity.class);
                startActivity(mainIntent);
            }
        }, 2500);
    }
}
