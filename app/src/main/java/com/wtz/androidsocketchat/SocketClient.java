package com.wtz.androidsocketchat;

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

    public SocketClient(String site, int port, Handler updateHandler) {
        this.mUpdateHandler = updateHandler;
        mReceivingHandler = new Handler(mUpdateHandler.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                String strMsg = (String) (msg != null ? msg.obj : "");
                updateMessages(strMsg, false);
            }
        };

        try {
            mClient = new Socket(site, port);
            Log.d(TAG, "Client is created! site:" + site + " port:" + port);
            
            if (mClient != null && !mClient.isClosed()) {
                mMessageQueue = new ArrayBlockingQueue<String>(QUEUE_CAPACITY);
                mSendThread = new Thread(new SendingRunnable(mClient, mMessageQueue, TAG));
                mSendThread.start();
                
                mRecThread = new Thread(new ReceivingRunnable(mClient, mReceivingHandler, TAG));
                mRecThread.start();
                
                updateMessages("Socket 连接成功！", true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            updateMessages("Socket 连接异常！", true);
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
        updateMessages(msg, true);
    }

    private synchronized void updateMessages(String msg, boolean local) {
        Log.e(TAG, "Updating message: " + msg);

        if (local) {
            msg = "me: " + msg;
        } else {
            msg = "them: " + msg;
        }

        Bundle messageBundle = new Bundle();
        messageBundle.putString("msg", msg);

        Message message = new Message();
        message.setData(messageBundle);
        mUpdateHandler.sendMessage(message);
    }
}
