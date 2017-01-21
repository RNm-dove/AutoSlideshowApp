package com.example.ryosuke.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.LogRecord;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    Button backButton;
    Button autoButton;
    Button goButton;
    ImageView imageView;

    Cursor cursor;
    int itemNum; //データの列数

    Timer mTimer;
    Handler mHandler = new Handler();
    int flag = 0; //ボタンのflag


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Android6.0以降の場合
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            //パーミッションの許可の状態を確認
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                //許可されている
                getContentsInfo();
            } else {
                //許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},PERMISSIONS_REQUEST_CODE);
            }
            //android5系以下の場合
        } else {
            getContentsInfo();
        }
    }

    @Override
    public  void onRequestPermissionsResult(int requestCode, String permissions[],int[] grantResults){
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                }
                break;
            default:
                break;
        }
    }

    private void getContentsInfo(){

        backButton = (Button) findViewById(R.id.back_button);
        autoButton = (Button) findViewById(R.id.auto_button);
        goButton = (Button) findViewById(R.id.go_button);
        imageView = (ImageView) findViewById(R.id.imageView);


        //画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if(cursor.moveToFirst()) {
            setImage();

            backButton.setOnClickListener(this);
            autoButton.setOnClickListener(this);
            goButton.setOnClickListener(this);
        }

    }

    @Override
    public void onClick(View v){

        itemNum = cursor.getCount(); //データの数
        //flag =0 戻る、進むボタンは押せる
        //flag =1  戻る、進むボタンは押せない

        switch (v.getId()){
            case R.id.back_button:
                if(flag == 0) {
                    if (cursor.getPosition() == 0) {
                        cursor.move(itemNum - 1);
                    } else {
                        cursor.move(-1);
                    }
                    setImage();
                }
                break;
            case R.id.go_button:
                if(flag== 0) {
                    if (cursor.getPosition() == itemNum - 1) {
                        cursor.move(-itemNum + 1);
                    } else {
                        cursor.move(1);
                    }
                    setImage();
                }
                break;
            case R.id.auto_button:
                if(flag == 0){
                    flag =1;
                    setTimer();
                } else {
                    flag = 0;
                    if(mTimer != null){
                        mTimer.cancel();
                        mTimer = null;
                    }
                }
                break;
            default: break;
        }


    }

    private void setImage(){
        // indexからIDを取得し、そのIDから画像のURIを取得する
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
        imageView.setImageURI(imageUri);
    }

    private void setTimer(){ //timer 作成

        if(mTimer == null) {
            mTimer = new Timer();

            // タイマーの始動
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (cursor.getPosition() == itemNum - 1) {
                                cursor.move(-itemNum + 1);
                            } else {
                                cursor.move(1);
                            }
                            setImage();
                        }
                    });
                }
            }, 100, 2000);    // 最初に始動させるまで 100ミリ秒、ループの間隔を 2000ミリ秒 に設定
        }

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        cursor.close();
    }
}
