package com.vsb.kru13.osmzhttpserver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SocketServer s;
    private static final int READ_EXTERNAL_STORAGE = 1;
    private Handler handler;
    private TextView textView1, textView2, textView3;
    private Bundle bundle;
    private String path, type;
    private long size;
    private long sizeOverall = 0;
    private EditText maxThreds;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn1 = (Button)findViewById(R.id.button1);
        Button btn2 = (Button)findViewById(R.id.button2);
        Button cameraActivBtn = (Button)findViewById(R.id.cameraActivityBtn);

        textView1 = (TextView)findViewById(R.id.textView);
        textView2 = (TextView)findViewById(R.id.textView2);
        textView3 = (TextView)findViewById(R.id.textView3);



        updateTextView1("");
        updateTextView2("");
        updateTextView3("");
        updateTextView4("");

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                bundle = getIntent().getExtras();
                bundle = msg.getData();
                path = bundle.getString("path");
                type = bundle.getString("type");
                Log.d("text", path);
                Log.d("text", type);
                size = bundle.getLong("sizeFile");
                Log.d("text", Long.toString(size));
                sizeOverall += size;

                updateTextView1(path);
                updateTextView2(Long.toString(size));
                updateTextView3(Long.toString(sizeOverall));
                updateTextView4(type);

            }
        };

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        cameraActivBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), CameraActivity.class);

                startActivity(intent);
            }
        });
    }

    public void updateTextView1(String toThis) {
        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(toThis);
    }

    public void updateTextView2(String toThis) {
        TextView textView = (TextView) findViewById(R.id.textView2);
        textView.setText(toThis);
    }

    public void updateTextView3(String toThis) {
        TextView textView = (TextView) findViewById(R.id.textView3);
        textView.setText(toThis);
    }

    public void updateTextView4(String toThis) {
        TextView textView = (TextView) findViewById(R.id.textView10);
        textView.setText(toThis);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.button1) {

            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
            } else {
                maxThreds = (EditText)findViewById(R.id.maxThreads);
                if(maxThreds.getText().toString().isEmpty()){
                    Toast.makeText(this,"Zadej počet vláken", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(this,"Server zapnut s poctem vláken:" + maxThreds.getText().toString(), Toast.LENGTH_LONG).show();
                    s = new SocketServer(handler, maxThreds.getText().toString());
                    s.start();
                }

            }
        }
        if (v.getId() == R.id.button2) {
            s.close();
            try {
                s.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {

            case READ_EXTERNAL_STORAGE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    maxThreds = (EditText)findViewById(R.id.maxThreads);
                    if(maxThreds.getText().toString() == null){
                        Toast.makeText(this,"Zadej počet vláken", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(this,"Server zapnut s poctem vláken:" + maxThreds.getText().toString() , Toast.LENGTH_LONG).show();
                        s = new SocketServer(handler, maxThreds.getText().toString());
                        s.start();
                    }
                }
                break;

            default:
                break;
        }
    }
}
