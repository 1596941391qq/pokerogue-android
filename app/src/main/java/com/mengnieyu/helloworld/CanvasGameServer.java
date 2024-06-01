package com.mengnieyu.helloworld;

import static org.nanohttpd.protocols.http.response.Response.newFixedLengthResponse;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.request.Method;
import org.nanohttpd.protocols.http.response.Response;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

public class CanvasGameServer extends NanoHTTPD {

//  private final AssetManager assetManager;

    private AssetManager assetManager;
    public CanvasGameServer(String hostname, int port, Context context) throws Exception {
        super(hostname,port); // 监听端口

       this.assetManager = context.getAssets();

    }

    @Override
    public Response serve(IHTTPSession session) throws IOException {
        String uri = session.getUri();

        // 将请求的 URI 转换为 assets 文件夹中的路径
        String path = uri.contains("?") ? uri.split("\\?")[0] : uri;

        // 转换为 assets 文件夹中的相对路径
        String assetPath = "web" + (path.startsWith("/") ? "" : "/") + path;

        assetPath = assetPath.replaceAll("//","/");

//        System.out.println("assetPath:"+assetPath);

        // 检查是否为目录并添加索引文件
        if (assetPath.endsWith("/")) {
            assetPath += "index.html"; // 假设默认文件为 index.html
        }
        // 尝试从 assets 文件夹中打开文件

        InputStream assetInputStream;

        try {
//            Log.d("Assets", "路径:"+assetPath);
           assetInputStream = assetManager.open(assetPath);

        } catch (FileNotFoundException e) {
            Log.d("NanoHttpServer", "File not found: " + assetPath, e);
            return null;
        } catch (IOException e) {
            // 文件未找到，返回 404 错误
            Log.d("Assets", "路径:"+assetPath);
            e.printStackTrace();

            return null;

        }


        // 获取文件的 MIME 类型
        String mimeType = getMimeType(assetPath);

        // 读取文件内容
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        try {
            while ((nRead = assetInputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
        } catch (IOException e) {
            try {
                assetInputStream.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            return newFixedLengthResponse(new CustomStatus(500, "server error"), mimeType, buffer.toByteArray());

        } finally {
            try {
                assetInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 返回文件内容和 MIME 类型
        return newFixedLengthResponse(new CustomStatus(200, "OK"), mimeType, buffer.toByteArray());

    }



        // 根据文件扩展名获取MIME类型
    private String getMimeType (String filePath) throws IOException {

        String mimeType = null; // 默认 MIME 类型
        Path path= null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            path = new File(filePath).toPath();
            mimeType = Files.probeContentType(path);

        }
        String ext = filePath.substring(filePath.lastIndexOf(".")).toLowerCase();
        if (ext.equals(".html")) {
            mimeType= "text/html";
        } else if (ext.equals(".css")) {
            mimeType= "text/css";
        } else if (ext.equals(".js")) {
            mimeType= "application/javascript";
        }
        else if (ext.equals(".png")) {
            mimeType = "image/png"; // PNG 图像文件
        } else if (ext.equals(".jpg") || ext.equals(".jpeg")) {
            mimeType = "image/jpeg";
        } else if (ext.equals(".json")) {
            mimeType = "application/json"; // JSON 数据文件
        }
        else if (ext.equals(".ps1")) {
            mimeType = "application/x-powershell"; // PowerShell 脚本文件
        }
        else if (ext.equals(".m4a")) {
            mimeType = "audio/m4a"; // MPEG-4 音频文件
        } else if (ext.equals(".mp3")) {
            mimeType = "audio/mpeg"; // MPEG 音频层 III 文件
        }
        else if (ext.equals(".gif")) {
            mimeType = "image/gif";}
        else if (ext.equals(".ttf")) {
            mimeType = "font/ttf"; // TrueType 字体文件
        }
        else if (ext.equals(".py")) { // Python 脚本文件
            mimeType = "text/plain";
        } else if (ext.equals(".webmanifest")) { // Web 应用 manifest 文件
            mimeType = "application/manifest+json";
        } else if (ext.equals(".bat")) { // Windows 批处理文件
            mimeType = "application/x-msdownload"; // 或 "text/plain"
        }
        else if (ext.equals(".wav")) { // Wave 音频文件
            mimeType = "audio/wav"; // 或 "audio/x-wav"
        }
        else if (ext.equals(".xml")) { // XML 文件
            mimeType = "application/xml"; // 或 "text/xml"
        }
        else if (ext.equals(".prsv")) { // prsv 文件
            mimeType = "text/json"; // 或 "text/json"
        }
        return mimeType;
    }
}


