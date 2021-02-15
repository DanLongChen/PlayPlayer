package com.chiron.playpalyer

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.chiron.playpalyer.mediac.decoder.AudioDecoder
import com.chiron.playpalyer.mediac.decoder.VideoDecoder
import com.chiron.playpalyer.permission.Permission
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.Executors
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initPlayer()
    }

    private fun initPlayer(){
        val permission= Permission()
        permission.verifyStoragePermission(this)
        val path=Environment.getExternalStorageDirectory().absolutePath+"/mvtest.mp4"

        val threadPool= Executors.newFixedThreadPool(2)
        val videoDecoder=VideoDecoder(path,sfv,null)
        threadPool.execute(videoDecoder)

        val audioDecoder=AudioDecoder(path)
        threadPool.execute(audioDecoder)

        videoDecoder.resume()
        audioDecoder.resume()
    }
}