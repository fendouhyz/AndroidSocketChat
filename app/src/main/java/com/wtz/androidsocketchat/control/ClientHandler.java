package com.wtz.androidsocketchat.control;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.wtz.androidsocketchat.model.SocketClient;

public class ClientHandler extends BaseHandler {

    private SocketClient mSocketClient;
    private boolean isConnecting;
    private Thread mConnectThread;
    private Handler mUpdateHandler;

    public boolean isConnecting() {
        return isConnecting;
    }

    public void connectToServer(final String ipString, final String portString,
            final Handler updateHandler) {
        if (isConnecting) {
            System.out.println("正在连接中...");
            if (updateHandler != null) {
                sendMsg(updateHandler, true, null, "正在连接中...");
            }
            return;
        }
        
        if (mSocketClient != null) {
            System.out.println("已经连接成功");
            if (updateHandler != null) {
                sendMsg(updateHandler, true, null, "已经连接成功");
            }
            return;
        }

        isConnecting = true;
        mUpdateHandler = updateHandler;
        mConnectThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    int port = Integer.parseInt(portString);
                    if (mSocketClient != null) {
                        mSocketClient.closeSocket();
                    }
                    mSocketClient = new SocketClient(ipString, port, updateHandler);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (mSocketClient != null) {
                        mSocketClient.closeSocket();
                        mSocketClient = null;
                    }
                    if (updateHandler != null) {
                        sendMsg(updateHandler, true, null, "连接错误！");
                    }
                } finally {
                    isConnecting = false;
                }
            }
        });
        mConnectThread.start();
    }

    @Override
    public void send(String msg) {
        try {
            mSocketClient.sendMsg(msg);
        } catch (Exception e) {
            e.printStackTrace();
            if (mUpdateHandler != null) {
                sendMsg(mUpdateHandler, true, null, "发送失败！");
            }
        }
    }

    public void disconnect() {
        if (mSocketClient != null) {
            mSocketClient.closeSocket();
            mSocketClient = null;
        }
    }

    private void sendMsg(Handler h, boolean isLocal, String address, String messageStr) {
        Bundle messageBundle = new Bundle();
        messageBundle.putString("msg", messageStr);
        messageBundle.putString("address", address);
        messageBundle.putBoolean("isLocal", isLocal);
        Message msg = h.obtainMessage();
        msg.setData(messageBundle);
        h.sendMessage(msg);
    }
}
