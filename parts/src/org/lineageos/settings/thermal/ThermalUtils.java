/*
 * Copyright (C) 2020 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.thermal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.UserHandle;

import androidx.preference.PreferenceManager;

import org.lineageos.settings.utils.FileUtils;

public final class ThermalUtils {

    private static final String THERMAL_CONTROL = "thermal_control";

    protected static final int STATE_DEFAULT = 0;
    protected static final int STATE_BENCHMARK = 1;
    protected static final int STATE_CAMERA = 3;
    protected static final int STATE_VIDEO_PLAYBACK = 4;
    protected static final int STATE_GAMING = 5;
    protected static final int STATE_STREAMING = 6;

    private static final String THERMAL_STATE_DEFAULT = "0";
    private static final String THERMAL_STATE_BENCHMARK = "6";
    private static final String THERMAL_STATE_VIDEO = "11";
    private static final String THERMAL_STATE_CAMERA = "15";
    private static final String THERMAL_STATE_GAMING = "19";
    private static final String THERMAL_STATE_STREAMING = "7";

    private static final String THERMAL_BENCHMARK = "thermal.benchmark=";
    private static final String THERMAL_VIDEO = "thermal.video=";
    private static final String THERMAL_CAMERA = "thermal.camera=";
    private static final String THERMAL_GAMING = "thermal.gaming=";
    private static final String THERMAL_STREAMING = "thermal.streaming=";

    private static final String THERMAL_SCONFIG = "/sys/class/thermal/thermal_message/sconfig";

    private SharedPreferences mSharedPrefs;

    protected ThermalUtils(Context context) {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void startService(Context context) {
        context.startServiceAsUser(new Intent(context, ThermalService.class),
                UserHandle.CURRENT);
    }

    private void writeValue(String profiles) {
        mSharedPrefs.edit().putString(THERMAL_CONTROL, profiles).apply();
    }

    private String getValue() {
        String value = mSharedPrefs.getString(THERMAL_CONTROL, null);

        if (value == null || value.isEmpty()) {
            value = THERMAL_BENCHMARK + ":" + THERMAL_CAMERA + ":" +
                    THERMAL_GAMING + ":" + THERMAL_STREAMING + ":" + THERMAL_VIDEO;
            writeValue(value);
        }
        return value;
    }

    protected void writePackage(String packageName, int mode) {
        String value = getValue();
        value = value.replace(packageName + ",", "");
        String[] modes = value.split(":");
        String finalString;

        switch (mode) {
            case STATE_BENCHMARK:
                modes[0] = modes[0] + packageName + ",";
                break;
            case STATE_CAMERA:
                modes[1] = modes[1] + packageName + ",";
                break;
            case STATE_GAMING:
                modes[2] = modes[2] + packageName + ",";
                break;
            case STATE_STREAMING:
                modes[3] = modes[3] + packageName + ",";
                break;
            case STATE_VIDEO_PLAYBACK:
                modes[4] = modes[4] + packageName + ",";
                break;
        }

        finalString = modes[0] + ":" + modes[1] + ":" + modes[2] + ":" + modes[3] + ":" + modes[4];

        writeValue(finalString);
    }

    protected int getStateForPackage(String packageName) {
        String value = getValue();
        String[] modes = value.split(":");
        int state = STATE_DEFAULT;
        if (modes[0].contains(packageName + ",")) {
            state = STATE_BENCHMARK;
        } else if (modes[1].contains(packageName + ",")) {
            state = STATE_CAMERA;
        } else if (modes[2].contains(packageName + ",")) {
            state = STATE_GAMING;
        } else if (modes[3].contains(packageName + ",")) {
            state = STATE_STREAMING;
        } else if (modes[4].contains(packageName + ",")) {
            state = STATE_VIDEO_PLAYBACK;
        }

        return state;
    }

    protected void setDefaultThermalProfile() {
        FileUtils.writeLine(THERMAL_SCONFIG, THERMAL_STATE_DEFAULT);
    }

    protected void setThermalProfile(String packageName) {
        String value = getValue();
        String modes[];
        String state = THERMAL_STATE_DEFAULT;

        if (value != null) {
            modes = value.split(":");

            if (modes[0].contains(packageName + ",")) {
                state = THERMAL_STATE_BENCHMARK;
            } else if (modes[1].contains(packageName + ",")) {
                state = THERMAL_STATE_CAMERA;
            } else if (modes[2].contains(packageName + ",")) {
                state = THERMAL_STATE_GAMING;
            } else if (modes[3].contains(packageName + ",")) {
                state = THERMAL_STATE_STREAMING;
            } else if (modes[4].contains(packageName + ",")) {
                state = THERMAL_STATE_VIDEO;
            }
        }
        FileUtils.writeLine(THERMAL_SCONFIG, state);
    }
}
