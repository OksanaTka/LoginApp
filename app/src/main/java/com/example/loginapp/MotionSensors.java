package com.example.loginapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class MotionSensors {
    private boolean isLandscape =  false;
    private boolean proximity =  false;
    private Context context;

    public MotionSensors() {}

    public MotionSensors(Context context) {
        this.context = context;
    }

    public void accelerometerSensor() {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float X_Axis = sensorEvent.values[0];
                float Y_Axis = sensorEvent.values[1];
                if ((X_Axis <= 6 && X_Axis >= -6) && Y_Axis > 5) {
                    isLandscape = false;
                } else if (X_Axis >= 6 || X_Axis <= -6) {
                    isLandscape = true;
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }

        }, sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void proximitySensor() {
        SensorManager mySensorManager;
        Sensor myProximitySensor;

        mySensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        myProximitySensor = mySensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        SensorEventListener proximitySensorEventListener
                = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                    if (event.values[0] == 0) {
                        proximity = true;
                        Log.d("proximitySensor", "Near");
                    } else {
                        Log.d("proximitySensor", "Away");
                    }
                }
            }
        };

        if (myProximitySensor == null) {
            Log.d("proximitySensor", "No Proximity Sensor! ");
        } else {
            mySensorManager.registerListener(proximitySensorEventListener,
                    myProximitySensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public boolean isLandscape() {
        return isLandscape;
    }

    public boolean isProximity() {
        return proximity;
    }
}
