package com.superv.alarm.activity;

import static com.superv.alarm.Utils.Const.DATE_FORMAT_HH_MM_SS;
import static com.superv.alarm.Utils.TimeUtil.HHMMSS_SDF;
import static com.superv.alarm.Utils.TimeUtil.HHMM_SDF;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.superv.alarm.AlarmService;
import com.superv.alarm.Model.AlarmModel;
import com.superv.alarm.Utils.ActivityManager;
import com.superv.alarm.Utils.TimeUtil;
import com.superv.alarm.data.MyAlarmDataBase;
import com.superv.alarm.R;
import com.superv.alarm.view.TimeView;


import java.util.LinkedHashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MyAlarmDataBase db;
    private Toolbar mToolBar;
    private FloatingActionButton mAddAlarmBtn;
    private TextView mNoAlarmTextView,tv_tip;
    private LinkedHashMap<Integer, Integer> IDmap = new LinkedHashMap<>();
    private AlarmService.MyBinder binder;
    private ServiceConnection connection = null;
    private TimeView time_demo;
    private long id;

    /**
     * 启动
     * @param context 上下文
     */
    public static void start(Context context) {
        Intent starter = new Intent(context, MainActivity.class);
        context.startActivity(starter);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityManager.addActivity(this);

        db = new MyAlarmDataBase(getApplicationContext());
        time_demo = findViewById(R.id.time_demo);
        tv_tip = findViewById(R.id.tv_tip);
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        mAddAlarmBtn = (FloatingActionButton) findViewById(R.id.add_reminder);
        mNoAlarmTextView = (TextView) findViewById(R.id.no_alarm_text);
        List<AlarmModel> mAlarmList = db.getAllAlarms();

        if (mAlarmList.isEmpty()) {
            mNoAlarmTextView.setVisibility(View.VISIBLE);
        }

        setSupportActionBar(mToolBar);
        mToolBar.setTitle(R.string.app_name);

        mAddAlarmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(id==-1){
                    Intent intent = new Intent(v.getContext(), AddAlarmActivity.class);
                    startActivityForResult(intent, 1);
                }else{
                    Intent intent = new Intent(v.getContext(), EditAlarmActivity.class);
                    intent.putExtra(EditAlarmActivity.ALARM_ID,id+"");
                    startActivityForResult(intent, 1);
//                    startActivity(intent);
                }


            }
        });
    }


    public void onResume() {
        super.onResume();
        setTimeShow();
    }


    private void setTimeShow() {
        List<AlarmModel> list = db.getAllAlarms();
        if (list.isEmpty()) {
            id = -1;
            time_demo.setVisibility(View.GONE);
            tv_tip.setVisibility(View.GONE);
            mNoAlarmTextView.setVisibility(View.VISIBLE);
        } else {
            id = list.get(list.size() - 1).getID();
            tv_tip.setVisibility(View.VISIBLE);
            AlarmModel alarm = list.get(list.size() - 1);
            long alarmTime = 0;
            String timeNowString = TimeUtil.getCurrentDateTime(DATE_FORMAT_HH_MM_SS);
            long timeNow = TimeUtil.string2Milliseconds(timeNowString, HHMMSS_SDF);
            long time = TimeUtil.string2Milliseconds(alarm.getTime(), HHMM_SDF);
            if (timeNow >= time) {//时间已过，闹钟为明天的闹钟
                alarmTime = (1000 * 60 * 60 * 24) - (timeNow - time);
            } else {//时间为今天的闹钟
                alarmTime = time - timeNow;
            }
//            time_demo.setTime(alarmTime/1000);
            time_demo.reStart(alarmTime / 1000);
            Log.i("vijoz", "now:" + timeNow + "-time:" + time + "--str:" + timeNowString);
            time_demo.setVisibility(View.VISIBLE);
            mNoAlarmTextView.setVisibility(View.GONE);
        }
    }

    public void cancelAlarm(final Context context, final int id, final AlarmModel alarm) {
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binder = (AlarmService.MyBinder) service;
                binder.cancelAlarm(alarm, id, context);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d("MainActivity", "解绑服务");
            }
        };
        Intent intent = new Intent(this, AlarmService.class);

        bindService(intent, connection, BIND_AUTO_CREATE);

        Log.d("MainActivity", "取消闹钟");

        unbindService(connection);

    }


    @SuppressLint("MissingSuperCall")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        setTimeShow();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_rec:
                Intent intent = new Intent(MainActivity.this, RecFilesActivity.class);
                startActivity(intent);
                break;
            case R.id.action_finish:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
