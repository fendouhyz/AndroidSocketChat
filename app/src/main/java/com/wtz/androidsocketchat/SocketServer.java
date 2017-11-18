package com.wtz.androidsocketchat;

import java.io.*;
import java.net.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SocketServer {
    private final String TAG = SocketServer.class.getSimpleName();

    private ServerSocket mSever;
    private Thread mServerStarter;
    
    private int QUEUE_CAPACITY = 10;
    private BlockingQueue<String> mMessageQueue;
    
    private Handler mUpdateHandler;
    private Handler mReceivingHandler;

    public SocketServer(int port, Handler updateHandler) {
        this.mUpdateHandler = updateHandler;
        mReceivingHandler = new Handler(mUpdateHandler.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                String strMsg = (String) (msg != null ? msg.obj : "");
                updateMessages(strMsg, false);
            }
        };
        
        try {
            mSever = new ServerSocket(port);
            mMessageQueue = new ArrayBlockingQueue<String>(QUEUE_CAPACITY);
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void start() {
        boolean hadStarted = hadStarted();
        Log.d(TAG, "start...mSever = " + mSever + ", hadStarted = " + hadStarted);
        if (mSever != null && !mSever.isClosed() && !hadStarted) {
            //TODO------------------区分处理上边的判断！！！
            mServerStarter = new Thread(new Runnable() {

                @Override
                public void run() {
                    Log.d(TAG, "start...run");
                    while (true) {
                        beginListen();
                    }
                }

            });
            mServerStarter.start();
        }
    }

    private boolean hadStarted() {
        if (mServerStarter == null) {
            return false;
        }
        if (!mServerStarter.isAlive()) {
            return false;
        }
        if (mServerStarter.isInterrupted()) {
            return false;
        }

        return true;
    }

    private void beginListen() {
        try {
            Log.d(TAG, "beginListen...mSever.accept");
            final Socket clientSocket = mSever.accept();
            Log.d(TAG, "accept new client ok! socket = " + clientSocket);

            new Thread(new Runnable() {
                public void run() {
                    try {
                        new Thread(new SendingRunnable(clientSocket, mMessageQueue, TAG)).start();
                        new Thread(new ReceivingRunnable(clientSocket, mReceivingHandler, TAG))
                                .start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void closeSocket() {
        if (mSever != null) {
            try {
                mSever.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSever = null;
        }
    }
    
    public void sendMsg(String msg) {
        if (mMessageQueue == null) {
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
