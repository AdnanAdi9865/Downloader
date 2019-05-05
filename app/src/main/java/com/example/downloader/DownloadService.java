package com.example.downloader;


import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;


public class DownloadService extends IntentService {

    private int result = Activity.RESULT_CANCELED;
    public static final String URL = "urlpath";
    public static final String FILENAME = "filename";
    public static final String FILEPATH = "filepath";
    public static final String RESULT = "result";
    public static final String NOTIFICATION = "service.download.complete";
    public static final String Progress="service.download.progress";
    public static final String ProgressValue="value";
    public static final String Complete="complete";


    public DownloadService() {
        super("DownloadService");
    }

    // will be called asynchronously by Android and download will happen in the background thread

    @Override
    protected void onHandleIntent(Intent intent) {
        String urlPath = intent.getStringExtra(URL);
        String fileName = intent.getStringExtra(FILENAME);
        File imageFile = new File(Environment.getExternalStorageDirectory(),
                fileName);
        if (imageFile.exists()) {
            imageFile.delete();
        }

        int count;
        try{
            URL url = new URL(urlPath);
            URLConnection connection = url.openConnection();
            connection.connect();

            int lengthOfFile = connection.getContentLength();

            InputStream input = new BufferedInputStream(url.openStream(), 8192);


            OutputStream output = new FileOutputStream(imageFile);

            byte data[] = new byte[1024];
            long total = 0;

            while((count = input.read(data)) != -1){
                total += count;

                publishProgress((int)((total*100)/lengthOfFile),false);

                output.write(data, 0, count);
            }
            output.flush();
            result=Activity.RESULT_OK;
            output.close();
            input.close();
        }catch (Exception e){
            Log.e("Error: ", e.getMessage());
        }
        publishProgress(100,true);
        publishResults(imageFile.getAbsolutePath(), result);

    }
    //for publishing that download completed successfully
    private void publishResults(String outputPath, int result) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(FILEPATH, outputPath);
        intent.putExtra(RESULT, result);
        sendBroadcast(intent);
    }
    //for notifying the progress of the download
    public void publishProgress (int progress,boolean complete)
    {
        Intent intent = new Intent(Progress);
        intent.putExtra(ProgressValue, progress);
        intent.putExtra(Complete, complete);
        sendBroadcast(intent);
    }
}
