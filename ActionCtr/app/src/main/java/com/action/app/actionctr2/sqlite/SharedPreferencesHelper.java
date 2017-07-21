package com.action.app.actionctr2.sqlite;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2017/6/25/025.
 */

public class SharedPreferencesHelper {
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    Context context;

    public SharedPreferencesHelper(Context c, String name) {
        context = c;
        sp = context.getSharedPreferences(name, 0);
        editor = sp.edit();
    }

    public void putString(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public String getString(String key) {
        return sp.getString(key, null);
    }

    public void putFloat(String key, Float value) {
        editor.putFloat(key, value);
        editor.commit();
    }

    public float getFloat(String key) {
        return sp.getFloat(key, 0);

    }
}
