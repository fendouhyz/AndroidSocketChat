package com.wtz.androidsocketchat.model;

import java.io.*;
import java.net.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SocketClient {
    private final String TAG = SocketClient.class.getSimpleName();

    private Socket mClient;

    private int QUEUE_CAPACITY = 10;
    private BlockingQueue<String> mMessageQueue;
    private Thread mSendThread;
    private Thread mRecThread;

    private Handler mUpdateHandler;
    private Handler mReceivingHandler;

    public SocketClient(String site, int port, Handler updateHandler) throws UnknownHostException, IOException {
        this.mUpdateHandler = updateHandler;
        mReceivingHandler = new Handler(mUpdateHandler.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Bundle data = msg.getData();
                if (data == null) {
                    return;
                }
                String strMsg = "" + data.get("msg");
                String address = "" + data.get("address");
                boolean isLocal = data.getBoolean("isLocal");
                updateMessages(isLocal, address, strMsg);
            }
        };

        mClient = new Socket(site, port);
        Log.d(TAG, "Client is created! site:" + site + " port:" + port);

        if (mClient != null && !mClient.isClosed()) {
            mMessageQueue = new ArrayBlockingQueue<String>(QUEUE_CAPACITY);
            mSendThread = new Thread(new SendingRunnable(mClient, mMessageQueue, TAG));
            mSendThread.start();

            mRecThread = new Thread(new ReceivingRunnable(mClient, mReceivingHandler, TAG));
            mRecThread.start();

            updateMessages(true, null, "Socket 连接成功！");
        }
    }

    public void closeSocket() {
        if (mClient != null) {
            try {
                mClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMsg(String msg) {
        if (mClient == null || mMessageQueue == null) {
            return;
        }

        mMessageQueue.add(msg);
        updateMessages(true, null, msg);
    }

    private synchronized void updateMessages(boolean isLocal, String address, String msg) {
        Log.e(TAG, "Updating message...isLocal: " + isLocal + ", from: " + address
                + ", msg: " + msg);
        if (mUpdateHandler != null) {
            Bundle messageBundle = new Bundle();
            messageBundle.putString("msg", msg);
            messageBundle.putString("address", address);
            messageBundle.putBoolean("isLocal", isLocal);

            Message message = new Message();
            message.setData(messageBundle);
            mUpdateHandler.sendMessage(message);
        }
    }
}
