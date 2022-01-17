package com.chiron.playpalyer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.chiron.playpalyer.utils.PermissionUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int REQUEST_CODE = 1;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PermissionUtil permissionUtil = new PermissionUtil();
        permissionUtil.verifyPermission(this);
        initUi();
    }

    private void initUi(){
        findViewById(R.id.btn_media).setOnClickListener(this);
        findViewById(R.id.btn_recorder).setOnClickListener(this);
        findViewById(R.id.btn_new_recorder).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_media: {
                Intent intent = new Intent(MainActivity.this, MediaActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.btn_recorder: {
                Intent intent = new Intent(MainActivity.this, RecorderActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.btn_new_recorder:{
                Intent intent = new Intent(MainActivity.this, NewRecorderActivity.class);
                startActivity(intent);
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_CODE:
                if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"权限申请通过",Toast.LENGTH_SHORT);
                }else{
                    Toast.makeText(this,"权限申请失败",Toast.LENGTH_SHORT);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }
    }
}
