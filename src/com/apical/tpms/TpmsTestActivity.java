package com.apical.tpms;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

public class TpmsTestActivity extends Activity {
    private static final String  TAG = "TpmsTestActivity";
    private static final boolean HIDE_REFRESH_BUTTONS = true;

    private int      mTpmsFuncRet;
    private int[]    mTpmsAlerts = new int[tpms.MAX_ALERT_NUM * 2];
    private int[]    mTpmsTires  = new int[tpms.MAX_TIRES_NUM * 4];
    private boolean  mResumeFlag = false;
    private Button   mBtnHandShake;
    private Button   mBtnRefreshAll;
    private Button   mBtnRefreshTire1;
    private Button   mBtnRefreshTire2;
    private Button   mBtnRefreshTire3;
    private Button   mBtnRefreshTire4;
    private Button   mBtnRefreshTire5;
    private Button   mBtnMatchTire1;
    private Button   mBtnMatchTire2;
    private Button   mBtnMatchTire3;
    private Button   mBtnMatchTire4;
    private Button   mBtnMatchTire5;
    private Button   mBtnUnwatchTire1;
    private Button   mBtnUnwatchTire2;
    private Button   mBtnUnwatchTire3;
    private Button   mBtnUnwatchTire4;
    private Button   mBtnUnwatchTire5;
    private Button   mBtnRefreshAlert1;
    private Button   mBtnRefreshAlert2;
    private Button   mBtnRefreshAlert3;
    private Button   mBtnRefreshAlert4;
    private Button   mBtnRefreshAlert5;
    private Button   mBtnRefreshAlert6;
    private Button   mBtnRefreshTireAll;
    private Button   mBtnMatchTireAll;
    private Button   mBtnUnwatchTireAll;
    private Button   mBtnRefreshAlertAll;
    private TextView mTxtTpmsStatus;
    private TextView mTxtTpmsTire1;
    private TextView mTxtTpmsTire2;
    private TextView mTxtTpmsTire3;
    private TextView mTxtTpmsTire4;
    private TextView mTxtTpmsTire5;
    private TextView mTxtTpmsAlert1;
    private TextView mTxtTpmsAlert2;
    private TextView mTxtTpmsAlert3;
    private TextView mTxtTpmsAlert4;
    private TextView mTxtTpmsAlert5;
    private TextView mTxtTpmsAlert6;

