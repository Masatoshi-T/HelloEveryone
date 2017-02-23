package jp.techacademy.masatoshi.tashiro.helloeveryone;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.SurfaceHolder.Callback;
import android.view.WindowManager.LayoutParams;
import android.view.SurfaceView;


public class MainActivity extends Activity{
    private SurfaceView mSvFacePreview;
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera = null;
    private FaceMarkerView mFaceMarkerView;
    private int w;
    private int h;
    private MediaPlayer mediaPlayer;
    private boolean isCheck;

    int[] mp3Sounds = new int[] {R.raw.konbini1, R.raw.conbini2, R.raw.hakken, R.raw.josei_hay,
    R.raw.josei_irassyaimase, R.raw.josei_arigatou, R.raw.josei_sinnyu};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isCheck = true;
        mSvFacePreview = (SurfaceView) findViewById(R.id.surface_view);
        mSurfaceHolder = mSvFacePreview.getHolder();
        mSurfaceHolder.addCallback(new Callback() {

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (mCamera != null) {
                    mCamera.stopFaceDetection();
                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera = null;
                }
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mCamera = Camera.open();
                if (mCamera != null) {
                    try {
                        mCamera.setDisplayOrientation(90);
                        mCamera.setPreviewDisplay(mSurfaceHolder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (mCamera != null) {
                    Parameters params = mCamera.getParameters();
                    w = width;
                    h = height;

                    mCamera.startPreview();

                    int maxFaces = params.getMaxNumDetectedFaces();

                    if (maxFaces > 0) {
                        mCamera.setFaceDetectionListener(new FaceDetectionListener() {
                            @Override
                            public void onFaceDetection(Face[] faces, Camera camera) {
                                mFaceMarkerView.faces = faces;
                                mFaceMarkerView.invalidate();
                                if (faces.length > 0 && isCheck) {
                                    audioSetup();
                                    audioPlay();
                                    Log.d("Log",String.valueOf(isCheck));
                                }
                            }
                        });
                    }

                    try {
                        mCamera.startFaceDetection();
                    } catch (IllegalArgumentException e) {

                    } catch (RuntimeException e) {
                    }
                }
            }
        });

        mFaceMarkerView = new FaceMarkerView(this);
        addContentView(mFaceMarkerView, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    private class FaceMarkerView extends View {
        Face[] faces;

        public FaceMarkerView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            canvas.drawColor(Color.TRANSPARENT);
            Paint paint = new Paint();
            paint.setColor(Color.CYAN);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            if (faces != null) {
                for (int i = 0; i < faces.length; i++) {
                    int saveState = canvas.save();
                    Matrix matrix = new Matrix();
                    boolean mirror = false;
                    matrix.setScale(mirror ? -1 : 1, 1);
                    matrix.postRotate(90);
                    matrix.postScale(getWidth() / 2000f, getHeight() / 2000f);
                    matrix.postTranslate(getWidth() / 2f, getHeight() / 2f);
                    canvas.concat(matrix);
                    Log.d("Log", "faces_rect=" + faces[i].rect);
                    canvas.drawRect(faces[i].rect, paint);
                    canvas.restoreToCount(saveState);
                }
            }
        }
    }

    private boolean audioSetup(){
        boolean fileCheck = false;
        mediaPlayer = new MediaPlayer();
        Random random = new Random();
        int n = random.nextInt(7);
        mediaPlayer=MediaPlayer.create(this,mp3Sounds[n]);
        Log.d("Log_random",String.valueOf(n));
        this.mediaPlayer.setVolume(1.0f, 1.0f);
        fileCheck = true;
        return fileCheck;
    }

    private void audioPlay() {
        isCheck = false;
        Log.d("Log_music","成功");
        mediaPlayer.start();
        // 終了を検知するリスナー
        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                isCheck = true;
                audioStop();
                Log.d("Log_music","終わったよ");
            }
        });
    }

    private void audioStop() {
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;
    }
}