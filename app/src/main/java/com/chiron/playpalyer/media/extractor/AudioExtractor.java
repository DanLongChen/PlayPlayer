package com.chiron.playpalyer.media.extractor;

import android.media.MediaFormat;

import com.chiron.playpalyer.media.IExtractor;
import com.chiron.playpalyer.media.MediaFormatCallback;

import java.nio.ByteBuffer;

public class AudioExtractor implements IExtractor {
    private MyMediaExtractor myMediaExtractor;
    public AudioExtractor(String path){
        myMediaExtractor = new MyMediaExtractor(path,MediaType.AUDIO);
    }

    @Override
    public MediaFormat getFormat() {
        return myMediaExtractor.getFormat();
    }

    @Override
    public void getFormat(MediaFormatCallback callback) {
        myMediaExtractor.getFormat(callback);
    }

    @Override
    public int readBuffer(ByteBuffer byteBuffer) {
        myMediaExtractor.setTrack(myMediaExtractor.getmAudioTrack());
        return myMediaExtractor.readBuffer(byteBuffer);
    }

    @Override
    public void setTrack(int track) {
        myMediaExtractor.setTrack(track);
    }

    @Override
    public long seek(long pos) {
        return myMediaExtractor.seek(pos);
    }

    @Override
    public void stop() {
        myMediaExtractor.stop();
    }

    @Override
    public long getCurrentTimeStamp() {
        return myMediaExtractor.getCurrentTimeStamp();
    }

    public int getSampleFlag(){
        return myMediaExtractor.getmCurSampleFlag();
    }
}
