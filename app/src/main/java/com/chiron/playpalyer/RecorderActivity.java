package com.chiron.playpalyer;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.chiron.playpalyer.recorder.AACEncoder;
import com.chiron.playpalyer.recorder.AudioPlayBackHandler;
import com.chiron.playpalyer.recorder.AudioRecorderHandler;
import com.chiron.playpalyer.recorder.MediaRecorderHandler;
import com.chiron.playpalyer.recorder.interfaces.EncodedDataCallback;
import com.chiron.playpalyer.recorder.interfaces.SourceDataCallback;
import com.chiron.playpalyer.utils.FileUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class RecorderActivity extends AppCompatActivity implements View.OnClickListener{
    private MediaRecorderHandler mediaRecorderHandler = null;
    private AudioRecorderHandler audioRecorderHandler = null;
    private AACEncoder aacEncoder = null;
    private BufferedOutputStream mAudioBos;
    private File savePath = null;
    private EditText editText = null;
    private static final String TAG = RecorderActivity.class.getSimpleName();
    private static final int REQUEST_CODE = 100;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);
        initUI();
        initRecorder();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initRecorder(){
        savePath = new File(FileUtil.getMainDir(this.getExternalFilesDir(""),"RecordFile"), "record.aac");
        try {
            mAudioBos = new BufferedOutputStream(new FileOutputStream(savePath), 200 * 1024);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mediaRecorderHandler = new MediaRecorderHandler();
        audioRecorderHandler = AudioRecorderHandler.getInstance();
        audioRecorderHandler.init();
        aacEncoder = new AACEncoder();
        aacEncoder.config(audioRecorderHandler.getAudioConfig());
        aacEncoder.setEncodedDataCallback(new EncodedDataCallback() {
            @Override
            public void onAudioEncodedCallback(byte[] data, MediaCodec.BufferInfo bufferInfo) {
                Log.e(TAG,"getAudioEncodedData: "+data.length);
                try {
                    mAudioBos.write(data);
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

    private void initUI(){
        findViewById(R.id.btn_media_recorder_start_record).setOnClickListener(this);
        findViewById(R.id.btn_media_recorder_stop_record).setOnClickListener(this);
        findViewById(R.id.btn_audio_recorder_start_record).setOnClickListener(this);
        findViewById(R.id.btn_audio_recorder_stop_record).setOnClickListener(this);
        findViewById(R.id.btn_audio_recorder_start_play).setOnClickListener(this);
        findViewById(R.id.btn_choose_file).setOnClickListener(this);
        editText = findViewById(R.id.et_file_path);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_media_recorder_start_record:
                mediaRecorderHandler.startRecord(this.getFilesDir().getAbsolutePath());
                break;
            case R.id.btn_media_recorder_stop_record:
                mediaRecorderHandler.stopRecord();
                break;
            case R.id.btn_audio_recorder_start_record:
                startAudioRecord();
                break;
            case R.id.btn_audio_recorder_stop_record:
                stopAudioRecord();
                break;
            case R.id.btn_audio_recorder_start_play:
                startPlay();
                break;
            case R.id.btn_choose_file:
                pickFile();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data!=null) {
            Uri uri = data.getData();
            editText.setText(getPath(this,uri));
//            ContentResolver contentResolver = this.getContentResolver();
//            Cursor cursor = contentResolver.query(uri,new String[]{"_data"},null,null,null);
//            if(cursor==null){
//                String path = uri.getPath();
//                editText.setText(path);
//                return;
//            }
//
//            if (cursor.getColumnCount() != 0) {
//                if(cursor.moveToFirst()){
//                    String path = cursor.getString(cursor.getColumnIndex("_data"));
//                    editText.setText(path);
//                }
//            }
//            cursor.close();
        }
    }

    private void startAudioRecord(){
        Log.e(TAG,"startAudioRecord");
        audioRecorderHandler.start();
        aacEncoder.start();
    }

    private void stopAudioRecord(){
        if(mAudioBos!=null){
            try {
                mAudioBos.flush();
                mAudioBos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        audioRecorderHandler.stop();
        aacEncoder.release();
    }

    private void startPlay(){
        AudioPlayBackHandler playBackHandler = new AudioPlayBackHandler();
        playBackHandler.startPlay(savePath.getAbsolutePath());
    }

    private void pickFile(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        this.startActivityForResult(intent,REQUEST_CODE);
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {

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

                // TODO handle non-primary volumes
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
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
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

}