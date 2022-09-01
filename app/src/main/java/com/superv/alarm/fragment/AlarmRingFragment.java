package com.superv.alarm.fragment;

import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mingle.pulltonextlayout.PullToNextView;
import com.mingle.pulltonextlayout.model.PullToNextModel;
import com.superv.alarm.Model.AlarmModel;
import com.superv.alarm.R;
import com.superv.alarm.Utils.CallBack;
import com.superv.alarm.activity.PlayAlarmActivity;
import com.superv.alarm.config.Constant;
import com.superv.alarm.data.MyAlarmDataBase;

import java.util.Calendar;

public class AlarmRingFragment extends PullToNextModel {
    private static final long FIVE_MINUTE_TIME = 1000 * 60 * 5;
    private static final int SNOOZE_ALARM_ID = 100;
    private Button btn;
    private TextView alarmTittle,hourText,minuteText;
    private PlayAlarmActivity activity;
    private CallBack callBack;

    public AlarmRingFragment(PlayAlarmActivity context, CallBack back) {
        this.activity =  context;
        this.callBack = back;
    }

    @Override
    public int getLayoutViewId() {
        return R.layout.fragment_play_alarm;
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
        btn = (Button) view.findViewById(R.id.btn_stopAlarm_normal);
        alarmTittle = (TextView) view.findViewById(R.id.textView3);
        hourText = (TextView) view.findViewById(R.id.time_hour_text);
        minuteText = (TextView) view.findViewById(R.id.time_minute_text);

        MyAlarmDataBase db = new MyAlarmDataBase(getView().getContext());
        AlarmModel alarm = db.getAlarm(activity.getmId());

        Log.d("id " , String.valueOf(activity.getmId()));
//        alarmTittle.setText(alarm.getTitle() + "时间到");

        Calendar calendar = Calendar.getInstance();
        String hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        String  minute = null;

        int showHour = calendar.get(Calendar.HOUR_OF_DAY);
        int showMinute = calendar.get(Calendar.MINUTE);

        if (showMinute < 10){
            minute = "0"+String.valueOf(showMinute);
        }else if (showHour < 10){
            hour= "0"+String.valueOf(showHour);
        }else {
            minute = String.valueOf(showMinute);
            hour = String.valueOf(showHour);
        }

        hourText.setText(hour);
        minuteText.setText(minute);
        btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (isEnable) {
                    isEnable = false;
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable, 1000);
                    callBack.run(Constant.STATUS_SAVE);
                }
                return false;
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                activity.finish();
                callBack.run(1);
            }
        });


//        ImageView imageView= (ImageView) v.findViewById(R.id.iv_other);
//        imageView.setImageResource(imgRes[index]);

//        timeView = (TimeView) v.findViewById(R.id.time_demo);
//        timeView.setTime(60);
        view.setClickable(true);
    }


//    public void update(View view) {
//        timeView.reStart(100);
//    }
//
//
//    public void update1(View view) {
//        //回到设置的事件
//        timeView.reStart();
//    }


}
