package com.chiron.playpalyer.utils;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {
    private static final String TAG = FileUtil.class.getSimpleName();

    public static File getMainDir(File parentPath, String dir){
        File file = new File(parentPath,dir);
        if(!file.exists()){
            file.mkdir();
        }
        Log.e(TAG,"File Name: "+file.getAbsolutePath());
        return file;
    }

    public static void save(String path, byte[] buffer,int offset,int length,boolean append){
        BufferedOutputStream bufferedOutputStream = null;
        try {
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(path,append));
            bufferedOutputStream.write(buffer,offset,length);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(bufferedOutputStream!=null){
                try {
                    bufferedOutputStream.flush();
                    bufferedOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
