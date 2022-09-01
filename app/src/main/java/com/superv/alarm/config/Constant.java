package com.superv.alarm.config;

public class Constant {
    /** 通知组配置 */

    /** 通知组id */
    public static final String NOTIFI_GROUP_ID = "g0001";

    /** 主频道id */
    public static final String NOTIFI_CHANNEL_MAIN_ID = "c0001";

    /** 常驻消息栏ID */
    public static final int NOTIFI_RECORDER_ID = 12345;

    public static final String EXTRA_MSG_DATA = "extra_msg_data";

    /** 通知栏完成录音 id */
    public static final String NOTIFI_FINISH_MSG = "NOTIFI_FINISH_MSG";


    //通知栏 暂停录音 恢复录音 ID
    /** 通知栏 暂停录音 恢复录音 ID */
    public static final String NOTIFICATION_START_ID="NOTIFICATION_START_ID";


    public static final String STATUS = "status_alarm_v";
    private boolean isStart = false;
    private boolean isPause = false;
    private boolean isSave = false;
    public static final int STATUS_STOP = 0; //停止状态
    public static final int STATUS_START = 1; //开始录制状态
    public static final int STATUS_PAUSE = 2; //暂停状态
    public static final int STATUS_SAVE = 3;  //保存状态
    public static final int STATUS_EMPT = 4;  //空闲状态
    public static final int STATUS_TIME = 5;  //更新时间
    public static final int STATUS_EXIT = 6;  //退出
    public static final String STATUS_TIME_STR = "";

    public static final String COMMAND = "cmd_alarm_v";
    public static final String COMMANDHOME = "cmd_home_alarm_v";


}
