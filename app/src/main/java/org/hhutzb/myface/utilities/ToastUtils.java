package org.hhutzb.myface.utilities;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;


/**
 * Toast工具类
 */
public class ToastUtils {

    private static Toast toast = null;

    /**
     * Make a long toast
     *
     * @param msg
     * @param context
     */
    public static void makeShortText(String msg, Context context) {
        if (context == null) {
            return;
        }

        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }

    /**
     * Make a short toast
     *
     * @param msg
     * @param context
     */
    public static void makeLongText(String msg, Context context) {
        if (context == null) {
            return;
        }

        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }



    public static void showLong(Context context, String msg){
        makeLongText(msg, context);
    }

    public static void showShort(Context context, String msg){
        makeShortText(msg, context);
    }

    /**
     * Make a long toast by resource id
     *
     * @param context
     * @param id
     */
    public static void showLong(Context context, int id){
        makeLongText(context.getResources().getString(id), context);
    }

    /**
     * Make a short toast by resource id
     *
     * @param context
     * @param id
     */
    public static void showShort(Context context, int id){
        makeShortText(context.getResources().getString(id), context);
    }


    public static void showCenterToast(Context context, String msg) {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}