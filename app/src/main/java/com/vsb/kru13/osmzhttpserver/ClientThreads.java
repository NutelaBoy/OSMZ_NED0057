package com.vsb.kru13.osmzhttpserver;


import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.MimeTypeMap;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import static com.vsb.kru13.osmzhttpserver.CameraActivity.imageInBytes;
import static com.vsb.kru13.osmzhttpserver.CameraActivity.streamingIsUp;

public class ClientThreads extends Thread {

    private Socket s;
    private Handler handler;
    private Bundle bundle;
    private Message msg;
    private Semaphore semaphore;


    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }


    public ClientThreads(Socket s, Handler handler, Semaphore semaphore) {
        this.s = s;
        this.handler = handler;
        this.semaphore = semaphore;


    }

    public void run() {
        try {
            Log.d("SERVER", "Socket Accepted");
            OutputStream o = s.getOutputStream();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(o));
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

            String uri = "";
            String type = "";
            String camera = "/camera/snapshot";
            String stream = "/camera/stream";
            String bin = "/cgi-bin";

            String tmp = in.readLine();
            if(tmp !=null && !tmp.isEmpty()){
                type = tmp.split(" ")[0];
                uri = tmp.split(" ")[1];

                if(uri.contains(bin)){

                    String command = "uptime";
                    List<String> alist = new ArrayList<>();
                    alist.add("cmd.exe");
                    alist.add("/C");
                    ProcessBuilder processBuilder = new ProcessBuilder();
                    processBuilder.command(alist);
                    //processBuilder.command("cmd.exe","/c","cd C:\\Users\\matej\\AppData\\Local\\Android\\Sdk\\platform-tools\\adb shell " + command);
                    try {
                        processBuilder.start();
                    }catch (IOException e ){
                        e.printStackTrace();
                    }

                    //File myObj = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "filename.txt");
                    //processBuilder.redirectOutput(myObj);
                }

                if(uri.contains(camera)){

                    if(imageInBytes != null) {

                        out.flush();
                        out.write("HTTP/1.0 200 OK\n" +
                                "Content-Type: image/jpeg\n\n");
                        out.flush();
                        o.write(imageInBytes);

                        o.flush();
                    }
                }

                if(uri.contains(stream)){

                    if(imageInBytes != null) {

                        out.flush();
                        out.write("HTTP/1.0 200 OK\n" +
                                "Content-Type: multipart/x-mixed-replace; boundary=\"OSMZ_boundary\"\n\n");



                        while(true){
                            out.flush();
                            out.write("--OSMZ_boundary\n" +
                                    "Content-Type: image/jpeg\n\n");
                            out.flush();
                            o.write(imageInBytes);
                            o.flush();

                            if (!streamingIsUp) {
                                break;
                            }
                        }


                        out.write("--OSMZ_boundary");

                        out.flush();
                        o.flush();

                    }

                }
            }


            String path = "";
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
            File f = new File(path + uri);

            bundle = new Bundle();
            msg = new Message();

            bundle.putString("type", type);
            bundle.putString("path", path + uri);
            bundle.putLong("sizeFile", f.length());
            msg.setData(bundle);
            handler.sendMessage(msg);

            if(!f.exists()){
                out.write("HTTP/1.0 404 Not found\n" +
                            "Content-Type: text/html\n" +
                            "\n" +
                            "<html>\n" +
                            "<body>\n" +
                            "<h1>Not found</h1></body></html>");
            }
            else{
                if(f.isFile()){
                        out.write("HTTP/1.0 200 OK\n" +
                            "Content-Type: " + getMimeType(f.getAbsolutePath()) + "\n" +
                            "Content-Type: " + f.length() + "\n\n");
                        out.flush();
                        FileInputStream inputStream = new FileInputStream(f);
                        byte[] byteArray = new byte[(int)f.length()];
                        inputStream.read(byteArray);
                        o.write(byteArray);
                }
                else{

                    File d = new File(path + uri);
                    File[] files = d.listFiles();
                    String vypis = "";
                    StringBuilder builder = new StringBuilder();
                    Log.d("files", files[0].getName());
                    for (File inFile : files)
                    {
                        builder.append(inFile.getName());
                        builder.append("<br>");
                    }
                    Log.d("text", vypis);
                    out.write("HTTP/1.0 404 Not found\n" +
                            "Content-Type: text/html\n" +
                            "\n" +
                            "<html>\n" +
                            "<body>\n" +
                            "<h1>Directory listing</h1>" +
                            "<h3>" + builder + "</h3></body></html>");
                }
        }
            out.flush();
            o.flush();
            s.close();
            Log.d("SERVER", "Socket Closed");
} catch (IOException e)
        {
            if (s != null && s.isClosed())
                Log.d("SERVER", "Normal exit");
            else {
                Log.d("SERVER", "Error");
                e.printStackTrace();
                }
        }
        finally {

                s = null;
                semaphore.release();
                }
    }
}
