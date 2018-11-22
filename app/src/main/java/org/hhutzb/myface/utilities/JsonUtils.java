package org.hhutzb.myface.utilities;

import android.content.Context;
import android.util.Log;

import org.hhutzb.myface.R;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Json请求工具类
 */
public class JsonUtils {

    public static final String TAG = "JsonUtils";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");



    public void __postJson(String json, String url){
//        _postJson(json, url);
    }

    protected void _postJson(String json, String url) {

        final String _json = json;
        final String _url = url;


        //开启一个线程，做联网操作
        new Thread() {
            @Override
            public void run() {
//                String strResponse = __postJson(__json, __url);
            }
        }.start();
    }

    public static String postJson(String image_base64, String url, Context context) {
        //申明给服务端传递一个json串
        //创建一个OkHttpClient对象
        OkHttpClient okHttpClient = MyOkHttpUtils.getInstance().getOkHttpClient();
        //创建一个RequestBody(参数1：数据类型 参数2：传递的json串)
//        RequestBody requestBody = RequestBody.create(JSON, String.valueOf(json));
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("api_key", context.getString(R.string.api_key))
                .addFormDataPart("api_secret", context.getString(R.string.api_secret))
                .addFormDataPart("return_attributes", "emotion")
                .addFormDataPart("image_base64", image_base64)
                .build();
        try {
            //创建一个请求对象
            Request request = new Request.Builder()
//                .header("Content-Type", "application/x-www-form-urlencoded")
                .url(url)
                .post(requestBody)
                .build();
            //发送请求获取响应
            Response response = okHttpClient.newCall(request).execute();
            //判断请求是否成功
            if(response.isSuccessful()) {
                String strResponse = response.body().string();
                //打印服务端返回结果
                Log.i(TAG, strResponse);
                return strResponse;
            }
            else {
                String strResponse = "Network Error";
                //打印错误消息
                Log.i(TAG, strResponse);
                return strResponse;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "Failed to connect to " + url);
        }
        return "Network Error";
    }

}
