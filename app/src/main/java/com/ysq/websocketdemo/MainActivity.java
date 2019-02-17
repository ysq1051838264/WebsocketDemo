package com.ysq.websocketdemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonObject;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;
import de.tavendo.autobahn.WebSocketOptions;

public class MainActivity extends Activity implements View.OnClickListener {
    private Button connectBtn;
    private Button disconnectBtn;
    private TextView messageTv;
    private EditText sendMsgEdit;
    private Button sendMsgBtn;

    private static WebSocketConnection webSocketConnection;
    private static WebSocketOptions options = new WebSocketOptions();
    private static boolean isExitApp = false;

    private static String websocketHost = "ws://192.168.43.43:9001/websocket/CH002";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        initViews();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", 1);
        jsonObject.addProperty("braceletNo", "CH00101");
        jsonObject.addProperty("avatarUrl", "CH00101");
        jsonObject.addProperty("nickName", "yang");
        jsonObject.addProperty("sex", 1);
        jsonObject.addProperty("sportType", 103);
        jsonObject.addProperty("userId", 1);

        sendMsgEdit.setText(jsonObject.toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.connect_btn:
                //连接前先检查网络，有网络才建立连接
                webSocketConnect();
                break;

            case R.id.disconnect_btn:
                closeWebsocket(false);
                break;

            case R.id.send_msg_btn:
                sendMsg(sendMsgEdit.getText().toString());
                break;
        }
    }

    private void findViews() {
        connectBtn = (Button) findViewById(R.id.connect_btn);
        disconnectBtn = (Button) findViewById(R.id.disconnect_btn);
        messageTv = (TextView) findViewById(R.id.message_tv);
        sendMsgEdit = (EditText) findViewById(R.id.send_msg_edit);
        sendMsgBtn = (Button) findViewById(R.id.send_msg_btn);
    }

    private void initViews() {
        connectBtn.setOnClickListener(this);
        disconnectBtn.setOnClickListener(this);
        sendMsgBtn.setOnClickListener(this);
    }

    protected void getMessage(String msg) {
        messageTv.setText("");
        messageTv.setText(msg);
    }

    @Override
    public void onBackPressed() {
        closeWebsocket(true);
        super.onBackPressed();
    }

    void webSocketConnect() {
        webSocketConnection = new WebSocketConnection();
        try {
            webSocketConnection.connect(websocketHost, new WebSocketHandler() {
                //websocket启动时候的回调
                @Override
                public void onOpen() {
                    Log.e("ysq打印", "open");
                }

                //websocket接收到消息后的回调
                @Override
                public void onTextMessage(String data) {
                    Log.e("ysq打印", "data = " + data);
                    getMessage(data);
                }

                //websocket关闭时候的回调
                @Override
                public void onClose(int code, String reason) {
                    Log.e("ysq打印", "code = " + code + " reason = " + reason);
                    switch (code) {
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3://手动断开连接
                            if (!isExitApp) {
                                webSocketConnect();
                            }
                            break;
                        case 4:
                            break;
                        case 5://网络断开连接
                            closeWebsocket(false);
                            webSocketConnect();
                            break;
                    }
                }
            }, options);
        } catch (WebSocketException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送信息
     */
    public void sendMsg(final String s) {
        Log.e("ysq打印", "sendMsg = " + s);
        if (!TextUtils.isEmpty(s))
            if (webSocketConnection != null) {
                Handler h = new Handler(Looper.getMainLooper());
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        webSocketConnection.sendTextMessage(s);
                    }
                });
            }
    }

    public void closeWebsocket(boolean exitApp) {
        isExitApp = exitApp;
        if (webSocketConnection != null && webSocketConnection.isConnected()) {
            webSocketConnection.disconnect();
            webSocketConnection = null;
        }
    }

}

