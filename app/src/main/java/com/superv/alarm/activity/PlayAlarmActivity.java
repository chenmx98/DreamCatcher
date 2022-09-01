package com.superv.alarm.activity;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.lodz.android.core.utils.FileUtils;
import com.lodz.android.core.utils.NotificationUtils;
import com.lodz.android.core.utils.ToastUtils;
import com.mingle.pulltonextlayout.OnItemSelectListener;
import com.mingle.pulltonextlayout.PullToNextLayout;
import com.mingle.pulltonextlayout.adapter.PullToNextModelAdapter;
import com.mingle.pulltonextlayout.model.PullToNextModel;
import com.superv.alarm.App;
import com.superv.alarm.BuildConfig;
import com.superv.alarm.Model.AlarmModel;
import com.superv.alarm.Utils.ActivityManager;
import com.superv.alarm.Utils.CallBack;
import com.superv.alarm.Utils.TimeUtils;
import com.superv.alarm.Utils.file.FileManager;
import com.superv.alarm.config.Constant;
import com.superv.alarm.data.MyAlarmDataBase;
import com.superv.alarm.R;
import com.superv.alarm.dialog.InputFileNameDialog;
import com.superv.alarm.event.RecorderEvent;
import com.superv.alarm.fragment.AlarmRingFragment;
import com.superv.alarm.fragment.RecordViewFragment;
import com.superv.alarm.recorder.RecordHelper;
import com.superv.alarm.recorder.RecordManager;
import com.superv.alarm.recorder.listener.RecordStateListener;
import com.zlw.main.recorderlib.recorder.RecordConfig;
import com.zlw.main.recorderlib.recorder.listener.RecordFftDataListener;
import com.zlw.main.recorderlib.recorder.listener.RecordResultListener;
import com.zlw.main.recorderlib.recorder.listener.RecordSoundSizeListener;
import com.zlw.main.recorderlib.utils.Logger;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 闹钟活动页面
 */
public class PlayAlarmActivity extends AppCompatActivity {
    public static final String ALARM_ID = "id";
    private static final String TAG = "vijoz";
    private Vibrator vibrator;
    private int mId;
    private String mRing;
    private MediaPlayer player;
    private AudioManager audioManager;

    private PullToNextLayout mPullToNextLayout;
    private List<PullToNextModel> mModelList;

