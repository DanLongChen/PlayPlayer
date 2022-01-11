package com.chiron.playpalyer.recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class AudioRecorderHandler {
    private final static int[] SAMPLE_RATES = {44100,22050,16000,11025};
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
    private int audioSource = MediaRecorder.AudioSource.MIC;
    private AudioRecord mAudioRecorder = null;
    private void initAudioDevice(){
        for (int sampleRate:SAMPLE_RATES){

        }
    }
}
