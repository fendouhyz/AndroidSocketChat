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

public class FragmentClient extends Fragment {
    private final String TAG = FragmentClient.class.getName();

    private Button btnConnect;
    private EditText etTargetIpInput;
    private EditText etTargetPortInput;
    
    private Button btnSent;
    private EditText etInput;
    private TextView tvChatResult;

    private Handler mUpdateHandler;
    private SocketClient mSocketClient;
    
    private boolean isConnecting;
    private Thread mConnectThread;

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
        View view = inflater.inflate(R.layout.client, container, false);
        btnConnect = (Button) view.findViewById(R.id.btn_client_connect);
        btnConnect.setOnClickListener(mOnclick);
        etTargetIpInput = (EditText) view.findViewById(R.id.et_client_target_ip_input);
        etTargetPortInput = (EditText) view.findViewById(R.id.et_client_target_port_input);
        
        tvChatResult = (TextView) view.findViewById(R.id.tv_client_chat_result);
        
        btnSent = (Button) view.findViewById(R.id.btn_client_send);
        btnSent.setOnClickListener(mOnclick);
        etInput = (EditText) view.findViewById(R.id.et_client_chat_input);

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
            case R.id.btn_client_send:
                clickSend();
                break;
                
            case R.id.btn_client_connect:
                clickConnect();
                break;

            default:
                break;
            }
        }
    };

    public void clickConnect() {
        Log.d(TAG, "clickConnect...");
        
        final String ipString = etTargetIpInput.getText().toString();
        final String portString = etTargetPortInput.getText().toString();
        Log.d(TAG, "clickConnect...ip = " + ipString);
        Log.d(TAG, "clickConnect...port = " + portString);
        if (ipString.isEmpty()) {
            toast("请输入IP");
            return;
        }
        if (portString.isEmpty()) {
            toast("请输入端口号");
            return;
        }
        
        if (isConnecting) {
            toast("正在连接中...");
            return;
        }
        
        if (mConnectThread != null && !mConnectThread.isInterrupted()) {
            toast("已启动线程");
            return;
        }
        
        isConnecting = true;
        mConnectThread = new Thread(new Runnable() {
            
            @Override
            public void run() {
                try {
                    int port = Integer.parseInt(portString);
                    mSocketClient = new SocketClient(ipString, port, mUpdateHandler);
                } catch (Exception e) {
                    e.printStackTrace();
                    showStatus("连接异常！");
                }
            }
        });
        mConnectThread.start();
        isConnecting = false;
    }
    
    public void clickSend() {
        Log.d(TAG, "clickSend...");
        String msgString = etInput.getText().toString();
        if (msgString.isEmpty()) {
            toast("请输入内容！");
            return;
        }
        
        try {
            mSocketClient.sendMsg(msgString);
        } catch (Exception e) {
            e.printStackTrace();
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
