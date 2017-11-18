package com.wtz.androidsocketchat.model;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
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
    private ArrayList<BlockingQueue<String>> mMsgQueueList;
    
    private Handler mUpdateHandler;
    private Handler mReceivingHandler;

    public SocketServer(int port, Handler updateHandler) throws IOException {
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

        mSever = new ServerSocket(port);
        mMsgQueueList = new ArrayList<BlockingQueue<String>>();
        
        start();
    }

    private void start() {
        boolean hadStarted = hadStarted();
        Log.d(TAG, "start...mSever = " + mSever + ", hadStarted = " + hadStarted);
        if (mSever != null && !mSever.isClosed() && !hadStarted) {
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
        if (mSever == null) {
            return;
        }
        try {
            Log.d(TAG, "beginListen...mSever.accept");
            final Socket clientSocket = mSever.accept();
            Log.d(TAG, "accept new client ok! socket = " + clientSocket);

            new Thread(new Runnable() {
                public void run() {
                    try {
                        BlockingQueue<String> messageQueue = new ArrayBlockingQueue<String>(QUEUE_CAPACITY);
                        mMsgQueueList.add(messageQueue);
                        new Thread(new SendingRunnable(clientSocket, messageQueue, TAG)).start();
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
        if (mMsgQueueList == null || mMsgQueueList.size() == 0) {
            return;
        }

        for (BlockingQueue<String> queue : mMsgQueueList) {
            queue.add(msg);
        }
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
