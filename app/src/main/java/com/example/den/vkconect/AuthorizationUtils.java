package com.example.den.vkconect;


import android.content.Context;

public class AuthorizationUtils {
    private static final String PREFERENCES_AUTHORIZED_KEY = "isAuthorized";
    private static final String LOGIN_PREFERENCES = "LoginData";

    /**
     * This method makes the user authorized
     *
     * @param context current context
     */

    public static void setAuthorized(Context context) {
        context.getSharedPreferences(LOGIN_PREFERENCES, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(PREFERENCES_AUTHORIZED_KEY, true)
                .apply();
    }

    /**
     * This method makes the user unauthorized
     *
     * @param context current context
     */

    public static void logout(Context context) {
        context.getSharedPreferences(LOGIN_PREFERENCES, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(PREFERENCES_AUTHORIZED_KEY, false)
                .apply();
    }


     // This method checks if the user is authorized
    public static boolean isAuthorized(Context context) {
        return context.getSharedPreferences(LOGIN_PREFERENCES, Context.MODE_PRIVATE)
                .getBoolean(PREFERENCES_AUTHORIZED_KEY, false);
    }
}