    @Override
    protected void onResume() {
        super.onResume();
        initRecordEvent();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_alarm);
        ActivityManager.addActivity(this);
//        register();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (maxVolume / 4) * 3,
                AudioManager.FLAG_PLAY_SOUND);

        mId = Integer.parseInt(getIntent().getStringExtra(ALARM_ID));
        setmId(mId);

        MyAlarmDataBase db = new MyAlarmDataBase(this);
        AlarmModel am = db.getAlarm(mId);

        initEvent();
        initRecord();

        mRing = am.getRing();
        if (mRing.equals("Vibrate")) {
            startVibrate();

        } else if (mRing.equals("Sound")) {
            SharedPreferences pf = getSharedPreferences("ringCode", MODE_PRIVATE);
            int ringCode = pf.getInt("key_ring", 1);

            startRing(ringCode);

        } else {
            SharedPreferences pf = getSharedPreferences("ringCode", MODE_PRIVATE);
            int ringCode = pf.getInt("key_ring", 1);
            startRing(ringCode);
            startVibrate();
        }

        mPullToNextLayout = findViewById(R.id.pulltonextlayout);

        mModelList = new ArrayList<>();

        mModelList.add(new AlarmRingFragment(this, new CallBack() {
            @Override
            public void run(int type) {
                if (player != null && player.isPlaying()) {
                    player.stop();
                    player.release();
                }
                PlayAlarmActivity.this.finish();
            }
        }));

        mModelList.add(new RecordViewFragment(PlayAlarmActivity.this, new CallBack() {
            @Override
            public void run(int type) {
                if (type == Constant.STATUS_START) {
                    Log.i("vijoz", "STATUS_PAUSE");
                    doPlay();
                } else if (type == Constant.STATUS_PAUSE) {
                    doPlay();
                } else if (type == Constant.STATUS_STOP) {
                    doCancel();
                } else if (type == Constant.STATUS_SAVE) {
                    complete();
                }else if (type == Constant.STATUS_EXIT) {
                    doCancel();
                    PlayAlarmActivity.this.finish();
                }
            }
        }));

        mPullToNextLayout.setAdapter(new PullToNextModelAdapter(this, mModelList));

        mPullToNextLayout.setOnItemSelectListener(new OnItemSelectListener() {
            @Override
            public void onSelectItem(int position, View view) {
                if (position == 1) {//转到第二页停止铃声
                    if (player != null && player.isPlaying()) {
                        player.stop();
                        player.release();
                    }
                    doPlay();//开始录制
                }
            }
        });
    }


    private void startRing(int ringCode) {
        switch (ringCode) {
            case 1:
                if (player != null && player.isPlaying()) {
                    player.stop();
                    player.release();
                    player = MediaPlayer.create(this, R.raw.ring01);
                } else {
                    player = MediaPlayer.create(this, R.raw.ring01);

                }
                break;
            case 2:
                if (player != null && player.isPlaying()) {
                    player.stop();
                    player.release();
                    player = MediaPlayer.create(this, R.raw.ring02);
                } else {
                    player = MediaPlayer.create(this, R.raw.ring02);

                }
                break;
            case 3:
                if (player != null && player.isPlaying()) {
                    player.stop();
                    player.release();
                    player = MediaPlayer.create(this, R.raw.ring03);
                } else {
                    player = MediaPlayer.create(this, R.raw.ring03);

                }
                break;
            case 4:
                if (player != null && player.isPlaying()) {
                    player.stop();
                    player.release();
                    player = MediaPlayer.create(this, R.raw.ring04);
                } else {
                    player = MediaPlayer.create(this, R.raw.ring04);

                }
                break;
            case 5:
                if (player != null && player.isPlaying()) {
                    player.stop();
                    player.release();
                    player = MediaPlayer.create(this, R.raw.ring05);
                } else {
                    player = MediaPlayer.create(this, R.raw.ring05);

                }
                break;
            case 6:
                if (player != null && player.isPlaying()) {
                    player.stop();
                    player.release();
                    player = MediaPlayer.create(this, R.raw.ring06);
                } else {
                    player = MediaPlayer.create(this, R.raw.ring06);

                }
                break;

        }
        player.start();
        player.setLooping(true);
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                player.start();
                player.setLooping(true);
            }
        });
    }

    private void startVibrate() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {1000, 5000, 1000, 5000};
        vibrator.vibrate(pattern, 0);

    }

    public void setmId(int mId) {
        this.mId = mId;
    }

    public int getmId() {
        return mId;
    }

    @Override
    protected void onDestroy() {
//        unRegister();
        if (player != null) {
            player.release();
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
        ActivityManager.removeActivity(this);
        super.onDestroy();
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }


    /**
     * 获取PendingIntent来启动
     *
     * @param context 上下文
     * @param data    数据
     */
    public static PendingIntent startPendingIntent(Context context, String data) {
        Intent intent = new Intent(context, PlayAlarmActivity.class);
        intent.putExtra(Constant.EXTRA_MSG_DATA, data);
        return PendingIntent.getActivity(context, UUID.randomUUID().hashCode(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }


    private void initEvent() {
        //16K采样率
        recordManager.changeRecordConfig(recordManager.getRecordConfig().setSampleRate(16000));
        //16Bit位宽
        recordManager.changeRecordConfig(recordManager.getRecordConfig().setEncodingConfig(AudioFormat.ENCODING_PCM_16BIT));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    /**
     * 处理Intent
     */
    private void handleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        String data = intent.getStringExtra(Constant.EXTRA_MSG_DATA);
        if (TextUtils.isEmpty(data)) {
            return;
        }
        //通知栏 完成录音按钮 事件
        if (data.equals(Constant.NOTIFI_FINISH_MSG)) {
            complete();
            return;
        }
    }

    //完成录音操作
    private void complete() {
        isSave = true;
        doStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecorderEvent(RecorderEvent event) {
        //暂停录音和开始录音 eventbus
        if (event == null) {
            return;
        }

        //录音时间更新
//        Intent intent = new Intent();
//        intent.setAction(Constant.COMMAND);
//        intent.putExtra(Constant.STATUS, Constant.STATUS_TIME + "");
//        intent.putExtra(Constant.STATUS_TIME_STR, TimeUtils.getGapTime(event.timeCounter));
//        sendBroadcast(intent);

        if (!TextUtils.isEmpty(event.type)) {
            doPlay();
            return;
        }

    }

    /**
     * 初始化RecordManager
     **/
    private void initRecord() {
        recordManager.init(App.getInstance(), BuildConfig.DEBUG);
//        recordManager.changeFormat(RecordConfig.RecordFormat.WAV);
        //Mp3格式
        recordManager.changeFormat(RecordConfig.RecordFormat.MP3);
//        recordManager.changeRecordDir(recordDir);
        //存储路径
//        String recordDir = String.format(Locale.getDefault(), "%s/Record/Test/",
//                Environment.getExternalStorageDirectory().getAbsolutePath());
        String recordDir = FileManager.getAudioFolderPath();
        recordManager.changeRecordDir(recordDir);
        initRecordEvent();
    }


    //命名录音文件 Dialog
    private void showInputFileNameDialog(File result) {
        InputFileNameDialog dialog = new InputFileNameDialog(PlayAlarmActivity.this);
        dialog.setListener(new InputFileNameDialog.Listener() {
            @Override
            public void onCancel(Dialog dialog) {
                dialog.dismiss();
            }

            @Override
            public void onConfirm(Dialog dialog, String fileName) {
                //如果录音文件中已存在同名称的文件，展开提示
                if (FileUtils.isFileExists(
                        FileManager.getAudioFolderPath() + fileName + FileUtils.getSuffix(result.getAbsolutePath()))) {

                    showCmDialog(result, fileName);
                } else {
                    //无重名直接保存
                    if (FileUtils.renameFile(result.getAbsolutePath(),
                            fileName + FileUtils.getSuffix(result.getAbsolutePath()))) {
                        ToastUtils.showShort(PlayAlarmActivity.this, "Saving successfully! File saved in：" + FileManager.getAudioFolderPath());
                    } else {
                        ToastUtils.showShort(PlayAlarmActivity.this, "Saving failed");
                    }

                }
                dialog.dismiss();
                isSave = false;
            }
        });
        dialog.show();
    }


    /**
     * 文件重名Dialog
     **/
    public void showCmDialog(File result, String fileName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PlayAlarmActivity.this);
        builder.setTitle("提示：");
        builder.setMessage("录音文件中已存在同名称的文件，是否覆盖？");
        builder.setIcon(R.drawable.ic_launcher);
        builder.setCancelable(true);            //点击对话框以外的区域是否让对话框消失
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog1, int which) {
                if (FileUtils.renameFile(result.getAbsolutePath(),
                        fileName + FileUtils.getSuffix(result.getAbsolutePath()))) {
                    ToastUtils.showShort(PlayAlarmActivity.this, "Saving completed");

                } else {
                    ToastUtils.showShort(PlayAlarmActivity.this, "Saving failed");
                }
                dialog1.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog1, int which) {
                dialog1.dismiss();
                return;
            }
        });
    }

    private void doCancel() {
        isSave =false;
        doStop();
    }

    /**
     * 停止  录音
     **/
    private void doStop() {
        recordManager.stop();

        isPause = false;
        isStart = false;

        NotificationUtils.create(this).getManager().cancel(Constant.NOTIFI_RECORDER_ID);

    }

    /**
     * 开始和暂停  录音
     **/
    private void doPlay() {
        if (isStart) {
            recordManager.pause();

            isPause = true;
            isStart = false;

        } else {
            if (isPause) {
                recordManager.resume();
            } else {
                recordManager.start();
            }
            isStart = true;
        }
//        showCustomNotify(TimeUtils.getGapTime(timeCounter));
    }


    final RecordManager recordManager = RecordManager.getInstance();

    //声音分贝
    private int mSoundSize = 0;
    private boolean isStart = false;
    private boolean isPause = false;
    private boolean isSave = false;

    /**
     * RecordManager回调
     **/
    private void initRecordEvent() {
        recordManager.setRecordStateListener(new RecordStateListener() {
            @Override
            public void onStateChange(RecordHelper.RecordState state) {
                Logger.i(TAG, "onStateChange %s", state.name());
                Intent intent = new Intent();
                switch (state) {
                    case PAUSE:
                        intent.setAction(Constant.COMMAND);
                        intent.putExtra(Constant.STATUS, Constant.STATUS_PAUSE + "");
                        sendBroadcast(intent);
                        break;
                    case IDLE:
                        intent.setAction(Constant.COMMAND);
                        intent.putExtra(Constant.STATUS, Constant.STATUS_EMPT + "");
                        sendBroadcast(intent);
                        break;
                    case RECORDING:
                        intent.setAction(Constant.COMMAND);
                        intent.putExtra(Constant.STATUS, Constant.STATUS_START + "");
                        sendBroadcast(intent);
                        break;
                    case STOP:
                        intent.setAction(Constant.COMMAND);
                        intent.putExtra(Constant.STATUS, Constant.STATUS_STOP + "");
                        sendBroadcast(intent);
                        break;
                    case FINISH:
                        intent.setAction(Constant.COMMAND);
                        intent.putExtra(Constant.STATUS, Constant.STATUS_SAVE + "");
                        sendBroadcast(intent);

                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onError(String error) {
                Logger.i(TAG, "onError %s", error);
            }
        });
        recordManager.setRecordSoundSizeListener(new RecordSoundSizeListener() {
            @Override
            public void onSoundSize(int soundSize) {
                mSoundSize = soundSize;
            }
        });


        recordManager.setRecordResultListener(new RecordResultListener() {
            @Override
            public void onResult(File result) {
                if (isSave) {//保存
                    showInputFileNameDialog(result);
                } else {//不保存，直接删除文件
                    FileUtils.delFile(result.getAbsolutePath());
                }
            }
        });
        recordManager.setRecordFftDataListener(new RecordFftDataListener() {
            @Override
            public void onFftData(byte[] data) {
//                audioView.setWaveData(data);
            }
        });
    }
}
