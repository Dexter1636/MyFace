package org.hhutzb.myface.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Base64;

import org.hhutzb.myface.models.EmotionData;


public class DataViewModel extends AndroidViewModel {
    private static final String TAG = "DataViewModel";
    private Context context;

    private EmotionData emotionData = new EmotionData(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    public MutableLiveData<EmotionData> emotionLiveData = new MutableLiveData<>();


    public DataViewModel(@NonNull Application context) {
        super(context);
        this.context = context;
        emotionLiveData.setValue(emotionData);
    }




    public void setImageByteArray(byte[] bitmapBytes) {
        String image_base64 = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
        new Thread() {
            @Override
            public void run() {
                emotionData.getData(image_base64, context);
                emotionLiveData.postValue(emotionData);
            }
        }.start();
    }

}
