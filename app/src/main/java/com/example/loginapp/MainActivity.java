package com.example.loginapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
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
    private AudioSensors audioSensors;
    private MotionSensors motionSensors;
    private CameraSensor cameraSensor;
    private LightSensor lightSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        motionSensors = new MotionSensors(this);
        cameraSensor = new CameraSensor(this);
        audioSensors = new AudioSensors();
        lightSensor = new LightSensor(this);
        instructions();
        getCameraPermission();
        getSoundPermission();

        main_BTN_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void instructions() {
        String message  = "TO LOGIN:\n1. Turn flash on \n2.Turn brightness " +
                "level to the highest \n3.Add battery percent at the end of password \n" +
                "4.Make a lot of Noise! \n5.Tilt your phone to landscape mode";
        AlertDialog alertDialog =
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(message)
                        .setPositiveButton(getString(android.R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                }).show();
        alertDialog.setCanceledOnTouchOutside(true);
    }

    private void login() {
        checkPassword();
        lightSensor.checkScreenBrightness();
        motionSensors.accelerometerSensor();
        motionSensors.proximitySensor();
        audioSensors.getAmplitude();

        if (cameraSensor.isFlash() && lightSensor.isBrightness() && password &&  motionSensors.isLandscape() && motionSensors.isProximity() && audioSensors.getSoundAmplitude() > 1000) {
            Toast.makeText(MainActivity.this, "LOGIN SUCCESSFUL!", Toast.LENGTH_LONG).show();
            audioSensors.stop();
        } else {
            if(!password){
                Toast.makeText(MainActivity.this, "Wrong password! Don't forget battery percentage!", Toast.LENGTH_SHORT).show();
            }
            if(!cameraSensor.isFlash()){
                Toast.makeText(MainActivity.this, "Turn on the flash!", Toast.LENGTH_SHORT).show();
            }
            if(!lightSensor.isBrightness()){
                Toast.makeText(MainActivity.this, "Turn the brightness level to the highest!", Toast.LENGTH_SHORT).show();
            }
            if(audioSensors.getSoundAmplitude() <= 1000){
                Toast.makeText(MainActivity.this, "Make more noise!", Toast.LENGTH_SHORT).show();
            }
            if(!motionSensors.isLandscape()){
                Toast.makeText(MainActivity.this, "Tilt your phone to landscape mode!", Toast.LENGTH_SHORT).show();
            }
            if(!motionSensors.isProximity()){
                Toast.makeText(MainActivity.this, "Bring your hand closer to the screen!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkPassword() {
        String inputPassword = main_EDT_password.getText().toString();
        Pattern pattern = Pattern.compile(""+PASSWORD + getBatteryPercentage(this));
        Matcher matcher = pattern.matcher(inputPassword + "");
        // check if password contains battery percent
        password = matcher.find();
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

    //-------------------Audio Recorder Permission -----------------------
    private void getSoundPermission() {
        requestPermissionLauncherSound.launch(Manifest.permission.RECORD_AUDIO);
    }

    private ActivityResultLauncher<String> requestPermissionLauncherSound = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    audioSensors.start();
                } else {
                    getSoundPermissionManually();
                }
            });

    private void getSoundPermissionManually() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        AlertDialog alertDialog =
                new AlertDialog.Builder(this)
                        .setMessage("You are directed to the permissions page of the app. Please enable the permission of recording audio so we could check the microphone for sound check. Thank you!")
                        .setPositiveButton(Resources.getSystem().getString(android.R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        manuallyPermissionResultLauncherSound.launch(intent);
                                        dialog.cancel();
                                    }
                                }).show();
        alertDialog.setCanceledOnTouchOutside(true);
    }

    private ActivityResultLauncher<Intent> manuallyPermissionResultLauncherSound = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        audioSensors.start();
                    }
                }
            });

    //----------------------Camera Permission ----------------------
    private void getCameraPermission() {
        requestPermissionLauncherCamera.launch(Manifest.permission.CAMERA);
    }

    private ActivityResultLauncher<String> requestPermissionLauncherCamera = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    cameraSensor.turnOffFlash();
                    cameraSensor.checkFlash();
                } else {
                    getCameraPermissionManually();
                }
            });


    private void getCameraPermissionManually() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        AlertDialog alertDialog =
                new AlertDialog.Builder(this)
                        .setMessage("You are directed to the permissions page of the app. Please enable the permission of the camera so we could access the flash for login check. Thank you!")
                        .setPositiveButton(Resources.getSystem().getString(android.R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        manuallyPermissionResultLauncherCamera.launch(intent);
                                        dialog.cancel();
                                    }
                                }).show();
        alertDialog.setCanceledOnTouchOutside(true);
    }

    private ActivityResultLauncher<Intent> manuallyPermissionResultLauncherCamera = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        cameraSensor.turnOffFlash();
                        cameraSensor.checkFlash();
                    }
                }
            });

    private void findViews() {
        main_BTN_login = findViewById(R.id.main_BTN_login);
        main_EDT_password = findViewById(R.id.main_EDT_password);
    }

}

