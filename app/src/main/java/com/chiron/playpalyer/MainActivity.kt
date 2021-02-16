package com.chiron.playpalyer

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.chiron.playpalyer.mediac.decoder.AudioDecoder
import com.chiron.playpalyer.mediac.decoder.VideoDecoder
import com.chiron.playpalyer.mediac.muxer.MP4Repack
import com.chiron.playpalyer.permission.Permission
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.Executors
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {
    val path=Environment.getExternalStorageDirectory().absolutePath+"/mvtest.mp4"
    lateinit var videoDecoder: VideoDecoder
    lateinit var audioDecoder: AudioDecoder
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val permission= Permission()
        permission.verifyStoragePermission(this)
        initPlayer()
    }

    private fun initPlayer(){
        val threadPool= Executors.newFixedThreadPool(2)
        videoDecoder=VideoDecoder(path,sfv,null)
        threadPool.execute(videoDecoder)

        audioDecoder=AudioDecoder(path)
        threadPool.execute(audioDecoder)

        videoDecoder.resume()
        audioDecoder.resume()
    }

    fun clickRepack(view:View){
        repack()
    }

    private fun repack(){
        //视频输入路径
        val path=Environment.getExternalStorageDirectory().absolutePath+"/mvtest.mp4"
        val repack=MP4Repack(path)
        repack.start()
    }

    override fun onDestroy() {
        videoDecoder.stop()
        audioDecoder.stop()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode){
            1->{
                if(grantResults.size>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"权限申请通过",Toast.LENGTH_LONG)
                }else{
                    Toast.makeText(this,"权限申请不通过",Toast.LENGTH_LONG)
                    finish()
                }
            }
            else->{
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }

    }


}