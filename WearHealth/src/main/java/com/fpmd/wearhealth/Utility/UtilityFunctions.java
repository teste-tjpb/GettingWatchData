package com.fpmd.wearhealth.Utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;

import static com.fpmd.wearhealth.Constants.Constant.IS_DEBUGGER_ON;


/**
 * Created by vikasaggarwal on 24/03/18.
 */

public class UtilityFunctions {
    private  Context ctx;
    private String Tag = "UtilityTag";



    private SharedPreferences sharedPreferences;



    public UtilityFunctions(Context ctx) {
        this.ctx = ctx;
        Tag = ctx.getClass().getSimpleName();

    }

    public void showToast(String msg){
        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
    }

    public void showLogDebug(String msg){
        if(IS_DEBUGGER_ON)
            Log.d(Tag,msg);
    }
    public void showLogError(String msg){
        if(IS_DEBUGGER_ON)
        Log.e(Tag,msg);
    }
    public static void showLogErrorStatic(String msg){
         String Tag = "UtilityTag";
        if(IS_DEBUGGER_ON)
            Log.e(Tag,msg);
    }

    public String getCurrentTime(){


        Calendar var0 = Calendar.getInstance();
        int var1 = var0.get(Calendar.YEAR);
        int var2 = var0.get(Calendar.MONTH) + 1;
        int var3 = var0.get(Calendar.DATE);
        int var4 = var0.get(Calendar.HOUR);
        int var5 = var0.get(Calendar.MINUTE);
        int var6 = var0.get(Calendar.SECOND);

               return var1+"-"+var2+"-"+var3+"-"+var4+"-"+var5+"-"+var6;
    }

    /** Returns the consumer friendly device name */
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        try {
            return URLEncoder.encode(capitalize(manufacturer) + " " + model,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "Some Error while encoding scheme";
        }
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase.append(c);
        }

        return phrase.toString();
    }
}
