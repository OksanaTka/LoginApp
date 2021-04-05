package com.example.loginapp;

import android.media.MediaRecorder;

import java.io.IOException;

public class AudioSensors {
    private double soundAmplitude =0;
    private MediaRecorder mRecorder = null;

    public AudioSensors() {

    }

    public double getSoundAmplitude() {
        return soundAmplitude;
    }

    public void start() {
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");
            try {
                mRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mRecorder.start();
        }
    }

    public void stop() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    public void getAmplitude() {
        if (mRecorder != null)
            soundAmplitude =  mRecorder.getMaxAmplitude();
        else
            soundAmplitude = 0;
    }
}
