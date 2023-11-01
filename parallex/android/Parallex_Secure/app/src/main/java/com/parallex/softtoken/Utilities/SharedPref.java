package com.parallex.softtoken.Utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {
    private static final String PREFERENCEKEY = "PREF_KEY";
    public synchronized static void set(Context context, String key, String value){
        SharedPreferences sp = context.getSharedPreferences(PREFERENCEKEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public synchronized static String get(Context context, String key, String defVal){
        SharedPreferences sp = context.getSharedPreferences(PREFERENCEKEY, Context.MODE_PRIVATE);
        return sp.getString(key, defVal);
    }
}
