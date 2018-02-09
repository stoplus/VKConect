package com.example.den.vkconect;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;

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

    public static void logoutPref(Context context) {
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

    //	If user is not authorized we finish the main activity
    public static void onLogout(Context context) {
        Account account = new Account();
        account.access_token = null;
        account.user_id = 0;
        account.save(context);
        Intent login = new Intent(context, LoginActivity.class);
        login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(login);
    }
}
