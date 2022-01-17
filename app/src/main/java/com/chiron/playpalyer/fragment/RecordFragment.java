package com.chiron.playpalyer.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaCodec;
import android.media.MediaDataSource;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.chiron.playpalyer.R;
import com.chiron.playpalyer.recorder.AACEncoder;
import com.chiron.playpalyer.recorder.AudioPlayBackHandler;
import com.chiron.playpalyer.recorder.AudioRecorderHandler;
import com.chiron.playpalyer.recorder.MediaRecorderHandler;
import com.chiron.playpalyer.recorder.interfaces.EncodedDataCallback;
import com.chiron.playpalyer.recorder.interfaces.SourceDataCallback;
import com.chiron.playpalyer.utils.FileUtil;
import com.chiron.playpalyer.utils.SharedPreferencesUtil;
import com.melnykov.fab.FloatingActionButton;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class RecordFragment extends Fragment {
    private static final String TAG = RecordFragment.class.getSimpleName();
    private static final String ARG = "Fragment Position";
    private int position = -1;
    private FloatingActionButton mRecordButton = null;
    private Chronometer mChronometer = null;
    private TextView mRecordingStatus;

    private AudioRecorderHandler audioRecorderHandler = null;
    private AACEncoder aacEncoder = null;
    private BufferedOutputStream mAudioBos;
    private ByteArrayOutputStream mTempBos;
    private File savePath = null;
    private File cacheFile = null;
    private AudioPlayBackHandler audioPlayBackHandler = new AudioPlayBackHandler();


    private boolean isRecording = false;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd$HH:mm:ss");

    private Object mLock = new Object();

    private long mDuration = -1;
    private long mStartTime = -1;




    private RecordFragment(){}

    public static RecordFragment newInstance(int position){
        RecordFragment recordFragment = new RecordFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG,position);
        recordFragment.setArguments(bundle);
        return recordFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //创建布局
        View recordView = inflater.inflate(R.layout.fragment_record,container,false);
        mChronometer = recordView.findViewById(R.id.chronometer);
        mRecordingStatus = recordView.findViewById(R.id.recording_status_text);
        mRecordButton = recordView.findViewById(R.id.btnRecord);
        mRecordButton.setColorNormal(R.color.primary_light);
        mRecordButton.setColorPressed(R.color.primary);

        mRecordButton.setOnLongClickListener((v)->{
            Log.e(TAG,"onLongClickRecordBtn");
            startRecord();
            return false;//TODO 代表事件还没有消耗完毕，会再往下传递
        });
        /**
         * 停止播放的时候把录音文件保存在缓存中，当试听完毕确定要保存的时候再存到磁盘
         */
        mRecordButton.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()){
                case MotionEvent.ACTION_UP:
                    /**
                     * 先停止录音，音频数据保存在内存中，(录音结束，直接开始试听) 弹出对话框询问是否保存
                     */
                    stopAudioRecord();
                    break;
                default:
                    break;
            }
            return false;
        });
        return recordView;
    }

    private void initRecorder(){
        mTempBos = new ByteArrayOutputStream();
        audioRecorderHandler = AudioRecorderHandler.getInstance();
        audioRecorderHandler.init();
        aacEncoder = new AACEncoder();
        aacEncoder.config(audioRecorderHandler.getAudioConfig());
        aacEncoder.setEncodedDataCallback(new EncodedDataCallback() {
            @Override
            public void onAudioEncodedCallback(byte[] data, MediaCodec.BufferInfo bufferInfo) {
                Log.e(TAG,"getAudioEncodedData: "+data.length);
                try {
//                    mAudioBos.write(data);
                    mTempBos.write(data);//写入到内存中
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onVideoEncodedCallback(byte[] data, MediaCodec.BufferInfo bufferInfo) {

            }
        });

        audioRecorderHandler.setDataCallBack(new SourceDataCallback() {
            @Override
            public void onAudioSourceDataCallback(byte[] data, int index) {
                Log.e(TAG,"getAudioData: "+data.length);
                aacEncoder.putData(data);
            }

            @Override
            public void onVideoSourceDataCallback(byte[] data, int index) {

            }
        });
    }

    /**
     * 开始录音与停止录音的调用逻辑还可以优化
     */
    private void startRecord(){
        if (isRecording) {
            Log.e(TAG, "Already start record");
            return;
        }
        mStartTime = System.currentTimeMillis();

        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();

        isRecording = true;
        initRecorder();
        audioRecorderHandler.start();
        aacEncoder.start();
    }

    private void stopAudioRecord(){
        if(!isRecording){
            Log.e(TAG, "Recorder is not started, can not stop");
            return;
        }
        mDuration = System.currentTimeMillis()-mStartTime;
        mChronometer.stop();
        mChronometer.setBase(SystemClock.elapsedRealtime());

        isRecording = false;
        audioRecorderHandler.stop();
        aacEncoder.release();
        audioRecorderHandler=null;
        aacEncoder=null;

        try {
            mTempBos.flush();//数据刷新到内存中
        } catch (IOException e) {
            e.printStackTrace();
        }

//        new AudioPlayBackHandler().startPlay(getDataSource());
        cacheFile = getDataSourceFile();

        openAuditionDialog(getActivity());
    }

    private void memWriteToFile(){
        try {
            mAudioBos = new BufferedOutputStream(new FileOutputStream(getSavedPath()), 200 * 1024);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (mTempBos != null && mTempBos.size() > 0 && mAudioBos != null) {
            try {
                mTempBos.writeTo(mAudioBos);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    mAudioBos.flush();
                    mAudioBos.close();
                    mTempBos.reset();
                    Log.e(TAG,"WriteToFileFinished");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private File getSavedPath() {
        String fileName = simpleDateFormat.format(new Date(System.currentTimeMillis())) + "_" + String.valueOf(mDuration) + "_Audio.aac";
        savePath = new File(FileUtil.getMainDir(getActivity().getExternalFilesDir(""), "RecordFile"), fileName);
        SharedPreferencesUtil.getInstance(getActivity()).putSP(fileName, savePath.getAbsolutePath());
        return savePath;
    }

    private File getCachePath(){
        String fileName = simpleDateFormat.format(new Date(System.currentTimeMillis()))+"-Audio.aac";
        savePath = new File(FileUtil.getMainDir(getActivity().getExternalCacheDir(),"RecordFile"), fileName);
        return savePath;
    }

    private void openSaveAudioFileDialog(Context context){
        new AlertDialog.Builder(context)
                .setTitle("Save AudioRecord?")
                .setMessage("Click OK to save the recording file locally")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        memWriteToFile();
                        dialog.cancel();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(mTempBos!=null){
                            mTempBos.reset();//重用空间，避免重新创建
                        }
                        dialog.cancel();
                    }
                })
                .show();
    }

    private void openAuditionDialog(Context context){
        startPlay();

        new AlertDialog.Builder(context)
                .setTitle("Playing....")
                .setMessage("Click OK to stop playing")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        audioPlayBackHandler.stopPlay();
                        openSaveAudioFileDialog(context);
                        dialog.cancel();
                        if (cacheFile != null) {
                            cacheFile.delete();
                        }
                    }
                })
                .show();
    }

    private MediaDataSource getDataSource(){
        /**
         * 这里加上试听的逻辑
         * (1)使用MediaDataSource，或者存到文件中再播放
         */
        MediaDataSource dataSource = new MediaDataSource() {//在这里组建DataSource，内存数据中的音频数据拷贝进去
            @Override
            public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
                if (mTempBos != null && mTempBos.size() > 0 && offset + size <= mTempBos.size()) {
                    buffer = mTempBos.toByteArray();
                    System.arraycopy(mTempBos.toByteArray(), offset, buffer, offset, size);
                    return size;
                } else {
                    return -1;
                }
            }

            @Override
            public long getSize() throws IOException {
                if(mTempBos!=null && mTempBos.size()>0){
                    return mTempBos.size();
                }
                return 0;
            }

            @Override
            public void close() throws IOException {

            }
        };
        return dataSource;
    }

    private File getDataSourceFile(){
        if(mTempBos!=null && mTempBos.size()>0){
            String fileName = simpleDateFormat.format(new Date(System.currentTimeMillis()))+"-AudioCache.aac";
            File file = new File(FileUtil.getMainDir(getActivity().getExternalCacheDir(),"RecordFile"), fileName);
            byte[] newData = Arrays.copyOf(mTempBos.toByteArray(),mTempBos.size());
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(newData);
            FileOutputStream fileOutputStream = null;
            byte[] bytes = new byte[1024];
            int len = -1;
            try {
                 fileOutputStream = new FileOutputStream(file);
                while ((len = byteArrayInputStream.read(bytes)) != -1) {
                    fileOutputStream.write(bytes);
                }
                return file;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.flush();
                        fileOutputStream.close();
                        synchronized (mLock) {
                            Log.e(TAG,"执行到这里了");
                            mLock.notifyAll();
                            Log.e(TAG,"执行到这里了1");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    private void startPlay(){
        synchronized (mLock){
            try {
                mLock.wait(3000);
                Log.e(TAG,"拿到文件路径，继续运行: "+cacheFile.getAbsolutePath());
                audioPlayBackHandler.startPlay(cacheFile.getAbsolutePath());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