    private TpmsTestService mTpmsServ = null;
    private ServiceConnection mTpmsServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder serv) {
            mTpmsServ = ((TpmsTestService.TpmsTestBinder)serv).getService(TpmsTestActivity.this);
            mTpmsFuncRet = mTpmsServ.tpmsRequestAlert(0);
            mTpmsFuncRet = mTpmsServ.tpmsRequestTire (0);
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

        mBtnHandShake      = (Button)findViewById(R.id.btn_refresh_status   );
        mBtnRefreshAll     = (Button)findViewById(R.id.btn_refresh_all      );
        mBtnRefreshTire1   = (Button)findViewById(R.id.btn_refresh_tire1    );
        mBtnRefreshTire2   = (Button)findViewById(R.id.btn_refresh_tire2    );
        mBtnRefreshTire3   = (Button)findViewById(R.id.btn_refresh_tire3    );
        mBtnRefreshTire4   = (Button)findViewById(R.id.btn_refresh_tire4    );
        mBtnRefreshTire5   = (Button)findViewById(R.id.btn_refresh_tire5    );
        mBtnMatchTire1     = (Button)findViewById(R.id.btn_match_tire1      );
        mBtnMatchTire2     = (Button)findViewById(R.id.btn_match_tire2      );
        mBtnMatchTire3     = (Button)findViewById(R.id.btn_match_tire3      );
        mBtnMatchTire4     = (Button)findViewById(R.id.btn_match_tire4      );
        mBtnMatchTire5     = (Button)findViewById(R.id.btn_match_tire5      );
        mBtnUnwatchTire1   = (Button)findViewById(R.id.btn_unwatch_tire1    );
        mBtnUnwatchTire2   = (Button)findViewById(R.id.btn_unwatch_tire2    );
        mBtnUnwatchTire3   = (Button)findViewById(R.id.btn_unwatch_tire3    );
        mBtnUnwatchTire4   = (Button)findViewById(R.id.btn_unwatch_tire4    );
        mBtnUnwatchTire5   = (Button)findViewById(R.id.btn_unwatch_tire5    );
        mBtnRefreshAlert1  = (Button)findViewById(R.id.btn_refresh_alert1   );
        mBtnRefreshAlert2  = (Button)findViewById(R.id.btn_refresh_alert2   );
        mBtnRefreshAlert3  = (Button)findViewById(R.id.btn_refresh_alert3   );
        mBtnRefreshAlert4  = (Button)findViewById(R.id.btn_refresh_alert4   );
        mBtnRefreshAlert5  = (Button)findViewById(R.id.btn_refresh_alert5   );
        mBtnRefreshAlert6  = (Button)findViewById(R.id.btn_refresh_alert6   );
        mBtnRefreshTireAll = (Button)findViewById(R.id.btn_refresh_tire_all );
        mBtnMatchTireAll   = (Button)findViewById(R.id.btn_match_tire_all   );
        mBtnUnwatchTireAll = (Button)findViewById(R.id.btn_unwatch_tire_all );
        mBtnRefreshAlertAll= (Button)findViewById(R.id.btn_refresh_alert_all);
        mBtnHandShake      .setOnClickListener(mOnClickListener);
        mBtnRefreshAll     .setOnClickListener(mOnClickListener);
        mBtnRefreshTire1   .setOnClickListener(mOnClickListener);
        mBtnRefreshTire2   .setOnClickListener(mOnClickListener);
        mBtnRefreshTire3   .setOnClickListener(mOnClickListener);
        mBtnRefreshTire4   .setOnClickListener(mOnClickListener);
        mBtnRefreshTire5   .setOnClickListener(mOnClickListener);
        mBtnMatchTire1     .setOnClickListener(mOnClickListener);
        mBtnMatchTire2     .setOnClickListener(mOnClickListener);
        mBtnMatchTire3     .setOnClickListener(mOnClickListener);
        mBtnMatchTire4     .setOnClickListener(mOnClickListener);
        mBtnMatchTire4     .setOnClickListener(mOnClickListener);
        mBtnUnwatchTire1   .setOnClickListener(mOnClickListener);
        mBtnUnwatchTire2   .setOnClickListener(mOnClickListener);
        mBtnUnwatchTire3   .setOnClickListener(mOnClickListener);
        mBtnUnwatchTire4   .setOnClickListener(mOnClickListener);
        mBtnUnwatchTire5   .setOnClickListener(mOnClickListener);
        mBtnRefreshAlert1  .setOnClickListener(mOnClickListener);
        mBtnRefreshAlert2  .setOnClickListener(mOnClickListener);
        mBtnRefreshAlert3  .setOnClickListener(mOnClickListener);
        mBtnRefreshAlert4  .setOnClickListener(mOnClickListener);
        mBtnRefreshAlert5  .setOnClickListener(mOnClickListener);
        mBtnRefreshAlert6  .setOnClickListener(mOnClickListener);
        mBtnRefreshTireAll .setOnClickListener(mOnClickListener);
        mBtnMatchTireAll   .setOnClickListener(mOnClickListener);
        mBtnUnwatchTireAll .setOnClickListener(mOnClickListener);
        mBtnRefreshAlertAll.setOnClickListener(mOnClickListener);

        mTxtTpmsStatus = (TextView)findViewById(R.id.txt_tpms_status);
        mTxtTpmsTire1  = (TextView)findViewById(R.id.txt_tire1 );
        mTxtTpmsTire2  = (TextView)findViewById(R.id.txt_tire2 );
        mTxtTpmsTire3  = (TextView)findViewById(R.id.txt_tire3 );
        mTxtTpmsTire4  = (TextView)findViewById(R.id.txt_tire4 );
        mTxtTpmsTire5  = (TextView)findViewById(R.id.txt_tire5 );
        mTxtTpmsAlert1 = (TextView)findViewById(R.id.txt_alert1);
        mTxtTpmsAlert2 = (TextView)findViewById(R.id.txt_alert2);
        mTxtTpmsAlert3 = (TextView)findViewById(R.id.txt_alert3);
        mTxtTpmsAlert4 = (TextView)findViewById(R.id.txt_alert4);
        mTxtTpmsAlert5 = (TextView)findViewById(R.id.txt_alert5);
        mTxtTpmsAlert6 = (TextView)findViewById(R.id.txt_alert6);
        mTxtTpmsTire1 .setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        mTxtTpmsTire2 .setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        mTxtTpmsTire3 .setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        mTxtTpmsTire4 .setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        mTxtTpmsTire5 .setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        mTxtTpmsAlert1.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        mTxtTpmsAlert2.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        mTxtTpmsAlert3.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        mTxtTpmsAlert4.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        mTxtTpmsAlert5.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        mTxtTpmsAlert6.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);

        if (HIDE_REFRESH_BUTTONS) {
            mBtnHandShake      .setVisibility(View.GONE);
            mBtnRefreshTire1   .setVisibility(View.GONE);
            mBtnRefreshTire2   .setVisibility(View.GONE);
            mBtnRefreshTire3   .setVisibility(View.GONE);
            mBtnRefreshTire4   .setVisibility(View.GONE);
            mBtnRefreshTire5   .setVisibility(View.GONE);
            mBtnRefreshAlert1  .setVisibility(View.GONE);
            mBtnRefreshAlert2  .setVisibility(View.GONE);
            mBtnRefreshAlert3  .setVisibility(View.GONE);
            mBtnRefreshAlert4  .setVisibility(View.GONE);
            mBtnRefreshAlert5  .setVisibility(View.GONE);
            mBtnRefreshAlert6  .setVisibility(View.GONE);
            mBtnRefreshTireAll .setVisibility(View.GONE);
            mBtnRefreshAlertAll.setVisibility(View.GONE);
        }

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
        mResumeFlag = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        mResumeFlag = false;
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.btn_refresh_status   : mTpmsFuncRet = mTpmsServ.tpmsHandShake   () ; break;
            case R.id.btn_refresh_tire1    : mTpmsFuncRet = mTpmsServ.tpmsRequestTire (1); break;
            case R.id.btn_refresh_tire2    : mTpmsFuncRet = mTpmsServ.tpmsRequestTire (2); break;
            case R.id.btn_refresh_tire3    : mTpmsFuncRet = mTpmsServ.tpmsRequestTire (3); break;
            case R.id.btn_refresh_tire4    : mTpmsFuncRet = mTpmsServ.tpmsRequestTire (4); break;
            case R.id.btn_refresh_tire5    : mTpmsFuncRet = mTpmsServ.tpmsRequestTire (5); break;
            case R.id.btn_match_tire1      : mTpmsFuncRet = mTpmsServ.tpmsMatchTire   (1); break;
            case R.id.btn_match_tire2      : mTpmsFuncRet = mTpmsServ.tpmsMatchTire   (2); break;
            case R.id.btn_match_tire3      : mTpmsFuncRet = mTpmsServ.tpmsMatchTire   (3); break;
            case R.id.btn_match_tire4      : mTpmsFuncRet = mTpmsServ.tpmsMatchTire   (4); break;
            case R.id.btn_match_tire5      : mTpmsFuncRet = mTpmsServ.tpmsMatchTire   (5); break;
            case R.id.btn_unwatch_tire1    : mTpmsFuncRet = mTpmsServ.tpmsUnwatchTire (1); break;
            case R.id.btn_unwatch_tire2    : mTpmsFuncRet = mTpmsServ.tpmsUnwatchTire (2); break;
            case R.id.btn_unwatch_tire3    : mTpmsFuncRet = mTpmsServ.tpmsUnwatchTire (3); break;
            case R.id.btn_unwatch_tire4    : mTpmsFuncRet = mTpmsServ.tpmsUnwatchTire (4); break;
            case R.id.btn_unwatch_tire5    : mTpmsFuncRet = mTpmsServ.tpmsUnwatchTire (5); break;
            case R.id.btn_refresh_alert1   : mTpmsFuncRet = mTpmsServ.tpmsRequestAlert(1); break;
            case R.id.btn_refresh_alert2   : mTpmsFuncRet = mTpmsServ.tpmsRequestAlert(2); break;
            case R.id.btn_refresh_alert3   : mTpmsFuncRet = mTpmsServ.tpmsRequestAlert(3); break;
            case R.id.btn_refresh_alert4   : mTpmsFuncRet = mTpmsServ.tpmsRequestAlert(4); break;
            case R.id.btn_refresh_alert5   : mTpmsFuncRet = mTpmsServ.tpmsRequestAlert(5); break;
            case R.id.btn_refresh_alert6   : mTpmsFuncRet = mTpmsServ.tpmsRequestAlert(6); break;
            case R.id.btn_refresh_tire_all : mTpmsFuncRet = mTpmsServ.tpmsRequestTire (0); break;
            case R.id.btn_match_tire_all   : mTpmsFuncRet = mTpmsServ.tpmsMatchTire   (0); break;
            case R.id.btn_unwatch_tire_all : mTpmsFuncRet = mTpmsServ.tpmsUnwatchTire (0); break;
            case R.id.btn_refresh_alert_all: mTpmsFuncRet = mTpmsServ.tpmsRequestAlert(0); break;
            }
            if (v.getId() == R.id.btn_refresh_all) {
                mTpmsFuncRet = mTpmsServ.tpmsRequestTire(0);
                mTpmsFuncRet = mTpmsServ.tpmsRequestAlert(0);
            }
            updateUI();
        }
    };

    private void updateUI() {
        if (!mResumeFlag) return;
        mTxtTpmsStatus.setText("status: " + (mTpmsFuncRet == 0 ? "connected" : "disconnect"));
        mTxtTpmsTire1 .setText(String.format("tire1: %06X %-4d %-3d %02X", mTpmsTires[0 ], mTpmsTires[1 ], mTpmsTires[2 ], mTpmsTires[3 ]));
        mTxtTpmsTire2 .setText(String.format("tire2: %06X %-4d %-3d %02X", mTpmsTires[4 ], mTpmsTires[5 ], mTpmsTires[6 ], mTpmsTires[7 ]));
        mTxtTpmsTire3 .setText(String.format("tire3: %06X %-4d %-3d %02X", mTpmsTires[8 ], mTpmsTires[9 ], mTpmsTires[10], mTpmsTires[11]));
        mTxtTpmsTire4 .setText(String.format("tire4: %06X %-4d %-3d %02X", mTpmsTires[12], mTpmsTires[13], mTpmsTires[14], mTpmsTires[15]));
        mTxtTpmsTire5 .setText(String.format("tire5: %06X %-4d %-3d %02X", mTpmsTires[16], mTpmsTires[17], mTpmsTires[18], mTpmsTires[19]));
        mTxtTpmsAlert1.setText(String.format("alert1: %-3d  %-3d", mTpmsAlerts[0 ], mTpmsAlerts[1 ]));
        mTxtTpmsAlert2.setText(String.format("alert2: %-3d  %-3d", mTpmsAlerts[2 ], mTpmsAlerts[3 ]));
        mTxtTpmsAlert3.setText(String.format("alert3: %-3d  %-3d", mTpmsAlerts[4 ], mTpmsAlerts[5 ]));
        mTxtTpmsAlert4.setText(String.format("alert4: %-3d  %-3d", mTpmsAlerts[6 ], mTpmsAlerts[7 ]));
        mTxtTpmsAlert5.setText(String.format("alert5: %-3d  %-3d", mTpmsAlerts[8 ], mTpmsAlerts[9 ]));
        mTxtTpmsAlert6.setText(String.format("alert6: %-3d  %-3d", mTpmsAlerts[10], mTpmsAlerts[11]));
    }

    public void sendMessage(int type, int i) {
        Message msg = new Message();
        msg.what = type;
        msg.arg1 = i;
        mHandler.sendMessage(msg);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case tpms.TPMS_TYPE_ALERT:
                mTpmsServ.tpmsGetParams(tpms.TPMS_TYPE_ALERT, mTpmsAlerts);
                break;
            case tpms.TPMS_TYPE_TIRES:
            case tpms.TPMS_TYPE_LEARN:
                mTpmsServ.tpmsGetParams(tpms.TPMS_TYPE_TIRES, mTpmsTires );
                break;
            case tpms.TPMS_TYPE_UNWATCH:
                mTpmsServ.tpmsRequestTire(0);
                break;
            }
            updateUI();
        }
    };
}
