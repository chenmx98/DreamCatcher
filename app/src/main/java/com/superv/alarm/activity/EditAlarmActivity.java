package com.superv.alarm.activity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.superv.alarm.AlarmService;
import com.superv.alarm.Model.AlarmModel;
import com.superv.alarm.data.MyAlarmDataBase;
import com.superv.alarm.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EditAlarmActivity extends AppCompatActivity implements
        TimePickerDialog.OnTimeSetListener {

    private Toolbar mToolbar;
    private TextView mTimeText, mRepeatText,mRingText;
    private FloatingActionButton mFAB1;
    private FloatingActionButton mFAB2;
    private int  mHour, mMinute;
    private String mTitle="闹钟";
    private String mTime;
    private String mRepeatType,mRepeatCode;
    private String mActive,mRing;
    private List<Integer> repeatCode = new ArrayList<>() ;
    private int mAlarmID;
    private MyAlarmDataBase db;
    private AlarmModel mCheckedAlarm;
    private AlarmService.MyBinder binder;
    private ServiceConnection connection = null;

    private static final String KEY_TITLE = "title_key";
    private static final String KEY_TIME = "time_key";
    private static final String KEY_REPEAT = "repeat_key";
    private static final String KEY_RING = "ring_key";
    private static final String KEY_ACTIVE = "active_key";
    public static final String ALARM_ID = "Alarm_ID";



    private String finalDefine;
    private MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTimeText = (TextView) findViewById(R.id.set_time);
        mRepeatText = (TextView) findViewById(R.id.set_repeat);
        mRingText = (TextView) findViewById(R.id.set_ring);

        mFAB1 = (FloatingActionButton) findViewById(R.id.starred1);
        mFAB2 = (FloatingActionButton) findViewById(R.id.starred2);


        //配置ToolBar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.title_activity_edit_alarm);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        mAlarmID = Integer.parseInt(getIntent().getStringExtra(ALARM_ID));

        db = new MyAlarmDataBase(this);
        mCheckedAlarm = db.getAlarm(mAlarmID);

        Log.d("ed id", String.valueOf(mAlarmID));

        mTitle = mCheckedAlarm.getTitle();
        mRepeatType = mCheckedAlarm.getRepeatType();
        mTime = mCheckedAlarm.getTime();
        mActive = mCheckedAlarm.getActive();
        mRing = mCheckedAlarm.getRing();

        mTimeText.setText(mTime);
        mRepeatText.setText(mRepeatType);
        mRingText.setText(mRing);


        // 得到上次设置状态
        if (savedInstanceState != null) {
            String savedTitle = savedInstanceState.getString(KEY_TITLE);
            mTitle = savedTitle;

            String savedTime = savedInstanceState.getString(KEY_TIME);
            mTimeText.setText(savedTime);
            mTime = savedTime;
            String savedRepeat= savedInstanceState.getString(KEY_REPEAT);
            mRepeatText.setText(savedRepeat);
            mRepeatType = savedRepeat;

            String savedRing = savedInstanceState.getString(KEY_RING);
            mRingText.setText(savedRing);
            mRing = savedRing;

            mActive = savedInstanceState.getString(KEY_ACTIVE);
        }

        if (mActive.equals("false")) {
            mFAB1.setVisibility(View.VISIBLE);
            mFAB2.setVisibility(View.GONE);
        } else if (mActive.equals("true")) {
            mFAB1.setVisibility(View.GONE);
            mFAB2.setVisibility(View.VISIBLE);
        }


    }

    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putCharSequence(KEY_TITLE, "闹钟");
        outState.putCharSequence(KEY_TIME, mTimeText.getText());
        outState.putCharSequence(KEY_REPEAT, mRepeatText.getText());
        outState.putCharSequence(KEY_RING, mRingText.getText());
        outState.putCharSequence(KEY_ACTIVE, mActive);

    }

    public void selectFab1(View v){
        mFAB1 = (FloatingActionButton) findViewById(R.id.starred1);
        mFAB1.setVisibility(View.GONE);
        mFAB2 = (FloatingActionButton) findViewById(R.id.starred2);
        mFAB2.setVisibility(View.VISIBLE);
        mActive = "true";
    }

    public void selectFab2(View v){
        mFAB2 = (FloatingActionButton) findViewById(R.id.starred2);
        mFAB2.setVisibility(View.GONE);
        mFAB1 = (FloatingActionButton) findViewById(R.id.starred1);
        mFAB1.setVisibility(View.VISIBLE);
        mActive = "false";
    }


    public void selectTime(View v){
        Calendar now = Calendar.getInstance();
        TimePickerDialog timeDialog = TimePickerDialog.newInstance(
                this, now.get(Calendar.HOUR_OF_DAY),now.get(Calendar.MINUTE),false);
        timeDialog.setThemeDark(false);
        timeDialog.show(getFragmentManager(), "Select time");
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        mHour = hourOfDay;
        mMinute = minute;
        if (minute < 10) {
            mTime = hourOfDay + ":" + "0" + minute;
        } else {
            mTime = hourOfDay + ":" + minute;
        }
        mTimeText.setText(mTime);
    }

    public void selectRepeat(View v){

        final String[] items = {"Once", "Everyday", "Weekday", "Weekend", "Customize"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_view_day_grey600_24dp);
        builder.setTitle("Repeat");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String repeatType = items[which];
                if (which == 4) {
                    showDefineDialog(dialog);
                } else {
                    mRepeatType = repeatType;
                    mRepeatText.setText(mRepeatType);
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void showDefineDialog(DialogInterface lastDialog) {

        lastDialog.dismiss();

        final String[] myDefine = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        final List<String> choosedDefine = new ArrayList<>();
        finalDefine="";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("自定义");
        builder.setMultiChoiceItems(myDefine, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                if (isChecked) {
                    choosedDefine.add(myDefine[which]);
                    repeatCode.add(which + 2);

                    StringBuilder sb = new StringBuilder();
                    if (repeatCode != null && repeatCode.size() > 0) {
                        for (int i = 0; i < repeatCode.size(); i++) {
                            if (i < repeatCode.size() - 1) {
                                sb.append(repeatCode.get(i) + ",");
                            } else {
                                sb.append(repeatCode.get(i));
                            }
                        }
                    }
                    mRepeatCode = sb.toString();
                }
            }
        });

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int i = 0; i < choosedDefine.size(); i++) {
                    finalDefine = finalDefine + " " + choosedDefine.get(i);
                    if (choosedDefine.size() == 7) {
                        mRepeatType = "Everyday";
                        mRepeatText.setText(mRepeatType);
                    } else {
                        mRepeatType = finalDefine;
                        mRepeatText.setText(mRepeatType);
                    }
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void showRingDialog(DialogInterface lastDialog) {

        lastDialog.dismiss();


        String[] ringList = new String[]{"Morning","卡农","空灵","天籁森林","唯美","温暖早晨"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setSingleChoiceItems(ringList, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = getSharedPreferences("ringCode", MODE_PRIVATE).edit();
                editor.putInt("key_ring", which+1);
                System.out.println("已添加ringCode 是：  "+(which+1));
                editor.apply();
                playRing(which + 1);

            }
        });

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (player != null && player.isPlaying()) {
                    player.stop();
                    player.release();
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (player.isPlaying()) {
                    player.stop();
                    player.release();
                }
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void selectRing(View v){
        final String[] options = new String[]{"Vibrate","Sound","Sound and Vibrate"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select alarm sound");
        builder.setSingleChoiceItems(options, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mRing = options[which];
                mRingText.setText(mRing);
                if (which == 1 || which == 2) {
                    showRingDialog(dialog);
                }
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void playRing(int i){
        switch (i){
            case 1:
                if (player!=null && player.isPlaying()){
                    player.stop();
                    player.release();
                    player = MediaPlayer.create(this, R.raw.ring01);
                }else {
                    player = MediaPlayer.create(this,R.raw.ring01);

                }
                break;
            case 2:
                if (player!=null && player.isPlaying()){
                    player.stop();
                    player.release();
                    player = MediaPlayer.create(this,R.raw.ring02);
                }else {
                    player = MediaPlayer.create(this,R.raw.ring02);

                }
                break;
            case 3:
                if (player!=null && player.isPlaying()){
                    player.stop();
                    player.release();
                    player = MediaPlayer.create(this,R.raw.ring03);
                }else {
                    player = MediaPlayer.create(this,R.raw.ring03);

                }
                break;
            case 4:
                if (player!=null && player.isPlaying()){
                    player.stop();
                    player.release();
                    player = MediaPlayer.create(this,R.raw.ring04);
                }else {
                    player = MediaPlayer.create(this,R.raw.ring04);

                }
                break;
            case 5:
                if (player!=null && player.isPlaying()){
                    player.stop();
                    player.release();
                    player = MediaPlayer.create(this,R.raw.ring05);
                }else {
                    player = MediaPlayer.create(this,R.raw.ring05);

                }
                break;
            case 6:
                if (player!=null && player.isPlaying()){
                    player.stop();
                    player.release();
                    player = MediaPlayer.create(this,R.raw.ring06);
                }else {
                    player = MediaPlayer.create(this,R.raw.ring06);

                }
                break;

        }
        assert player != null;
        player.setLooping(true);
        player.start();

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_alarm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.save_Alarm:
                    updateAlarm();
                return true;

            case R.id.discard_alarm:
                Toast.makeText(getApplicationContext(), "取消编辑",
                        Toast.LENGTH_SHORT).show();

                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateAlarm() {

        mCheckedAlarm.setTitle(mTitle);
        mCheckedAlarm.setTime(mTime);
        mCheckedAlarm.setRepeatType(mRepeatType);
        mCheckedAlarm.setRepeatCode(mRepeatCode);
        mCheckedAlarm.setActive(mActive);
        mCheckedAlarm.setRing(mRing);
        db.updateAlarm(mCheckedAlarm);

        if (mActive.equals("true")){
            connection = new ServiceConnection(){
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    binder = (AlarmService.MyBinder)service;

                    switch (mRepeatType) {
                        case "Once":
                            binder.setSingleAlarm(getApplicationContext(), mTime, mAlarmID);
                            break;
                        case "Everyday":
                            binder.setEverydayAlarm(getApplicationContext(), mTime, mAlarmID);
                            break;
                        default:
                            binder.setDiyAlarm(getApplicationContext(), mRepeatType, mTime, mAlarmID,mRepeatCode);
                            break;
                    }
                }
                @Override
                public void onServiceDisconnected(ComponentName name) {
                }
            };
            Intent intent = new Intent(this, AlarmService.class);
            bindService(intent, connection, BIND_AUTO_CREATE);
        }

        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {

        if (connection!=null) {
            unbindService(connection);
        }
        if (player!=null){
            player.release();
        }
        super.onDestroy();
    }
}
