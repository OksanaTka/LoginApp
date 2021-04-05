package com.example.loginapp;

import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.os.Build;

public class CameraSensor {
    private boolean flash = false;
    private Context context;

    public CameraSensor() { }

    public CameraSensor(Context context) {
        this.context = context;
    }

    public boolean isFlash() {
        return flash;
    }

    public void turnOffFlash(){
        CameraManager camManager = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            camManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        }
        String cameraId = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraId = camManager.getCameraIdList()[0];
                camManager.setTorchMode(cameraId, false);
            }
        } catch (Exception e) {
            //Throws exception if there is no camera permission
            e.printStackTrace();
        }
    }

    public void checkFlash() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CameraManager camManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            CameraManager.TorchCallback torchCallback = new CameraManager.TorchCallback() {
                @Override
                public void onTorchModeUnavailable(String cameraId) {
                    super.onTorchModeUnavailable(cameraId);
                }

                @Override
                public void onTorchModeChanged(String cameraId, boolean enabled) {
                    super.onTorchModeChanged(cameraId, enabled);
                    flash = enabled;
                }
            };
            camManager.registerTorchCallback(torchCallback, null);
        }
    }

}
