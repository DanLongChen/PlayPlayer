package com.chiron.playpalyer.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

public class PermissionUtil {
    private static final int REQUEST_CODE = 1;
    private static final String[] PERMISSIONS= {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.RECORD_AUDIO"};

    public void verifyPermission(Activity activity){
        for (String permission:PERMISSIONS){
            if(ActivityCompat.checkSelfPermission(activity,permission)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(activity,PERMISSIONS,REQUEST_CODE);
            }
        }
    }
}
