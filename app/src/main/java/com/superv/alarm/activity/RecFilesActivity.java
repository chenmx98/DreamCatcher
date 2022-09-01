package com.superv.alarm.activity;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.superv.alarm.R;
import com.superv.alarm.Utils.ActivityManager;
import com.superv.alarm.Utils.FileTool;
import com.superv.alarm.Utils.file.FileManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class RecFilesActivity extends AppCompatActivity {
    private Toolbar mToolBar;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rec_files);
        ActivityManager.addActivity(this);
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        listView = findViewById(R.id.alarm_lists);
        load();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_finish:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        recordings.playerStop();
        recordings.close();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    public final static String TAG = "vijoz";

    Recordings recordings;

    void load() {
        recordings = new Recordings(this);
        recordings.scan();
        listView.setAdapter(recordings);
    }

    public class Recordings extends ArrayAdapter<File> {
        MediaPlayer player;
        Runnable updatePlayer;
        int selected = -1;

        Map<File, Integer> durations = new TreeMap<>();

        public Recordings(Context context) {
            super(context, 0);
        }

        public void scan() {
            setNotifyOnChange(false);
            clear();
            durations.clear();
            String tempFilePath = FileManager.getAudioFoldersPath();
            File file = new File(tempFilePath);
            List<File> ff = FileTool.listFilesInDir(file, false);
            Log.i("vijoz", "filesize:" + ff.size() + "path:" + tempFilePath);
            for (File f : ff) {
                if (f.isFile()) {
                    MediaPlayer mp = MediaPlayer.create(getContext(), Uri.fromFile(f));
                    if (mp != null) {
                        int d = mp.getDuration();
                        mp.release();
                        durations.put(f, d);
                        add(f);
                    } else {
                        Log.e(TAG, f.toString());
                    }
                }
            }
            //排序
            sort(new FileComparator());
            notifyDataSetChanged();
        }

        public void close() {
            if (player != null) {
                player.release();
                player = null;
            }
            if (updatePlayer != null) {
                updatePlayer = null;
            }
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.recording, parent, false);
                convertView.setTag(-1);
            }

            final File f = getItem(position);

            TextView title = convertView.findViewById(R.id.recording_title);
            title.setText(f.getName());

            SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            TextView time = convertView.findViewById(R.id.recording_time);
            time.setText(s.format(new Date(f.lastModified())));

            final View playerBase = convertView.findViewById(R.id.recording_player);

            if (selected == position) {
                updatePlayerText(convertView, f);

                final View play = convertView.findViewById(R.id.recording_player);
                play.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (player == null) {
                            playerPlay(playerBase, f);
                        } else if (player.isPlaying()) {
                            playerPause(playerBase, f);
                        } else {
                            playerPlay(playerBase, f);
                        }
                    }
                });

                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        select(-1);
                    }
                });
                playerPlay(playerBase, f);
            } else {
                ImageView i = playerBase.findViewById(R.id.iv_play);
                i.setImageResource(R.mipmap.play);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        select(position);
                    }
                });
            }

            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return true;
                }
            });

            return convertView;
        }

        void playerPlay(View v, File f) {
            if (player == null)
                player = MediaPlayer.create(getContext(), Uri.fromFile(f));
            player.setLooping(true);
            if (player == null) {
                Toast.makeText(v.getContext(), "文件不存在", Toast.LENGTH_SHORT).show();
                return;
            }
            player.start();

            updatePlayerRun(v, f);
        }

        void playerPause(View v, File f) {
            if (player != null) {
                player.pause();
            }
            if (updatePlayer != null) {
                updatePlayer = null;
            }
            updatePlayerText(v, f);
        }


        void playerStop() {
            if (updatePlayer != null) {
                updatePlayer = null;
            }
            if (player != null) {
                player.stop();
                player.release();
                player = null;
            }
        }

        void updatePlayerRun(final View v, final File f) {
            boolean playing = updatePlayerText(v, f);

            if (updatePlayer != null) {
                updatePlayer = null;
            }

            if (!playing) {
                return;
            }

            updatePlayerText(v, f);
        }

        boolean updatePlayerText(final View v, final File f) {
            ImageView i = v.findViewById(R.id.iv_play);
            final boolean playing = player != null && player.isPlaying();
            i.setImageResource(playing ? R.mipmap.pause : R.mipmap.play);
            return playing;
        }

        public void select(int pos) {
            selected = pos;
            notifyDataSetChanged();
            playerStop();
        }
    }


    static class SortFiles implements Comparator<File> {
        @Override
        public int compare(File file, File file2) {
            if (file.isDirectory() && file2.isFile())
                return -1;
            else if (file.isFile() && file2.isDirectory())
                return 1;
            else
                return file2.getPath().compareTo(file.getPath());
        }
    }

    /**
     * 将文件按名字降序排列
     */
    class FileComparator1 implements Comparator<File> {

        @Override
        public int compare(File file1, File file2) {
            return file2.getName().compareTo(file1.getName());
        }
    }


    class FileComparator3 implements Comparator<File> {

        @Override
        public int compare(File file1, File file2) {
            if (file1.length() < file2.length()) {
                return -1;// 小文件在前
            } else {
                return 1;
            }
        }
    }

    /**
     * 将文件按时间降序排列
     */
    class FileComparator implements Comparator<File> {

        @Override
        public int compare(File file1, File file2) {
            if (file1.lastModified() < file2.lastModified()) {
                return 1;// 最后修改的文件在前
            } else {
                return -1;
            }
        }
    }
}
