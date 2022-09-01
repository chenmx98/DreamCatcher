package com.superv.alarm.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mingle.pulltonextlayout.PullToNextView;
import com.mingle.pulltonextlayout.model.PullToNextModel;
import com.superv.alarm.R;
import com.superv.alarm.Utils.CallBack;
import com.superv.alarm.Utils.TimeUtils;
import com.superv.alarm.activity.PlayAlarmActivity;
import com.superv.alarm.config.Constant;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecordViewFragment extends PullToNextModel {
    private static final String TAG = "vijoz";
    private PlayAlarmActivity activity;
    Button btRecord;
    Button btStop;
    Button btFinish;
    TextView tvState;
    ImageView iv_exit;
    //声音分贝
    private int mSoundSize = 0;
    private boolean isStart = false;
    private boolean isPause = false;
    private boolean isSave = false;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constant.COMMAND)) {
                String status = intent.getStringExtra(Constant.STATUS);
                if (status.equals(Constant.STATUS_STOP + "")) {
                    tvState.setText("Finish");
                } else if (status.equals(Constant.STATUS_START + "")) {
                    tvState.setText("Recording");
                } else if (status.equals(Constant.STATUS_PAUSE + "")) {
                    tvState.setText("Pause");
                } else if (status.equals(Constant.STATUS_SAVE + "")) {
                    tvState.setText("Finish");
                } else if (status.equals(Constant.STATUS_EMPT + "")) {
                    tvState.setText("空闲中");
                }
            }
        }
    };

    private void register() {
        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Constant.COMMAND);
            activity.registerReceiver(receiver, intentFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unRegister() {
        try {
            if (receiver != null) {
                activity.unregisterReceiver(receiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    CallBack callBack;

    public RecordViewFragment(PlayAlarmActivity context, CallBack back) {
        this.activity = context;
        this.callBack = back;
    }

    @Override
    public int getLayoutViewId() {
        return R.layout.fragment_record;
    }

    private boolean isEnable = true;

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            isEnable = true;
        }
    };

    @Override
    public void onBindView(int position, View view, PullToNextView pullToNextView) {
        register();
        btRecord = view.findViewById(R.id.btRecord);
        btStop = view.findViewById(R.id.btStop);
        btFinish = view.findViewById(R.id.btFinish);
        tvState = view.findViewById(R.id.tvState);
        iv_exit = view.findViewById(R.id.iv_exit);

        view.setClickable(true);

        iv_exit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (isEnable) {
                    isEnable = false;
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable, 1000);
                    callBack.run(Constant.STATUS_EXIT);
                }
                return false;
            }
        });

        btRecord.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (isEnable) {
                    isEnable = false;
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable, 1000);
                    if (isStart) {
                        btRecord.setText("Record");
                        isPause = true;
                        isStart = false;
                        callBack.run(Constant.STATUS_PAUSE);
                    } else {
                        btRecord.setText("Pause");
                        isStart = true;
                        callBack.run(Constant.STATUS_START);
                    }
                    Log.i("vijoz", "click了：" + isStart);

                }
                return false;
            }
        });

        btFinish.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (isEnable) {
                    isEnable = false;
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable, 1000);
                    callBack.run(Constant.STATUS_SAVE);
                    btRecord.setText("Record");
                }
                return false;
            }
        });

        btStop.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (isEnable) {
                    isEnable = false;
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable, 1000);
                    callBack.run(Constant.STATUS_STOP);
                    btRecord.setText("Recording");
                }
                return false;
            }
        });

        //进来就开始录制
        btRecord.setText("Pause");
        isStart = true;
    }

    @Override
    public void onPauseView(int position, View view, PullToNextView pullToNextView) {
        super.onPauseView(position, view, pullToNextView);
//        unRegister();
    }
}
