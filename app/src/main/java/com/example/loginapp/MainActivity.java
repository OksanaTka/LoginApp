package com.example.loginapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.button.MaterialButton;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {

    private static final String PASSWORD = "123456";

    private MaterialButton main_BTN_login;
    private EditText main_EDT_password;
    private boolean password = false;
    private boolean flash = false;
    private boolean brightness = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        getPermission();
        turnOffFlash();
        checkFlash();

        main_BTN_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getPassword();
                checkScreenBrightness();


                if (flash && brightness && password) {
                    Log.d("SUCCESS1", " SUCCESS");
                } else {
                    Log.d("SUCCESS1", " FAIL  --->" + " password: " + password + " || " + " flash: " + flash + " || " + " brightness: " + brightness);
                }
            }

        });


    }


    private void getPassword() {
        String inputPassword = main_EDT_password.getText().toString();

        int percent = getBatteryPercentage(this);

        Pattern pattern = Pattern.compile(""+PASSWORD + percent);
        Matcher matcher = pattern.matcher(inputPassword + "");

        boolean find = matcher.find();

        // check if password contains battery percent
        if (find) {
            password = true;
        }
    }


    public static int getBatteryPercentage(Context context) {
        if (Build.VERSION.SDK_INT >= 21) {
            BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
            return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } else {
            IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, iFilter);
            int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
            int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;
            double batteryPct = level / (double) scale;
            return (int) (batteryPct * 100);
        }
    }


    private void checkScreenBrightness() {
        try {
            int currentBrightness = Settings.System.getInt(
                    getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);

            int maxBrightness = getMaxBrightness(0);

            if (maxBrightness == currentBrightness) {
                brightness = true;
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    public int getMaxBrightness(int defaultValue) {

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            Field[] fields = powerManager.getClass().getDeclaredFields();
            for (Field field : fields) {

                if (field.getName().equals("BRIGHTNESS_ON")) {
                    field.setAccessible(true);
                    try {
                        return (int) field.get(powerManager);
                    } catch (IllegalAccessException e) {
                        return defaultValue;
                    }
                }
            }
        }
        return defaultValue;
    }

    private void turnOffFlash(){
        CameraManager camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        String cameraId = null; // Usually front camera is at 0 position and back camera is 1.
        try {
            cameraId = camManager.getCameraIdList()[0];
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                camManager.setTorchMode(cameraId, false);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }



    private void checkFlash() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CameraManager camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

            CameraManager.TorchCallback torchCallback = new CameraManager.TorchCallback() {
                @Override
                public void onTorchModeUnavailable(String cameraId) {
                    super.onTorchModeUnavailable(cameraId);
                    Log.d("TAG", "onTorchModeUnavailable ");
                }

                @Override
                public void onTorchModeChanged(String cameraId, boolean enabled) {
                    super.onTorchModeChanged(cameraId, enabled);
                    flash = enabled;
                    Log.d("TAG", "onTorchModeChanged  " + enabled);

                }

            };
            camManager.registerTorchCallback(torchCallback, null);

        }
    }


    private void findViews() {
        main_BTN_login = findViewById(R.id.main_BTN_login);
        main_EDT_password = findViewById(R.id.main_EDT_password);
    }


    private void getPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    //  mCamera = Camera.open();
                } else {
                    getPermissionManually();
                }
            });


    private void getPermissionManually() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        manuallyPermissionResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> manuallyPermissionResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // mCamera = Camera.open();
                    }
                }
            });
}