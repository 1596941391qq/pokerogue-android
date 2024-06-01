package com.mengnieyu.helloworld;


import android.content.Context;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class DownloadBlobFileJSInterface {
    private static String mimeType = "";
    private static String fileExtension = ".prsv";
    private static String fileName = "";
    private Context mContext;
    private DownloadGifSuccessListener mDownloadGifSuccessListener;

    public DownloadBlobFileJSInterface(Context context) {
        this.mContext = context;
    }

    public void setDownloadGifSuccessListener(DownloadGifSuccessListener listener) {
        mDownloadGifSuccessListener = listener;
    }

    @JavascriptInterface
    public void getBase64FromBlobData(String base64Data) {
        convertToGifAndProcess(base64Data);
    }

    public static String getBase64StringFromBlobUrl(String blobUrl, String fileMimeType, String urlFileName) {
        if (blobUrl.startsWith("blob")) {
            mimeType = fileMimeType;
            fileName = urlFileName.endsWith(fileExtension) ? urlFileName : urlFileName.substring(0, 4) + fileExtension;
            return "javascript: var xhr = new XMLHttpRequest();" + "xhr.open('GET', '" + blobUrl + "', true);" + "xhr.setRequestHeader('Content-type','" + fileMimeType + ";charset=UTF-8');" + "xhr.responseType = 'blob';" + "xhr.onload = function(e) {" + "    if (this.status == 200) {" + "        var blobFile = this.response;" + "        var reader = new FileReader();" + "        reader.readAsDataURL(blobFile);" + "        reader.onloadend = function() {" + "            base64data = reader.result;" + "            Android.getBase64FromBlobData(base64data);" + "        }" + "    }" + "};" + "xhr.send();";

        }
        return "javascript: console.log('It is not a Blob URL');";
    }

    private void convertToGifAndProcess(String base64) {
        //File gifFile = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName);
        File downloadDirectory  = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!downloadDirectory.exists()) {
            downloadDirectory.mkdirs();
        }
        File gifFile = new File(downloadDirectory, fileName);
        Log.d("download","《《下载请求》》开始保存数据：\n" + base64 + "\n路径：" + gifFile.getPath().toString());
        saveGifToPath(base64, gifFile);
        Toast.makeText(mContext, "保存成功", Toast.LENGTH_SHORT).show();

        if (mDownloadGifSuccessListener != null) {
            mDownloadGifSuccessListener.downloadGifSuccess(gifFile.getAbsolutePath());
        }
    }

    private void saveGifToPath(String base64, File gifFilePath) {
        try {
            byte[] fileBytes = Base64.decode(base64.replaceFirst("data:" + mimeType + ";base64,", ""), 0);
            FileOutputStream os = new FileOutputStream(gifFilePath, false);
            os.write(fileBytes);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface DownloadGifSuccessListener {
        void downloadGifSuccess(String absolutePath);
    }

}
