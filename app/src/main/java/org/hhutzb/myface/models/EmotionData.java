package org.hhutzb.myface.models;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.hhutzb.myface.models.bean.FaceDetectReceived;
import org.hhutzb.myface.models.bean.FaceDetectReceived.FacesBean;
import org.hhutzb.myface.models.bean.FaceDetectReceived.FacesBean.AttributesBean.EmotionBean;
import org.hhutzb.myface.utilities.APIUtils;
import org.hhutzb.myface.utilities.JsonUtils;

import java.util.Objects;


public class EmotionData {
    private static final String TAG = "EmotionData";

    public double anger_value;
    public double disgust_value;
    public double fear_value;
    public double happiness_value;
    public double neutral_value;
    public double sadness_value;
    public double surprise_value;
    public String errorMessage = "";


    public EmotionData(double anger_value, double disgust_value, double fear_value, double happiness_value, double neutral_value, double sadness_value, double surprise_value) {
        this.anger_value = anger_value;
        this.disgust_value = disgust_value;
        this.fear_value = fear_value;
        this.happiness_value = happiness_value;
        this.neutral_value = neutral_value;
        this.sadness_value = sadness_value;
        this.surprise_value = surprise_value;
    }


    private void setValue(FaceDetectReceived bean) {
        FacesBean facesBean = (FacesBean)Objects.requireNonNull(bean.getFaces().toArray())[0];
        EmotionBean emotionBean = facesBean.getAttributes().getEmotion();
        anger_value = emotionBean.getAnger();
        disgust_value = emotionBean.getDisgust();
        fear_value = emotionBean.getFear();
        happiness_value = emotionBean.getHappiness();
        neutral_value = emotionBean.getNeutral();
        sadness_value = emotionBean.getSadness();
        surprise_value = emotionBean.getSurprise();
    }

    private void setVauleToZero() {
        anger_value = 0.0;
        disgust_value = 0.0;
        fear_value = 0.0;
        happiness_value = 0.0;
        neutral_value = 0.0;
        sadness_value = 0.0;
        surprise_value = 0.0;
    }




    public void getData(String image_base64 , Context context) {
        String response = JsonUtils.postJson(image_base64, APIUtils.FACE_DETECT, context);
        Log.i(TAG, "received:" + response);
        try {
            FaceDetectReceived faceDetectReceived = new Gson().fromJson(response, FaceDetectReceived.class);
            if (faceDetectReceived.getFaces().size() == 0) {
                setVauleToZero();
                errorMessage = "未检测到人脸";
            } else {
                errorMessage = "";
                setValue(faceDetectReceived);
            }
        } catch (JsonSyntaxException e) {
            Log.i(TAG, "fromJson ERROR");
        }
    }

}
