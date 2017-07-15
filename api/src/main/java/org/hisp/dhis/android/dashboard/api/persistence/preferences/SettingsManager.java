package org.hisp.dhis.android.dashboard.api.persistence.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {
    public static final String CHART_WIDTH = "key:chart_width";
    public static final String CHART_HEIGHT = "key:chart_height";
    public static final String NETWORK_TIMEOUT = "key:network_timeout";
    public static final String DISK_TIMEOUT = "key:disk_timeout";
    public static final String IMAGE_TIMEOUT = "key:image_timeout";
    public static final String MINIMUM_WIDTH = "480";
    public static final String MINIMUM_HEIGHT = "320";
    public static final String MINIMUM_NETWORK_TIMEOUT = "0";
    public static final String MINIMUM_DISK_TIMEOUT = "0";
    public static final String MINIMUM_IMAGE_TIMEOUT = "0";
    private static final String PREFERENCES = "preferences:settings";
    public static final String MAXIMUM_WIDTH = "1920";
    public static final String MAXIMUM_HEIGHT = "1080";
    public static final String MAXIMUM_NETWORK_TIMEOUT = "60";
    public static final String MAXIMUM_DISK_TIMEOUT = "10";
    public static final String MAXIMUM_IMAGE_TIMEOUT = "5184000";
    private static SettingsManager mSettingsManager = null;
    private SharedPreferences mPrefs;

    public SettingsManager(Context context) {
        mPrefs = context.getSharedPreferences(SettingsManager.PREFERENCES,
                Context.MODE_PRIVATE);
    }

    public static SettingsManager getInstance(Context context) {
        if (mSettingsManager == null) {
            mSettingsManager = new SettingsManager(context);
        }
        return mSettingsManager;
    }

    public void setPreference(String key, String value) {
        mPrefs.edit().putString(key, value).commit();
    }

    public String getPreference(String key, String defaultValue) {
        return mPrefs.getString(key, defaultValue);
    }


}
