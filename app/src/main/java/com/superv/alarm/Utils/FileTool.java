/*
 * Copyright 2015 Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.superv.alarm.Utils;


import static com.superv.alarm.Utils.Const.KB;
import static com.zlw.main.recorderlib.utils.FileUtils.getFileByPath;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

//import android.util.Log;

/**
 * Created in Sep 10, 2016 4:22:18 PM.
 *
 * @author Vondear.
 */
public class FileTool {

    public static final int BUFSIZE = 1024 * 8;
    private static final String TAG = "LavaFileTool";

    /**
     * ??????SD????????????.
     */
    public static File getRootPath() {
        File path = null;
        if (sdCardIsAvailable()) {
            path = Environment.getExternalStorageDirectory(); // ??????sdcard????????????
        } else {
            path = Environment.getDataDirectory();
        }
        return path;
    }

    /**
     * ???????????????????????????????????????/???,??????????????????
     * ?????????????????????????????????
     *
     * @return
     */
    public static File getCecheFolder(Context context) {
        File folder = new File(context.getCacheDir(), "IMAGECACHE");
        if (!folder.exists()) {
            folder.mkdir();
        }
        return folder;
    }

    /**
     * ??????SD???????????????
     *
     * @return true : ??????<br>false : ?????????
     */
    public static boolean isSDCardEnable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * ??????SD?????????
     * <p>?????????/storage/emulated/0/</p>
     *
     * @return SD?????????
     */
    public static String getSDCardPath() {
        if (!isSDCardEnable()) {
            return "sdcard unable!";
        }
        return Environment.getExternalStorageDirectory().getPath() + File.separator;
    }

    /**
     * ??????SD???Data??????
     *
     * @return SD???Data??????
     */
    public static String getDataPath() {
        if (!isSDCardEnable()) {
            return "sdcard unable!";
        }
        return Environment.getDataDirectory().getPath();
    }

