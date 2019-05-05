package com.example.downloader;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{



    ImageView my_image;
    ProgressBar progressBar;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private TextView textView;

// broadcast receiver for download finished notification
    private BroadcastReceiver notificationReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String string = bundle.getString(DownloadService.FILEPATH);
                int resultCode = bundle.getInt(DownloadService.RESULT);
                if (resultCode == RESULT_OK) {
                    Toast.makeText(MainActivity.this,
                            "Download complete. Download URI: " + string,
                            Toast.LENGTH_LONG).show();
                    textView.setText("Download done");
                    String imagePath = Environment.getExternalStorageDirectory() + "/Sample-jpg-image-2mb.jpg";
                    my_image.setImageDrawable(Drawable.createFromPath(imagePath));
                    my_image.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(MainActivity.this, "Download failed",
                            Toast.LENGTH_LONG).show();
                    textView.setText("Download failed");
                }
            }
        }
    };
//broadcast receiver for porgressbar update
    private BroadcastReceiver progressReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int progress = bundle.getInt(DownloadService.ProgressValue);
                boolean bool= bundle.getBoolean(DownloadService.Complete);

                if(bool)
                    progressBar.setVisibility(View.GONE);
                progressBar.setProgress(progress);
            }
        }
    };




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check if runtime read and write permissions are necessary
        if (Build.VERSION.SDK_INT >= 23)
        {
            if (!checkPermission())
            {
                requestPermission();
            }
        }

        textView = findViewById(R.id.status);
        progressBar=findViewById(R.id.progressBar);
        my_image=findViewById(R.id.imageView);


    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(notificationReceiver, new IntentFilter(
                DownloadService.NOTIFICATION));
        registerReceiver(progressReceiver, new IntentFilter(
                DownloadService.Progress));
    }

    public void onClick(View view) {

        my_image.setVisibility(View.GONE);
        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, DownloadService.class);
        // add infos for the service which file to download and where to store
        intent.putExtra(DownloadService.FILENAME, "Sample-jpg-image-2mb.jpg");
        intent.putExtra(DownloadService.URL,
                "https://sample-videos.com/img/Sample-jpg-image-2mb.jpg");
        startService(intent);
        textView.setText("Service started");
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Write External Storage permission is important because we need to write the downloaded file in external storage.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted");
                } else {
                    Log.e("value", "Permission Denied");
                }
                break;
        }
    }
}
