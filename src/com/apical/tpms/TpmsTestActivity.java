package com.apical.tpms;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import java.util.*;

public class TpmsTestActivity extends Activity {
    private static final String TAG = "TpmsTestActivity";

    private TpmsTestService mTpmsServ = null;
    private ServiceConnection mTpmsServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder serv) {
            mTpmsServ = ((TpmsTestService.TpmsTestBinder)serv).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mTpmsServ = null;
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // start record service
        Intent i = new Intent(TpmsTestActivity.this, TpmsTestService.class);
        startService(i);

        // bind record service
        bindService(i, mTpmsServiceConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        // unbind record service
        unbindService(mTpmsServiceConn);

        // stop record service
        Intent i = new Intent(TpmsTestActivity.this, TpmsTestService.class);
        stopService(i);

        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}



