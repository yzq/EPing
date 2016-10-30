package com.yzq.android.eping;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PingActivity extends AppCompatActivity {

    private Button mPingButton;
    private EditText mIpAddress;
    private EditText mPacketCount;
    private EditText mPacketSize;
    private TextView mResponse;
    private ScrollView mScroll;
    public static final String TAG = "PingActivity";
    public static final int SHOW_RESULT = 0;
    Process process = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ping);

        mPingButton = (Button) findViewById(R.id.btn_ping);
        mIpAddress = (EditText) findViewById(R.id.edit_ip);
        mPacketCount = (EditText) findViewById(R.id.edit_count);
        mPacketSize = (EditText) findViewById(R.id.edit_size);
        mResponse = (TextView) findViewById(R.id.tv_result);
        mScroll = (ScrollView) findViewById(R.id.sv_scroll);

        mPingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(PingActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                if (process != null) {
                    process.destroy();
                }
                mResponse.setText("");
                String ip = mIpAddress.getText().toString();

                String count = mPacketCount.getText().toString().trim();
                String size = mPacketSize.getText().toString().trim();
                Log.d(TAG, "count0: "+ count);
                String countCmd = "";
                String sizeCmd = "";
                if (!("".equals(count))) {
                    Log.d(TAG, "count: "+ count);
                    countCmd = "-c " + count + " ";
                }
                if (!("".equals(size))) {
                    sizeCmd = "-s " + size + " ";
                }


                //String countCmd = " -c " + count + " ";
                //String sizeCmd = "-s " + size + " ";
                String pingCmd = "ping " + countCmd + sizeCmd + ip;

                startPing(pingCmd);
            }
        });

    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_RESULT:
                    String result = (String) msg.obj;
                    mResponse.append(result);
                    scrollToBottom(mScroll, mResponse);
            }
        }
    };

    private void startPing(final String ping) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    process = Runtime.getRuntime().exec(ping);
                    Log.d(TAG, "receive the command: "+ ping);

                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    InputStream in = process.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Log.d(TAG, "receiving: "+ line);

                        Message message = new Message();
                        message.what = SHOW_RESULT;
                        message.obj = line + "\r\n";
                        Log.d(TAG, "get result: "+ message.obj);
                        mHandler.sendMessage(message);
                    }

                    while ((line = errorReader.readLine()) != null) {
                        Log.i(TAG, "error: " + line);
                        Message message = new Message();
                        message.what = SHOW_RESULT;
                        message.obj = line + "\r\n";
                        mHandler.sendMessage(message);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (process != null) {
                        process.destroy();
                    }
                }
            }
        }).start();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
// TODO Auto-generated method stub
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            System.out.println("down");
            if (PingActivity.this.getCurrentFocus() != null) {
                if (PingActivity.this.getCurrentFocus().getWindowToken() != null) {
                    //调用系统自带的隐藏软键盘
                    ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(PingActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        }
        return super.onTouchEvent(event);
    }

    public static void scrollToBottom(final ScrollView scroll, final View inner) {

        Handler mHandler = new Handler();

        mHandler.post(new Runnable() {
            public void run() {

                if (scroll == null || inner == null) {
                    return;
                }
                scroll.fullScroll(ScrollView.FOCUS_DOWN);

                /*int offset = inner.getMeasuredHeight() - scroll.getHeight();
                if (offset < 0) {
                    offset = 0;
                }

                scroll.scrollTo(0, offset);*/
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (process != null) {
            process.destroy();
        }
    }

}
