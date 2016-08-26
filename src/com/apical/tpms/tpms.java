package com.apical.tpms;

import android.util.Log;

public class tpms {
    private static final String TAG = "tpms";

    public static final int TPMS_TYPE_ALERT   = 0x62;
    public static final int TPMS_TYPE_TIRES   = 0x63;
    public static final int TPMS_TYPE_UNWATCH = 0x65;
    public static final int TPMS_TYPE_LEARN   = 0x66;
    public static final int MAX_TIRES_NUM     = 5;
    public static final int MAX_ALERT_NUM     = 6;

    public interface TpmsEventListener {
        public void onTpmsEvent(int type, int i);
    }

    private long              mTpmsContext;
    private TpmsEventListener mListener;

    public void init(String dev, TpmsEventListener l) {
        mTpmsContext = nativeInit(dev);
        mListener    = l;
        nativeInitCallback(mTpmsContext);
    }

    public void release() {
        nativeFree(mTpmsContext);
    }

    public int handShake() {
        return nativeHandShake(mTpmsContext);
    }

    public int configAlert(int i, int hot, int low) {
        return nativeConfigAlert(mTpmsContext, i, hot, low);
    }

    public int configAlert(int[] alerts) {
        return nativeConfigAlert(mTpmsContext, alerts);
    }

    public int requestAlert(int i) {
        return nativeRequestAlert(mTpmsContext, i);
    }

    public int requestTire(int i) {
        return nativeRequestTire(mTpmsContext, i);
    }

    public int unwatchTire(int i) {
        return nativeUnwatchTire(mTpmsContext, i);
    }

    public int learnTire(int i) {
        return nativeLearnTire(mTpmsContext, i);
    }

    public void getParams(int t, int[] params) {
        nativeGetParams(mTpmsContext, t, params);
    }

    private void internalCallback(int t, int i) {
        if (mListener != null) {
            mListener.onTpmsEvent(t, i);
        }
    }

    private native void nativeInitCallback(long ctxt);

    private static native long nativeInit(String dev);
    private static native void nativeFree(long  ctxt);

    private static native int  nativeHandShake   (long ctxt);
    private static native int  nativeConfigAlert (long ctxt, int i, int hot, int low);
    private static native int  nativeConfigAlert (long ctxt, int[] alerts);
    private static native int  nativeRequestAlert(long ctxt, int i);
    private static native int  nativeRequestTire (long ctxt, int i);
    private static native int  nativeUnwatchTire (long ctxt, int i);
    private static native int  nativeLearnTire   (long ctxt, int i);
    private static native int  nativeGetParams   (long ctxt, int t, int[] params);

    static {
        System.loadLibrary("tpms_jni");
    }
}







