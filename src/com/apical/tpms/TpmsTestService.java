package com.apical.tpms;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import java.io.*;

public class TpmsTestService extends Service
{
    private static final String TAG = "TpmsTestService";

    private TpmsTestBinder        mBinder   = null;
    private TpmsTestActivity      mActivity = null;
    private PowerManager.WakeLock mWakeLock = null;
    private boolean  mStarted;
    private int      mCurTime;
    private tpms     mTpms;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        // binder
        mBinder = new TpmsTestBinder();

        // wake lock
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.setReferenceCounted(false);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mTpms.release();
        mTpms = null;
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
        public TpmsTestService getService(TpmsTestActivity activity) {
            mActivity = activity;
            if (mTpms == null) {
                mTpms = new tpms();
                mTpms.init("/dev/ttyS0", new tpms.TpmsEventListener() {
                    @Override
                    public void onTpmsEvent(int type, int i) {
                        Log.d(TAG, "onTpmsEvent");
                        mActivity.sendMessage(type, i);
                    }
                });
            }
            return TpmsTestService.this;
        }
    }

    public int tpmsHandShake() {
        if (mTpms != null) {
            return mTpms.handShake();
        }
        return -1;
    }

    public int tpmsRequestAlert(int i) {
        if (mTpms != null) {
            return mTpms.requestAlert(i);
        }
        return -1;
    }

    public int tpmsRequestTire(int i) {
        if (mTpms != null) {
            return mTpms.requestTire(i);
        }
        return -1;
    }

    public int tpmsMatchTire(int i) {
        if (mTpms != null) {
            return mTpms.learnTire(i);
        }
        return -1;
    }

    public int tpmsUnwatchTire(int i) {
        if (mTpms != null) {
            return mTpms.unwatchTire(i);
        }
        return -1;
    }

    public void tpmsGetParams(int t, int[] params) {
        if (mTpms != null) {
            mTpms.getParams(t, params);
        }
    }
}


