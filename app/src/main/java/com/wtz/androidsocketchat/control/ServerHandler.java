package com.wtz.androidsocketchat.control;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.wtz.androidsocketchat.model.SocketServer;

public class ServerHandler extends BaseHandler {

    private SocketServer mSocketServer;
    private boolean isCreating;
    private Thread mCreateThread;
    private Handler mUpdateHandler;

    public boolean isCreating() {
        return isCreating;
    }

    public void create(final String portString, final IResultListener l, final Handler updateHandler) {
        if (isCreating) {
            System.out.println("正在创建服务端...");
            sendCallback(updateHandler, l, false, "正在创建服务端...");
            return;
        }

        if (mSocketServer != null) {
            System.out.println("已经存在服务端");
            sendCallback(updateHandler, l, false, "已经存在服务端");
            return;
        }

        isCreating = true;
        mUpdateHandler = updateHandler;
        mCreateThread = new Thread(new Runnable() {

            @Override
            public void run() {
                boolean ret = false;
                try {
                    int port = Integer.parseInt(portString);
                    mSocketServer = new SocketServer(port, mUpdateHandler);
                    ret = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    if (mSocketServer != null) {
                        mSocketServer.closeSocket();
                        mSocketServer = null;
                    }
                    ret = false;
                } finally {
                    isCreating = false;
                    if (ret) {
                        sendCallback(updateHandler, l, true, "创建成功！");
                    } else {
                        sendCallback(updateHandler, l, false, "创建错误！");
                    }
                }
            }
        });
        mCreateThread.start();
    }

    @Override
    public void send(String msg) {
        try {
            mSocketServer.sendMsg(msg);
        } catch (Exception e) {
            e.printStackTrace();
            if (mUpdateHandler != null) {
                sendMsg(mUpdateHandler, true, null, "发送失败！");
            }
        }
    }
    
    public void destroy() {
        if (mSocketServer != null) {
            mSocketServer.closeSocket();
            mSocketServer = null;
        }
    }

    private void sendCallback(Handler h, final IResultListener l, final boolean success, final String error) {
        if (l != null && h != null) {
            h.post(new Runnable() {
                @Override
                public void run() {
                    l.onResult(success, error);
                }
            });
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
