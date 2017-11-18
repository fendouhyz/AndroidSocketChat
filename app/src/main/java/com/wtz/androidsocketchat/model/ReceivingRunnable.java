package com.wtz.androidsocketchat.model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ReceivingRunnable implements Runnable {
    private String TAG = ReceivingRunnable.class.getSimpleName();

    private Socket mSocket;
    private Handler mReceivingHandler;

    public ReceivingRunnable(Socket socket, Handler h, String tag) {
        this.mReceivingHandler = h;
        this.mSocket = socket;
        TAG = tag + "-" + TAG;
    }

    @Override
    public void run() {
        Log.d(TAG, "mSocket = " + mSocket + ", mReceivingHandler = " + mReceivingHandler);
        if (mSocket == null || mReceivingHandler == null) {
            return;
        }

        BufferedReader input;
        try {
            input = new BufferedReader(new InputStreamReader(mSocket.getInputStream(), "UTF-8"));
            while (!Thread.currentThread().isInterrupted()) {

                String messageStr = null;
                messageStr = input.readLine();
                if (messageStr != null) {
                    Log.d(TAG, "Read from the stream: " + messageStr);
                    Bundle messageBundle = new Bundle();
                    messageBundle.putString("msg", messageStr);
                    messageBundle.putString("address", mSocket.getInetAddress().getHostAddress());
                    messageBundle.putBoolean("isLocal", false);
                    Message msg = mReceivingHandler.obtainMessage();
                    msg.setData(messageBundle);
                    mReceivingHandler.sendMessage(msg);
                } else {
                    Log.d(TAG, "The nulls! The nulls!");
                    break;
                }
            }
            input.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
