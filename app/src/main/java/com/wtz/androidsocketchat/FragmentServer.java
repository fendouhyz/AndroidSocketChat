package com.wtz.androidsocketchat;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentServer extends Fragment {
    private final String TAG = FragmentServer.class.getName();

    private Button btnCreate;
    private EditText etTargetPortInput;
    
    private Button btnSent;
    private EditText etInput;
    private TextView tvChatResult;

    private Handler mUpdateHandler;
    private SocketServer mSocketServer;
    
    private boolean isCreating;
    private Thread mCreateThread;

    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach");
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.server, container, false);
        btnCreate = (Button) view.findViewById(R.id.btn_server_create);
        btnCreate.setOnClickListener(mOnclick);
        etTargetPortInput = (EditText) view.findViewById(R.id.et_server_port_input);
        
        tvChatResult = (TextView) view.findViewById(R.id.tv_server_chat_result);
        
        btnSent = (Button) view.findViewById(R.id.btn_server_send);
        btnSent.setOnClickListener(mOnclick);
        etInput = (EditText) view.findViewById(R.id.et_server_chat_input);

        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String chatLine = msg.getData().getString("msg");
                addChatLine(chatLine);
            }
        };
        
        return view;
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    private View.OnClickListener mOnclick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.btn_server_send:
                clickSend();
                break;
                
            case R.id.btn_server_create:
                clickCreate();
                break;

            default:
                break;
            }
        }
    };

    public void clickCreate() {
        Log.d(TAG, "clickCreate...");
        final String portString = etTargetPortInput.getText().toString();
        Log.d(TAG, "clickCreate...port = " + portString);
        if (portString.isEmpty()) {
            toast("请输入端口号");
            return;
        }
        try {
            int port = Integer.parseInt(portString);
            if (port <= 1024) {
                toast("请输入大于1024的端口号");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (isCreating) {
            toast("正在创建中...");
            return;
        }
        
        try {
            //TODO 为何线程还在时，isAlive是false？但isInterrupted是false!
            Log.d(TAG, "mCreateThread.isAlive() = " + mCreateThread.isAlive());
            Log.d(TAG, "mCreateThread.isInterrupted() = " + mCreateThread.isInterrupted());
        } catch (Exception e) {
            // TODO: handle exception
        }
        if (mCreateThread != null && !mCreateThread.isInterrupted()) {
            toast("已启动线程");
            return;
        }
        
        isCreating = true;
        mCreateThread = new Thread(new Runnable() {
            
            @Override
            public void run() {
                try {
                    int port = Integer.parseInt(portString);
                    mSocketServer = new SocketServer(port, mUpdateHandler);
                } catch (Exception e) {
                    e.printStackTrace();
                    showStatus("创建异常！");
                }
            }
        });
        mCreateThread.start();
        isCreating = false;
    }
    
    public void clickSend() {
        Log.d(TAG, "clickSend...");
        String msgString = etInput.getText().toString();
        if (msgString.isEmpty()) {
            toast("请输入内容！");
            return;
        }
        
        try {
            mSocketServer.sendMsg(msgString);
        } catch (Exception e) {
            e.printStackTrace();
            showStatus("发送异常！");
        }
        etInput.setText("");
    }

    public void addChatLine(String line) {
        tvChatResult.append("\n" + line);
    }

    private void toast(String msg) {
        Toast toast = Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
    
    private void showStatus(String msg) {
        Bundle messageBundle = new Bundle();
        messageBundle.putString("msg", msg);

        Message message = new Message();
        message.setData(messageBundle);
        mUpdateHandler.sendMessage(message);
    }
}
