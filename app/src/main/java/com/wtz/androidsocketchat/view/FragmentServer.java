package com.wtz.androidsocketchat.view;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.WriterException;
import com.wtz.androidsocketchat.R;
import com.wtz.androidsocketchat.control.IResultListener;
import com.wtz.androidsocketchat.control.ServerHandler;
import com.wtz.androidsocketchat.utils.IPHelper;
import com.wtz.androidsocketchat.utils.QrcodeUtil;

public class FragmentServer extends Fragment {
    private final String TAG = FragmentServer.class.getName();

    private Button btnCreate;
    private Button btnDetroy;
    private EditText etTargetPortInput;

    private TextView tvQrcodeTips;
    private ImageView ivQrcode;

    private Button btnSent;
    private EditText etInput;
    private ScrollView svChatResult;
    private TextView tvChatResult;

    private ServerHandler mServerHandler;
    private Handler mUpdateHandler;
    private IResultListener mCreateListener;

    private final static int QRCODE_SIZE = 100;

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

        btnDetroy = (Button) view.findViewById(R.id.btn_server_destroy);
        btnDetroy.setOnClickListener(mOnclick);

        etTargetPortInput = (EditText) view.findViewById(R.id.et_server_port_input);

        tvQrcodeTips = (TextView) view.findViewById(R.id.tv_qrcode_tips);
        ivQrcode = (ImageView) view.findViewById(R.id.iv_qrcode);

        svChatResult = (ScrollView) view.findViewById(R.id.sv_server_chat_result);
        tvChatResult = (TextView) view.findViewById(R.id.tv_server_chat_result);

        btnSent = (Button) view.findViewById(R.id.btn_server_send);
        btnSent.setOnClickListener(mOnclick);
        etInput = (EditText) view.findViewById(R.id.et_server_chat_input);

        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle data = msg.getData();
                if (data == null) {
                    return;
                }
                String strMsg = "" + data.get("msg");
                String address = "" + data.get("address");
                boolean isLocal = data.getBoolean("isLocal");
                showMsg(isLocal, address, strMsg);
            }
        };

        mCreateListener = new IResultListener() {
            @Override
            public void onResult(boolean success, String error) {
                showMsg(true, null, error);
                if (success) {
                    try {
                        String text = IPHelper.getLocalIPAddress() + ":" + etTargetPortInput.getText().toString();
                        tvQrcodeTips.setText("扫描二维码来绑定吧：");
                        ivQrcode.setImageBitmap(QrcodeUtil.CreateQrCode(text, QRCODE_SIZE, QRCODE_SIZE));
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        mServerHandler = new ServerHandler();

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
        if (mServerHandler != null) {
            mServerHandler.destroy();
        }
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

                case R.id.btn_server_destroy:
                    clickDestroy();
                    break;

                default:
                    break;
            }
        }
    };

    private void clickDestroy() {
        mServerHandler.destroy();
        showMsg(true, null, "已经销毁");
        ivQrcode.setImageResource(R.mipmap.default_qrcode);
        tvQrcodeTips.setText(R.string.create_success_will_create_qrcode);
    }

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

        mServerHandler.create(portString, mCreateListener, mUpdateHandler);
    }

    public void clickSend() {
        Log.d(TAG, "clickSend...");
        String msgString = etInput.getText().toString();
        if (msgString.isEmpty()) {
            toast("请输入内容！");
            return;
        }

        mServerHandler.send(msgString);
        etInput.setText("");
    }

    private void showMsg(boolean isLocal, String address, String message) {
        tvChatResult.append("\n");
        if (isLocal) {
            tvChatResult.append("我：");
        } else {
            tvChatResult.append(address);
        }
        tvChatResult.append("\n");
        tvChatResult.append("    ");
        tvChatResult.append(message + "\n");

        // 将光标移到最后，实现滚动条的自动滚动
        mUpdateHandler.post(new Runnable() {
            @Override
            public void run() {
                svChatResult.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    private void toast(String msg) {
        Toast toast = Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

}
