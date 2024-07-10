package com.mengnieyu.helloworld;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.Manifest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private ValueCallback<Uri[]> mfilePathCallback;
    private final int FILECHOOSER_RESULTCODE=1;

    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //看看还差什么权限
        request_permissions();
        //获取权限
        requestmanageexternalstorage_Permission();
        //隐藏导航
        hideUIoption();
        //锁屏幕
        lockPositive();

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        webView = new WebView(this);
        webView.clearCache(true);
        FrameLayout myFrameLayout = findViewById(R.id.my_frame_layout);
        myFrameLayout.addView(webView);//动态加载

        //设置webview
        setWebViewSetting();
        webView.addJavascriptInterface(new DownloadBlobFileJSInterface(this), "Android");



        webView.setWebChromeClient(new WebChromeClient(){
           @Override
           public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
               if (mfilePathCallback != null) {
                   mfilePathCallback.onReceiveValue(null);
               }
               mfilePathCallback = filePathCallback;
               Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
               contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
               contentSelectionIntent.setType("*/*");

               Intent[] intentArray;
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                   intentArray = new Intent[]{contentSelectionIntent};
               } else {

                   String packageName = getPackageName();
                   Uri uri = Uri.parse("content://" + packageName + ".fileprovider/read_file");
                   grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                   Intent intent = new Intent(Intent.ACTION_PICK);
                   intent.setDataAndType(uri, "*/*");
                   intentArray = new Intent[]{intent};
               }

               startActivityForResult(Intent.createChooser(contentSelectionIntent, "File Chooser"), FILECHOOSER_RESULTCODE);
               return true;
           }


        });


        webView.setDownloadListener(new DownloadListener() {

            //处理下载事件
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                if (TextUtils.isEmpty(url)) {
                    return;
                }
                if (url.startsWith("blob")) {
                    String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
                    Log.d("download", "filename：" + fileName);
                    // 3. 执行JS
                    webView.loadUrl(DownloadBlobFileJSInterface.getBase64StringFromBlobUrl(url, mimetype, fileName));

                }


            }
        });

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {


                return super.shouldOverrideUrlLoading(view, request);
            }




            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                // 返回null，让WebView自己处理请求

                return null;

            }

        });


        String ip = getLocalIpAddress();


        try {
            CanvasGameServer server = new CanvasGameServer(ip,6868, this);
            server.start();
//            System.out.println("Server started at port 6868");
        } catch (IOException e) {
            e.printStackTrace();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

//        webView.loadUrl("http://10.120.171.38:6868/");

        webView.loadUrl("http://localhost:6868/");


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != FILECHOOSER_RESULTCODE || mfilePathCallback == null) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        Uri uri = data.getData();


        // Check that the response is a good one
        if (resultCode == Activity.RESULT_OK&&data != null) {

            mfilePathCallback.onReceiveValue(new Uri[]{uri});

        }


    }


    // 获取ip地址
    private String getLocalIpAddress() {

        return "0.0.0.0";
    }
    //隐藏导航
    private void hideUIoption() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void setWebViewSetting(){
        WebSettings webSettings = webView.getSettings();
        //WebView.setWebContentsDebuggingEnabled(true);
        webSettings.setDomStorageEnabled(true);// 启用DOM存储，允许网页使用localStorage和sessionStorage
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); // 允许JavaScript自动打开新窗口
        webSettings.setJavaScriptEnabled(true);// 启用JavaScript，允许网页运行JavaScript代码
        webSettings.setAllowFileAccess(true);    // 允许WebView访问文件系统上的文件
        webSettings.setAllowUniversalAccessFromFileURLs(true);// 允许从文件URL访问所有URL
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);  // 允许从HTTPS加载HTTP资源
        webSettings.setDatabaseEnabled(true);// 启用数据库存储，允许网页使用数据库
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); // 设置缓存模式为首先尝试加载缓存数据，如果失败再从网络加载m
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);


    }
    private void request_permissions() {
        // 创建一个权限列表，把需要使用而没用授权的的权限存放在这里
        List<String> permissionList = new ArrayList<>();

        // 判断权限是否已经授予，没有就把该权限添加到列表中
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("permission", "WRITE_EXTERNAL_STORAGE");
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            Log.d("permission", "READ_EXTERNAL_STORAGE");
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE);
            Log.d("permission", "MANAGE_EXTERNAL_STORAGE");
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_MEDIA_IMAGES);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_MEDIA_AUDIO);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_MEDIA_VIDEO);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.INTERNET);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_NETWORK_STATE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_WIFI_STATE);
        }

        // 如果列表为空，就是全部权限都获取了，不用再次获取了。不为空就去申请权限
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionList.toArray(new String[permissionList.size()]), 1002);
        } else {
            Toast.makeText(this, "多个权限你都有了，不用再次申请", Toast.LENGTH_LONG).show();
        }
    }
    private void requestmanageexternalstorage_Permission() {
        if (Build.VERSION.SDK_INT >= 23) {// 6.0
            String[] perms = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE



            };
            for (String p : perms) {
                int f = ContextCompat.checkSelfPermission(this, p);

                if (f != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(perms, 0XCF);
                    break;
                }
            }
        }
      if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 先判断有没有权限
            Log.d("permission", "requestmanageexternalstorage");
            if (Environment.isExternalStorageManager()) {
                Toast.makeText(this, "Android VERSION  R OR ABOVE，HAVE MANAGE_EXTERNAL_STORAGE GRANTED!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Android VERSION  R OR ABOVE，NO MANAGE_EXTERNAL_STORAGE GRANTED!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                startActivityForResult(intent, REQUEST_CODE_ASK_PERMISSIONS);
            }
        }

    }

    private void lockPositive(){
        // 创建AlertDialog.Builder对象
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

// 设置对话框标题
        builder.setTitle("选择屏幕方向");

// 设置对话框内容
        builder.setMessage("本apk由B站up主黑咖啡和冰月亮免费提供，禁止倒卖,\n您希望锁定屏幕方向为横屏还是竖屏？");

// 为横屏选项设置监听器
        builder.setPositiveButton("横屏", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // 用户选择横屏
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        });

// 为竖屏选项设置监听器
        builder.setNegativeButton("竖屏", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // 用户选择竖屏
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        });

// 为取消操作设置监听器
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // 用户取消选择
                // 这里可以添加取消操作的逻辑
            }
        });

// 创建并显示对话框
        builder.create().show();
    }










}