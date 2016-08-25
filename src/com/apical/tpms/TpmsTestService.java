package com.apical.tpms;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import java.io.*;

public class TpmsTestService extends Service
{
    private static final String TAG = "TpmsTestService";

    private TpmsTestBinder        mBinder   = null;
    private PowerManager.WakeLock mWakeLock = null;
    private boolean  mStarted;
    private int      mCurTime;
    private tpms     mTpms;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        // binder
        mBinder = new TpmsTestBinder();

        // tpms
        /*
        mTpms = new tpms();
        mTpms.init("/dev/ttyS0");
        mTpms.requestAlert(0);
        mTpms.requestTire (0);
        */

        // wake lock
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.setReferenceCounted(false);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
//      mTpms.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
//      return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    public class TpmsTestBinder extends Binder {
        public TpmsTestService getService() {
            return TpmsTestService.this;
        }
    }
}


