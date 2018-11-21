package org.hhutzb.myface.ui;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import org.hhutzb.myface.R;
import org.hhutzb.myface.databinding.ActivityDataBinding;
import org.hhutzb.myface.utilities.ToastUtils;
import org.hhutzb.myface.utilities.WindowUtils;
import org.hhutzb.myface.viewmodels.DataViewModel;

import java.nio.ByteBuffer;
import java.util.Arrays;


public class DataActivity extends AppCompatActivity {
    private static final String TAG = "DataActivity";
    private static final int MY_CAMERA_REQUEST_CODE = 100;

    // 为了使照片竖直显示
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 270);
        ORIENTATIONS.append(Surface.ROTATION_90, 180);
        ORIENTATIONS.append(Surface.ROTATION_180, 90);
        ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    private CameraManager manager; // 摄像头管理器
    private Handler childHandler, mainHandler;
    private CameraDevice mCamera;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mSession;
    private ImageReader mImageReader;
    private CaptureRequest.Builder captureRequestBuilder; // 创建拍照需要的CaptureRequest.Builder

    private DataViewModel viewModel;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private ImageView iv_show;
    private String mCameraID = "1"; // 摄像头id 0为后 1为前


    /**
     * 摄像头创建监听
     */
    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            // 打开摄像头
            try {
                //开启预览
                mCamera = camera;
                startPreview(camera);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            //关闭摄像头
            if (mCamera != null) {
                mCamera.close();
                mCamera = null;
            }
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            //发生错误
            ToastUtils.makeShortText("摄像头出错", DataActivity.this);
        }
    };

    /**
     * 会话状态回调
     */
    private CameraCaptureSession.StateCallback mSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            mSession = session;
            if (mCamera != null && captureRequestBuilder == null) {
                try {
                    captureRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                    // 将imageReader的surface作为CaptureRequest.Builder的目标
                    captureRequestBuilder.addTarget(mImageReader.getSurface());
                    // 关闭自动对焦
                    captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
                    captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
//                    // 设置拍摄图像时相机设备是否使用光学防抖（OIS）。
//                    captureRequestBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON);
//                    captureRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, valueISO);
                    // 曝光补偿
                    captureRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, 0);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
            try {
                updatePreview(session);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {

        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityDataBinding activityDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_data);
        viewModel = ViewModelProviders.of(this).get(DataViewModel.class);
        activityDataBinding.setViewmodel(viewModel);

        // 窗口配置
        WindowUtils.setStatusBarTranslucent(this);

        // 权限检查
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(DataActivity.this, new String[] {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_CAMERA_REQUEST_CODE);
        }

        ImageButton imgbtn = findViewById(R.id.imgbtn_back);
        imgbtn.setOnClickListener(v -> onBackPressed());

        iv_show = findViewById(R.id.iv_show);
        mSurfaceView = findViewById(R.id.sv_camera);
        mSurfaceView.setOnClickListener(view -> takePicture());
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setFixedSize(240, 320);
        mSurfaceHolder.setKeepScreenOn(true);


        // 很多过程都变成了异步的了，所以这里需要一个子线程的looper
        HandlerThread handlerThread = new HandlerThread("Camera2");
        handlerThread.start();
        childHandler = new Handler(handlerThread.getLooper());
        mainHandler = new Handler(getMainLooper());
        manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try {
                    // 需要相机权限
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    // 获取可用相机设备列表
                    String[] CameraIdList = manager.getCameraIdList();
                    // 打开相机
                    manager.openCamera(mCameraID, mCameraDeviceStateCallback, mainHandler);
                    CameraCharacteristics characteristics = manager.getCameraCharacteristics("1");
                    StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    Size[] mPreviewSize = map.getOutputSizes(SurfaceTexture.class);
                    Log.i(TAG, "compatible preview size:" + mPreviewSize.toString());
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                //释放资源
                if (mCamera != null) {
                    mCamera.close();
                    mCamera = null;
                }
            }
        });

        // 设置照片的大小
        mImageReader = ImageReader.newInstance(720, 960, ImageFormat.JPEG, 2);
        mImageReader.setOnImageAvailableListener(imageReader -> {
            // 拿到拍照照片数据
            Image image = imageReader.acquireNextImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            // 由缓冲区存入字节数组
            buffer.get(bytes);
            image.close();
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bitmap != null) {
                iv_show.setImageBitmap(bitmap);
            }
            viewModel.setImageByteArray(bytes);
//            // 保存照片
//            saveBitmap(bytes);
        }, mainHandler);




        viewModel.emotionLiveData.observe(this, emotionData -> {
            activityDataBinding.tvAngerValue.setText(String.valueOf(emotionData.anger_value));
            activityDataBinding.tvDisgustValue.setText(String.valueOf(emotionData.disgust_value));
            activityDataBinding.tvFearValue.setText(String.valueOf(emotionData.fear_value));
            activityDataBinding.tvHappinessValue.setText(String.valueOf(emotionData.happiness_value));
            activityDataBinding.tvNeutralValue.setText(String.valueOf(emotionData.neutral_value));
            activityDataBinding.tvSadnessValue.setText(String.valueOf(emotionData.sadness_value));
            activityDataBinding.tvSurpriseValue.setText(String.valueOf(emotionData.surprise_value));
            if (emotionData.errorMessage != "") {
                ToastUtils.makeShortText(emotionData.errorMessage, DataActivity.this);
            }
        });

    }



    /**
     * 开始预览，camera.createCaptureSession创建会话
     */
    private void startPreview(final CameraDevice camera) throws CameraAccessException {
        try {
            // 创建预览需要的CaptureRequest.Builder
            mPreviewBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            // 将SurfaceView的surface作为CaptureRequest.Builder的目标
            mPreviewBuilder.addTarget(mSurfaceHolder.getSurface());
            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
            mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
            //设置拍摄图像时相机设备是否使用光学防抖（OIS）。
            mPreviewBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON);
            //感光灵敏度
            mPreviewBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, 1600);
            //曝光补偿//
            mPreviewBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, 0);
            // 创建CameraCaptureSession，该对象负责管理处理预览请求和拍照请求
            camera.createCaptureSession(Arrays.asList(mSurfaceHolder.getSurface(), mImageReader.getSurface()), mSessionStateCallback, childHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新会话，开启预览
     *
     * @param session
     * @throws CameraAccessException
     */
    private void updatePreview(CameraCaptureSession session) throws CameraAccessException {
        session.setRepeatingRequest(mPreviewBuilder.build(), mCaptureCallback, childHandler);
    }

    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            //需要连拍时，循环保存图片就可以了
        }
    };

    /**
     * 单拍照片
     */
    private void takePicture() {
        if (mCamera == null) {
            return;
        }
        if (mSession != null && captureRequestBuilder != null) {
            // 获取手机方向
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            // 根据设备方向计算设置照片的方向
            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            // 拍照
            try {
                CaptureRequest cr = captureRequestBuilder.build();
                mSession.capture(cr, null, null); // 单拍API，也可以调连拍的哦
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

}
