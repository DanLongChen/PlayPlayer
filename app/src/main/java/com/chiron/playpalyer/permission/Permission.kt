package com.chiron.playpalyer.permission

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat


class Permission {
    private val  REQUEST_EXTERNAL_STORAGE = 1
    private var PERMISSIONS_STORAGE:Array<String>?= arrayOf( "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WRITE_EXTERNAL_STORAGE")
    public fun verifyStoragePermission(activity:Activity){
        try {
            //检测是否有写的权限
            val writePermission = ActivityCompat.checkSelfPermission(
                activity,
                "android.permission.WRITE_EXTERNAL_STORAGE"
            )
            val readPermission = ActivityCompat.checkSelfPermission(
                activity,
                "android.permission.READ_EXTERNAL_STORAGE"
            )
            if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                PERMISSIONS_STORAGE?.let {
                    ActivityCompat.requestPermissions(
                        activity,
                        it,
                        REQUEST_EXTERNAL_STORAGE
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}