    /**
     * SD???????????????.
     */
    public static boolean sdCardIsAvailable() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File sd = new File(Environment.getExternalStorageDirectory().getPath());
            return sd.canWrite();
        } else {
            return false;
        }
    }

    /**
     * ?????????????????????????????????.
     */
    public static boolean fileExists(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    /**
     * ????????????????????????????????????, ??????????????????.
     */
    public static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (file.isFile()) {
            file.delete();
            return true;
        }
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            File exeFile = files[i];
            if (exeFile.isDirectory()) {
                delAllFile(exeFile.getAbsolutePath());
            } else {
                exeFile.delete();
            }
        }
        file.delete();

        return flag;
    }

    /**
     * ??????????????????????????????
     *
     * @param dirPath ????????????
     * @return {@code true}: ????????????<br>{@code false}: ????????????
     */
    public static boolean deleteFilesInDir(String dirPath) {
        return deleteFilesInDir(getFileByPath(dirPath));
    }

    /**
     * ??????????????????????????????
     *
     * @param dir ??????
     * @return {@code true}: ????????????<br>{@code false}: ????????????
     */
    public static boolean deleteFilesInDir(File dir) {
        if (dir == null) {
            return false;
        }
        // ?????????????????????true
        if (!dir.exists()) {
            return true;
        }
        // ??????????????????false
        if (!dir.isDirectory()) {
            return false;
        }
        // ?????????????????????????????????
        File[] files = dir.listFiles();
        if (files != null && files.length != 0) {
            for (File file : files) {
                if (file.isFile()) {
                    if (!deleteFile(file)) {
                        return false;
                    }
                } else if (file.isDirectory()) {
                    if (!deleteDir(file)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    /**
     * ???????????????????????????
     * <p>/data/data/com.xxx.xxx/databases/dbName</p>
     *
     * @param dbName ???????????????
     * @return {@code true}: ????????????<br>{@code false}: ????????????
     */
    public static boolean cleanInternalDbByName(Context context, String dbName) {
        return context.deleteDatabase(dbName);
    }



    /**
     * ????????????.
     */
    public static boolean copy(String srcFile, String destFile) {
        try {
            FileInputStream in = new FileInputStream(srcFile);
            FileOutputStream out = new FileOutputStream(destFile);
            byte[] bytes = new byte[1024];
            int c;
            while ((c = in.read(bytes)) != -1) {
                out.write(bytes, 0, c);
            }
            in.close();
            out.flush();
            out.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * ????????????????????????.
     *
     * @param oldPath string ?????????????????????c:/fqf.
     * @param newPath string ?????????????????????f:/fqf/ff.
     */
    public static void copyFolder(String oldPath, String newPath) {
        try {
            (new File(newPath)).mkdirs(); // ???????????????????????? ?????????????????????
            File a = new File(oldPath);
            String[] file = a.list();
            File temp = null;
            for (int i = 0; i < file.length; i++) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file[i]);
                } else {
                    temp = new File(oldPath + File.separator + file[i]);
                }

                if (temp.isFile()) {
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath + "/" + (temp.getName()).toString());
                    byte[] b = new byte[1024 * 5];
                    int len;
                    while ((len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if (temp.isDirectory()) {// ?????????????????????
                    copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
                }
            }
        } catch (NullPointerException e) {
        } catch (Exception e) {
        }
    }

    /**
     * ???????????????.
     */
    public static boolean renameFile(String resFilePath, String newFilePath) {
        File resFile = new File(resFilePath);
        File newFile = new File(newFilePath);
        return resFile.renameTo(newFile);
    }

    /**
     * ????????????????????????.
     */
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static long getSDCardAvailaleSize() {
        File path = getRootPath();
        StatFs stat = new StatFs(path.getPath());
        long blockSize, availableBlocks;
        if (Build.VERSION.SDK_INT >= 18) {
            blockSize = stat.getBlockSizeLong();
            availableBlocks = stat.getAvailableBlocksLong();
        } else {
            blockSize = stat.getBlockSize();
            availableBlocks = stat.getAvailableBlocks();
        }
        return availableBlocks * blockSize;
    }

    /**
     * ??????????????????????????????.
     */
    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public static long getDirSize(String path) {
        StatFs stat = new StatFs(path);
        long blockSize, availableBlocks;
        if (Build.VERSION.SDK_INT >= 18) {
            blockSize = stat.getBlockSizeLong();
            availableBlocks = stat.getAvailableBlocksLong();
        } else {
            blockSize = stat.getBlockSize();
            availableBlocks = stat.getAvailableBlocks();
        }
        return availableBlocks * blockSize;
    }

    /**
     * ?????????????????????????????????.
     */
    public static long getFileAllSize(String path) {
        File file = new File(path);
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] childrens = file.listFiles();
                long size = 0;
                for (File f : childrens) {
                    size += getFileAllSize(f.getPath());
                }
                return size;
            } else {
                return file.length();
            }
        } else {
            return 0;
        }
    }

    /**
     * ??????????????????.
     */
    public static boolean initFile(String path) {
        boolean result = false;
        try {
            File file = new File(path);
            if (!file.exists()) {
                result = file.createNewFile();
            } else if (file.isDirectory()) {
                file.delete();
                result = file.createNewFile();
            } else if (file.exists()) {
                result = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * ?????????????????????.
     */
    public static boolean initDirectory(String path) {
        boolean result = false;
        File file = new File(path);
        if (!file.exists()) {
            result = file.mkdir();
        } else if (!file.isDirectory()) {
            file.delete();
            result = file.mkdir();
        } else if (file.exists()) {
            result = true;
        }
        return result;
    }

    /**
     * ????????????.
     */
    public static void copyFile(File from, File to) throws IOException {
        if (!from.exists()) {
            throw new IOException("The source file not exist: " + from.getAbsolutePath());
        }
        FileInputStream fis = new FileInputStream(from);
        try {
            copyFile(fis, to);
        } finally {
            fis.close();
        }
    }

    /**
     * ???InputStream???????????????.
     */
    public static long copyFile(InputStream from, File to) throws IOException {
        long totalBytes = 0;
        FileOutputStream fos = new FileOutputStream(to, false);
        try {
            byte[] data = new byte[1024];
            int len;
            while ((len = from.read(data)) > -1) {
                fos.write(data, 0, len);
                totalBytes += len;
            }
            fos.flush();
        } finally {
            fos.close();
        }
        return totalBytes;
    }

    /**
     * ??????InputStream????????????.
     */
    public static void saveFile(InputStream inputStream, String filePath) {
        try {
            OutputStream outputStream = new FileOutputStream(new File(filePath), false);
            int len;
            byte[] buffer = new byte[1024];
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ???UTF8??????????????????.
     */
    public static void saveFileUTF8(String path, String content, Boolean append) throws IOException {
        FileOutputStream fos = new FileOutputStream(path, append);
        Writer out = new OutputStreamWriter(fos, "UTF-8");
        out.write(content);
        out.flush();
        out.close();
        fos.flush();
        fos.close();
    }

    /**
     * ???UTF8??????????????????.
     */
    public static String getFileUTF8(String path) {
        String result = "";
        InputStream fin = null;
        try {
            fin = new FileInputStream(path);
            int length = fin.available();
            byte[] buffer = new byte[length];
            fin.read(buffer);
            fin.close();
            result = new String(buffer, "UTF-8");
        } catch (Exception e) {
        }
        return result;
    }

    /**
     * ??????????????????Intent.
     */
    public static Intent getFileIntent(String path, String mimeType) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(path)), mimeType);
        return intent;
    }

    /**
     * ??????????????????
     *
     * @param context
     * @return
     */
    public static String getDiskCacheDir(Context context) {
        String cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }

    /**
     * ??????????????????????????????
     *
     * @param context
     * @return
     */
    public static String getDiskFileDir(Context context) {
        String cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath();
        } else {
            cachePath = context.getFilesDir().getPath();
        }
        return cachePath;
    }

    /**
     * ??????????????????
     *
     * @param outFile
     * @param files
     */
    public static void mergeFiles(Context context, File outFile, List<File> files) {
        FileChannel outChannel = null;
        try {
            outChannel = new FileOutputStream(outFile).getChannel();
            for (File f : files) {
                FileChannel fc = new FileInputStream(f).getChannel();
                ByteBuffer bb = ByteBuffer.allocate(BUFSIZE);
                while (fc.read(bb) != -1) {
                    bb.flip();
                    outChannel.write(bb);
                    bb.clear();
                }
                fc.close();
            }
            Log.d(TAG, "????????????");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (outChannel != null) {
                    outChannel.close();
                }
            } catch (IOException ignore) {
            }
        }
    }

    /**
     * ????????????m3u8??????????????????m3u8
     *
     * @param context  ??????
     * @param file     ?????????m3u8
     * @param pathList ?????????ts??????
     * @return
     */
    public static String getNativeM3u(final Context context, File file, List<File> pathList) {
        InputStream in = null;
        int num = 0;
        //?????????????????????buff
        StringBuffer buf = new StringBuffer();
        try {
            if (file != null) {
                in = new FileInputStream(file);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                if (line.length() > 0 && line.startsWith("http://")) {
                    //replce ???????????????
//                    Log.d("ts??????", line + "  replce  " + pathList.get(num).getAbsolutePath());
                    buf.append("file:" + pathList.get(num).getAbsolutePath() + "\r\n");
                    num++;
                } else {
                    buf.append(line + "\r\n");
                }
            }
            in.close();
            write(file.getAbsolutePath(), buf.toString());
            Log.d("ts??????", "ts????????????");
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return buf.toString();
    }

    /**
     * ???????????? ????????? ??????
     *
     * @param filePath
     * @param content
     */
    public static void write(String filePath, String content) {
        BufferedWriter bw = null;
        try {
            //???????????????????????????????????????
            bw = new BufferedWriter(new FileWriter(filePath));
            // ????????????????????????
            bw.write(content);
//            Log.d("M3U8??????", "????????????");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // ?????????
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    bw = null;
                }
            }
        }
    }

    /**
     * ?????? ??????????????? ?????? ?????? ?????? ?????????
     *
     * @param fileAbsolutePath ???????????????
     * @param suffix           ????????????
     * @return
     */
    public static Vector<String> GetAllFileName(String fileAbsolutePath, String suffix) {
        Vector<String> vecFile = new Vector<String>();
        File file = new File(fileAbsolutePath);
        File[] subFile = file.listFiles();
        for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
            // ????????????????????????
            if (!subFile[iFileLength].isDirectory()) {
                String filename = subFile[iFileLength].getName();
                // ???????????????suffix??????
                if (filename.trim().toLowerCase().endsWith(suffix)) {
                    vecFile.add(filename);
                }
            }
        }
        return vecFile;
    }


    //----------------------------------------------------------------------------------------------


    /**
     * ????????????????????????
     *
     * @param filePath ????????????
     * @return {@code true}: ??????<br>{@code false}: ?????????
     */
    public static boolean isFileExists(String filePath) {
        return isFileExists(getFileByPath(filePath));
    }

    /**
     * ????????????????????????
     *
     * @param file ??????
     * @return {@code true}: ??????<br>{@code false}: ?????????
     */
    public static boolean isFileExists(File file) {
        return file != null && file.exists();
    }

    /**
     * ?????????????????????
     *
     * @param dirPath ????????????
     * @return {@code true}: ???<br>{@code false}: ???
     */
    public static boolean isDir(String dirPath) {
        return isDir(getFileByPath(dirPath));
    }

    /**
     * ?????????????????????
     *
     * @param file ??????
     * @return {@code true}: ???<br>{@code false}: ???
     */
    public static boolean isDir(File file) {
        return isFileExists(file) && file.isDirectory();
    }

    /**
     * ?????????????????????
     *
     * @param filePath ????????????
     * @return {@code true}: ???<br>{@code false}: ???
     */
    public static boolean isFile(String filePath) {
        return isFile(getFileByPath(filePath));
    }

    /**
     * ?????????????????????
     *
     * @param file ??????
     * @return {@code true}: ???<br>{@code false}: ???
     */
    public static boolean isFile(File file) {
        return isFileExists(file) && file.isFile();
    }

    /**
     * ???????????????????????????????????????????????????????????????
     *
     * @param dirPath ????????????
     * @return {@code true}: ?????????????????????<br>{@code false}: ????????????????????????
     */
    public static boolean createOrExistsDir(String dirPath) {
        return createOrExistsDir(getFileByPath(dirPath));
    }

    /**
     * ???????????????????????????????????????????????????????????????
     *
     * @param file ??????
     * @return {@code true}: ?????????????????????<br>{@code false}: ????????????????????????
     */
    public static boolean createOrExistsDir(File file) {
        // ?????????????????????????????????true?????????????????????false???????????????????????????????????????
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    /**
     * ???????????????????????????????????????????????????????????????
     *
     * @param filePath ????????????
     * @return {@code true}: ?????????????????????<br>{@code false}: ????????????????????????
     */
    public static boolean createOrExistsFile(String filePath) {
        return createOrExistsFile(getFileByPath(filePath));
    }

    /**
     * ???????????????????????????????????????????????????????????????
     *
     * @param file ??????
     * @return {@code true}: ?????????????????????<br>{@code false}: ????????????????????????
     */
    public static boolean createOrExistsFile(File file) {
        if (file == null) {
            return false;
        }
        // ?????????????????????????????????true?????????????????????false
        if (file.exists()) {
            return file.isFile();
        }
        if (!createOrExistsDir(file.getParentFile())) {
            return false;
        }
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ?????????????????????????????????????????????????????????
     *
     * @param filePath ????????????
     * @return {@code true}: ????????????<br>{@code false}: ????????????
     */
    public static boolean createFileByDeleteOldFile(String filePath) {
        return createFileByDeleteOldFile(getFileByPath(filePath));
    }

    /**
     * ?????????????????????????????????????????????????????????
     *
     * @param file ??????
     * @return {@code true}: ????????????<br>{@code false}: ????????????
     */
    public static boolean createFileByDeleteOldFile(File file) {
        if (file == null) {
            return false;
        }
        // ????????????????????????????????????false
        if (file.exists() && file.isFile() && !file.delete()) {
            return false;
        }
        // ????????????????????????false
        if (!createOrExistsDir(file.getParentFile())) {
            return false;
        }
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }



    /**
     * ????????????
     *
     * @param dirPath ????????????
     * @return {@code true}: ????????????<br>{@code false}: ????????????
     */
    public static boolean deleteDir(String dirPath) {
        return deleteDir(getFileByPath(dirPath));
    }

    /**
     * ????????????
     *
     * @param dir ??????
     * @return {@code true}: ????????????<br>{@code false}: ????????????
     */
    public static boolean deleteDir(File dir) {
        if (dir == null) {
            return false;
        }
        // ?????????????????????true
        if (!dir.exists()) {
            return true;
        }
        // ??????????????????false
        if (!dir.isDirectory()) {
            return false;
        }
        // ?????????????????????????????????
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                if (!deleteFile(file)) {
                    return false;
                }
            } else if (file.isDirectory()) {
                if (!deleteDir(file)) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    /**
     * ????????????
     *
     * @param srcFilePath ????????????
     * @return {@code true}: ????????????<br>{@code false}: ????????????
     */
    public static boolean deleteFile(String srcFilePath) {
        return deleteFile(getFileByPath(srcFilePath));
    }

    /**
     * ????????????
     *
     * @param file ??????
     * @return {@code true}: ????????????<br>{@code false}: ????????????
     */
    public static boolean deleteFile(File file) {
        return file != null && (!file.exists() || file.isFile() && file.delete());
    }

    /**
     * ???????????????????????????
     *
     * @param dirPath     ????????????
     * @param isRecursive ????????????????????????
     * @return ????????????
     */
    public static List<File> listFilesInDir(String dirPath, boolean isRecursive) {
        return listFilesInDir(getFileByPath(dirPath), isRecursive);
    }

    /**
     * ???????????????????????????
     *
     * @param dir         ??????
     * @param isRecursive ????????????????????????
     * @return ????????????
     */
    public static List<File> listFilesInDir(File dir, boolean isRecursive) {
        if (isRecursive) {
            return listFilesInDir(dir);
        }
        if (dir == null || !isDir(dir)) {
            return null;
        }
        List<File> list = new ArrayList<>();
        Collections.addAll(list, dir.listFiles());
        return list;
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param dirPath ????????????
     * @return ????????????
     */
    public static List<File> listFilesInDir(String dirPath) {
        return listFilesInDir(getFileByPath(dirPath));
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param dir ??????
     * @return ????????????
     */
    public static List<File> listFilesInDir(File dir) {
        if (dir == null || !isDir(dir)) {
            return null;
        }
        List<File> list = new ArrayList<>();
        File[] files = dir.listFiles();
        for (File file : files) {
            list.add(file);
            if (file.isDirectory()) {
                list.addAll(listFilesInDir(file));
            }
        }
        return list;
    }

    /**
     * ?????????????????????????????????suffix?????????
     * <p>???????????????</p>
     *
     * @param dirPath     ????????????
     * @param suffix      ?????????
     * @param isRecursive ????????????????????????
     * @return ????????????
     */
    public static List<File> listFilesInDirWithFilter(String dirPath, String suffix, boolean isRecursive) {
        return listFilesInDirWithFilter(getFileByPath(dirPath), suffix, isRecursive);
    }

    /**
     * ?????????????????????????????????suffix?????????
     * <p>???????????????</p>
     *
     * @param dir         ??????
     * @param suffix      ?????????
     * @param isRecursive ????????????????????????
     * @return ????????????
     */
    public static List<File> listFilesInDirWithFilter(File dir, String suffix, boolean isRecursive) {
        if (isRecursive) {
            return listFilesInDirWithFilter(dir, suffix);
        }
        if (dir == null || !isDir(dir)) {
            return null;
        }
        List<File> list = new ArrayList<>();
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.getName().toUpperCase().endsWith(suffix.toUpperCase())) {
                list.add(file);
            }
        }
        return list;
    }

    /**
     * ?????????????????????????????????suffix????????????????????????
     * <p>???????????????</p>
     *
     * @param dirPath ????????????
     * @param suffix  ?????????
     * @return ????????????
     */
    public static List<File> listFilesInDirWithFilter(String dirPath, String suffix) {
        return listFilesInDirWithFilter(getFileByPath(dirPath), suffix);
    }

    /**
     * ?????????????????????????????????suffix????????????????????????
     * <p>???????????????</p>
     *
     * @param dir    ??????
     * @param suffix ?????????
     * @return ????????????
     */
    public static List<File> listFilesInDirWithFilter(File dir, String suffix) {
        if (dir == null || !isDir(dir)) {
            return null;
        }
        List<File> list = new ArrayList<>();
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.getName().toUpperCase().endsWith(suffix.toUpperCase())) {
                list.add(file);
            }
            if (file.isDirectory()) {
                list.addAll(listFilesInDirWithFilter(file, suffix));
            }
        }
        return list;
    }

    /**
     * ???????????????????????????filter?????????
     *
     * @param dirPath     ????????????
     * @param filter      ?????????
     * @param isRecursive ????????????????????????
     * @return ????????????
     */
    public static List<File> listFilesInDirWithFilter(String dirPath, FilenameFilter filter, boolean isRecursive) {
        return listFilesInDirWithFilter(getFileByPath(dirPath), filter, isRecursive);
    }

    /**
     * ???????????????????????????filter?????????
     *
     * @param dir         ??????
     * @param filter      ?????????
     * @param isRecursive ????????????????????????
     * @return ????????????
     */
    public static List<File> listFilesInDirWithFilter(File dir, FilenameFilter filter, boolean isRecursive) {
        if (isRecursive) {
            return listFilesInDirWithFilter(dir, filter);
        }
        if (dir == null || !isDir(dir)) {
            return null;
        }
        List<File> list = new ArrayList<>();
        File[] files = dir.listFiles();
        for (File file : files) {
            if (filter.accept(file.getParentFile(), file.getName())) {
                list.add(file);
            }
        }
        return list;
    }

    /**
     * ???????????????????????????filter????????????????????????
     *
     * @param dirPath ????????????
     * @param filter  ?????????
     * @return ????????????
     */
    public static List<File> listFilesInDirWithFilter(String dirPath, FilenameFilter filter) {
        return listFilesInDirWithFilter(getFileByPath(dirPath), filter);
    }

    /**
     * ???????????????????????????filter????????????????????????
     *
     * @param dir    ??????
     * @param filter ?????????
     * @return ????????????
     */
    public static List<File> listFilesInDirWithFilter(File dir, FilenameFilter filter) {
        if (dir == null || !isDir(dir)) {
            return null;
        }
        List<File> list = new ArrayList<>();
        File[] files = dir.listFiles();
        for (File file : files) {
            if (filter.accept(file.getParentFile(), file.getName())) {
                list.add(file);
            }
            if (file.isDirectory()) {
                list.addAll(listFilesInDirWithFilter(file, filter));
            }
        }
        return list;
    }

    /**
     * ??????????????????????????????????????????????????????
     * <p>???????????????</p>
     *
     * @param dirPath  ????????????
     * @param fileName ?????????
     * @return ????????????
     */
    public static List<File> searchFileInDir(String dirPath, String fileName) {
        return searchFileInDir(getFileByPath(dirPath), fileName);
    }

    /**
     * ??????????????????????????????????????????????????????
     * <p>???????????????</p>
     *
     * @param dir      ??????
     * @param fileName ?????????
     * @return ????????????
     */
    public static List<File> searchFileInDir(File dir, String fileName) {
        if (dir == null || !isDir(dir)) {
            return null;
        }
        List<File> list = new ArrayList<>();
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.getName().toUpperCase().equals(fileName.toUpperCase())) {
                list.add(file);
            }
            if (file.isDirectory()) {
                list.addAll(listFilesInDirWithFilter(file, fileName));
            }
        }
        return list;
    }


    /**
     * ????????????????????????
     *
     * @param filePath ????????????
     * @param content  ????????????
     * @param append   ????????????????????????
     * @return {@code true}: ????????????<br>{@code false}: ????????????
     */
    public static boolean writeFileFromString(String filePath, String content, boolean append) {
        return writeFileFromString(getFileByPath(filePath), content, append);
    }

    /**
     * ????????????????????????
     *
     * @param file    ??????
     * @param content ????????????
     * @param append  ????????????????????????
     * @return {@code true}: ????????????<br>{@code false}: ????????????
     */
    public static boolean writeFileFromString(File file, String content, boolean append) {
        if (file == null || content == null) return false;
        if (!createOrExistsFile(file)) return false;
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file, append);
            fileWriter.write(content);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeIO(fileWriter);
        }
    }

    public static String getFileCharsetSimple(File file) {
        int p = 0;
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(file));
            p = (is.read() << 8) + is.read();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeIO(is);
        }
        switch (p) {
            case 0xefbb:
                return "UTF-8";
            case 0xfffe:
                return "Unicode";
            case 0xfeff:
                return "UTF-16BE";
            default:
                return "GBK";
        }
    }

    /**
     * ??????????????????
     *
     * @param filePath ????????????
     * @return ????????????
     */
    public static int getFileLines(String filePath) {
        return getFileLines(getFileByPath(filePath));
    }

    /**
     * ??????????????????
     *
     * @param file ??????
     * @return ????????????
     */
    public static int getFileLines(File file) {
        int count = 1;
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[KB];
            int readChars;
            while ((readChars = is.read(buffer, 0, KB)) != -1) {
                for (int i = 0; i < readChars; ++i) {
                    if (buffer[i] == '\n') {
                        ++count;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeIO(is);
        }
        return count;
    }



    /**
     * ???????????????MD5?????????
     *
     * @param filePath ??????
     * @return ?????????MD5?????????
     */
//    public static String getFileMD5(String filePath) {
//        return getFileMD5(getFileByPath(filePath));
//    }

    /**
     * ???????????????MD5?????????
     *
     * @param file ??????
     * @return ?????????MD5?????????
     */
//    public static String getFileMD5(File file) {
//        return RxEncryptTool.encryptMD5File2String(file);
//    }

    /**
     * ??????IO
     *
     * @param closeables closeable
     */
    public static void closeIO(Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        try {
            for (Closeable closeable : closeables) {
                if (closeable != null) {
                    closeable.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ???Uri?????????File
     *
     * @param context
     * @param uri
     * @return
     */
//    public static File getFilePhotoFromUri(Activity context, Uri uri) {
//        return new File(RxPhotoTool.getImageAbsolutePath(context, uri));
//    }

    @TargetApi(19)
    public static String getPathFromUri(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return "";
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.Images.Media.DATA;
        String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * ????????????IO
     *
     * @param closeables closeable
     */
    public static void closeIOQuietly(Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static String file2Base64(String filePath) {
        FileInputStream fis = null;
        String base64String = "";
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            fis = new FileInputStream(filePath);
            byte[] buffer = new byte[1024 * 100];
            int count = 0;
            while ((count = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, count);
            }
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        base64String = Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT);
        return base64String;

    }

    /**
     * ??????????????????????????????, ????????????????????????????????????
     *
     * @param strFilePath
     * @param strBuffer
     */
    public void TextToFile(final String strFilePath, final String strBuffer) {
        FileWriter fileWriter = null;
        try {
            // ??????????????????
            File fileText = new File(strFilePath);
            // ?????????????????????????????????
            fileWriter = new FileWriter(fileText);
            // ?????????
            fileWriter.write(strBuffer);
            // ??????
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * ?????????????????????????????????????????????????????????????????????
     */
    public void readFileByLines(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        try {
            System.out.println("?????????????????????????????????????????????????????????");
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 1;
            // ?????????????????????????????????null???????????????
            while ((tempString = reader.readLine()) != null) {
                // ????????????
                System.out.println("line?????????????????????????????????? " + line + ": " + tempString);
                String content = tempString;
                line++;
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
    }
}


//----------------------------------------m3u8 ts ??????----------------------------------------------
//    /**
//     * ??????ts??????
//     *
//     * @param url
//     * @param title
//     * @param i
//     * @param dataList
//     * @param fileList
//     */
//    public static void getFile(final Context context, String url, final String title, final int i, final List<TrackData> dataList, final List<File> fileList, final String duration, final File m3u8File) {
//
//        OkHttpUtils//
//                .get()//
//                .url(url)//
//                .build()//
//                .execute(new FileCallBack(FileUtil.getDiskFileDir(context) + File.separator + title, title + i + ".ts") {
//                    @Override
//                    public void onError(Call call, Exception e, int id) {
////                        Log.d("??????", e.getMessage());
//                    }
//
//                    @Override
//                    public void onResponse(File response, int id) {
////                        Log.d("ts??????", response.getAbsolutePath());
//                        fileList.add(response);
//                        if (dataList.size() == fileList.size()) {
//                            //FileUtil.mergeFiles(context, new File(FileUtil.getDiskFileDir(context) + File.separator + title, title + "_" + duration + ".mp4"), fileList);
////                            Log.d("ts??????", "????????????");
///*                            LavaBaseTools.ShowToast(context, "???????????????????????????????????????????????????...", false);*/
//                            getNativeM3u(context, m3u8File, fileList);
//                        } else {
//                            getFile(context, dataList.get(i + 1).getUri(), title, i + 1, dataList, fileList, duration, m3u8File);
//                        }
//                    }
//
//                    @Override
//                    public void inProgress(float progress, long total, int id) {
//                        super.inProgress(progress, total, id);
//                    }
//                });
//    }
//
//    /**
//     * ????????????
//     *
//     * @param url
//     */
//    public static void getFile(String url, final String filePath, String name) {
//
//        OkHttpUtils//
//                .get()//
//                .url(url)//
//                .build()//
//                .execute(new FileCallBack(filePath, name) {
//                    @Override
//                    public void onError(Call call, Exception e, int id) {
//
//                        try {
////                            Log.d("??????", e.getMessage());
//                        } catch (Exception e1) {
//                            e1.printStackTrace();
//                        }
//                    }
//
//                    @Override
//                    public void onResponse(File response, int id) {
//                        try {
////                            Log.d("??????", response.getAbsolutePath());
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    @Override
//                    public void inProgress(float progress, long total, int id) {
//                        super.inProgress(progress, total, id);
//                    }
//                });
//    }
//
//    /**
//     * ??????m3u8??????
//     *
//     * @param context
//     * @param videoParse1
//     * @param vid
//     */
//    public static void getM3U8(final Context context, final VideoParse1 videoParse1, final String vid, final String duration) {
//           /* final DownloadRequest downloadRequest = NoHttp.createDownloadRequest(videoParse1.getUrl().get(0).getU(), FileUtil.getDiskFileDir(getApplicationContext()), videoParse1.getTitle() + "m3u8", false, true);
//            downloadQueue.add(0, downloadRequest, downloadListener);*/
//            /*downloadRequest.onPreResponse();*/
//        OkHttpUtils//
//                .get()//
//                .url(videoParse1.getUrl().get(0).getU())//
//                .build()//
//                .execute(new FileCallBack(FileUtil.getDiskFileDir(context), videoParse1.getTitle() + "%" + vid + "%" + duration + "%m3u8") {
//                    @Override
//                    public void onError(Call call, Exception e, int id) {
//                        LavaBaseTools.ShowToast(context, "??????????????????????????????...", 500);
//                    }
//
//                    @Override
//                    public void onBefore(Request request, int id) {
//                        super.onBefore(request, id);
//                        LavaBaseTools.ShowToast(context, "????????????...", 500);
//                    }
//
//                    @Override
//                    public void onResponse(File response, int id) {
//                        try {
//                            InputStream inputStream = new FileInputStream(response);
//                            PlaylistParser parser = new PlaylistParser(inputStream, Format.EXT_M3U, Encoding.UTF_8);
//                            Playlist playlist = parser.parse();
////                            Log.d("????????????", playlist.toString());
//                            List<String> downList = new ArrayList<String>();
//                            List<TrackData> dataList = playlist.getMediaPlaylist().getTracks();
//                            for (int j = 0; j < dataList.size(); j++) {
////                              float duration = dataList.get(j).getTrackInfo().duration;//??????
//                                downList.add(dataList.get(0).getUri());
//                            }
//                            LavaBaseTools.ShowToast(context, "?????????????????????????????????????????????...", 500);
//                            FileUtil.getFile(context, dataList.get(0).getUri(), vid, 0, dataList, new ArrayList<File>(), duration, response);
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
////                            Log.d("????????????", "FileNotFoundException");
//                        } catch (IOException e) {
//                            e.printStackTrace();
////                            Log.d("????????????", "IOException");
//                        } catch (ParseException e) {
//                            e.printStackTrace();
////                            Log.d("????????????", "ParseException");
//                        } catch (PlaylistException e) {
//                            e.printStackTrace();
////                            Log.d("????????????", "PlaylistException");
//                        }
//                    }
//
//                    @Override
//                    public void inProgress(float progress, long total, int id) {
//                        super.inProgress(progress, total, id);
//                    }
//                });
//    }
//========================================m3u8 ts ??????==================================================

