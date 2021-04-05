package com.example.loginapp;

import android.content.Context;
import android.os.PowerManager;
import android.provider.Settings;
import java.lang.reflect.Field;

public class LightSensor {
    private boolean brightness = false;
    private Context context;

    public LightSensor() { }

    public LightSensor(Context context) {
        this.context = context;
    }

    public boolean isBrightness() {
        return brightness;
    }

    /**
     * Get the screen brightness
     */
    public void checkScreenBrightness() {
        try {
            int currentBrightness = Settings.System.getInt(
                    context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            int maxBrightness = getMaxBrightness(0);
            if (maxBrightness == currentBrightness) {
                brightness = true;
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the maximum brightness level of the device
     * @param defaultValue
     * @return (int) max level of device
     */
    public int getMaxBrightness(int defaultValue) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
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
}
