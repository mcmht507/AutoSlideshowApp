package jp.techacademy.tomoya.miyoshi.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

import static android.os.Build.VERSION_CODES.M;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final String CONST_PLAY = "再生";
    private static final String CONST_STOP = "停止";
    private int imageIndex = 0;
    Timer timer;
    Handler handler = new Handler();
    Button nextBtn;
    Button backBtn;
    Button toggleBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 画像取得の許可
        if (Build.VERSION.SDK_INT >= M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                btnEnableSetting();
            } else {
                // 許可されていなにのでダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 許可された場合
                btnEnableSetting();
            } else {
                // 許可されなかった場合
                btnDisableSetting();
            }
        }
    }

    private void btnEnableSetting() {
        // 次へボタン作成
        nextBtn = (Button) findViewById(R.id.nextBtn);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageIndex = nextImage(imageIndex);
            }
        });
        // 戻るボタン作成
        backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageIndex = backImage(imageIndex);
            }
        });
        // 再生／停止ボタン作成
        toggleBtn = (Button) findViewById(R.id.toggleBtn);
        toggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 文字の表示切り替え
                String btnText = toggleBtn.getText().toString();
                if(CONST_PLAY.equals(btnText)){
                    toggleBtn.setText(CONST_STOP);
                } else {
                    toggleBtn.setText(CONST_PLAY);
                }

                if (timer == null) {
                    // 進むボタンと戻るボタンを非活性
                    nextBtn.setEnabled(false);
                    backBtn.setEnabled(false);

                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    imageIndex = nextImage(imageIndex);

                                }
                            });
                        }
                    }, 2000, 2000);
                } else if (timer != null) {
                    nextBtn.setEnabled(true);
                    backBtn.setEnabled(true);
                    timer.cancel();
                    timer = null;
                }
            }
        });
    }

    private void btnDisableSetting() {
        // 次へボタン作成
        nextBtn = (Button) findViewById(R.id.nextBtn);
        nextBtn.setEnabled(false);
        // 戻るボタン作成
        backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setEnabled(false);
        // 再生／停止ボタン作成
        toggleBtn = (Button) findViewById(R.id.toggleBtn);
        toggleBtn.setEnabled(false);
    }

    private int nextImage(int imageIndex) {
        // 画像の情報を取得する
        Cursor cursor = getCursor();
        imageIndex = imageIndexAdjust(imageIndex, cursor.getCount());
        if (cursor.moveToPosition(imageIndex)) {
            // 画像のURI取得する
            int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            Long id = cursor.getLong(fieldIndex);
            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageURI(imageUri);
            imageIndex++;
        }
        cursor.close();
        return imageIndex;
    }

    private int backImage(int imageIndex) {
        // 画像の情報を取得する
        Cursor cursor = getCursor();
        imageIndex = imageIndexAdjust(imageIndex, cursor.getCount());
        if (cursor.moveToPosition(imageIndex)) {
            // 画像のURI取得する
            int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            Long id = cursor.getLong(fieldIndex);
            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageURI(imageUri);
            imageIndex--;
        }
        cursor.close();
        return imageIndex;
    }

    private Cursor getCursor() {
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
        );
        return cursor;
    }

    private int imageIndexAdjust(int imageIndex, int imageCount) {
        if (imageIndex < 0) {
            imageIndex = imageCount - 1;
        } else if (imageCount <= imageIndex) {
            imageIndex = 0;
        }
        return imageIndex;
    }
}
