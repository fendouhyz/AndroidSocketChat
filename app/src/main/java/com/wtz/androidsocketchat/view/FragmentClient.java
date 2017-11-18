package com.wtz.androidsocketchat.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.dtr.zxing.activity.CaptureActivity;
import com.dtr.zxing.decode.DecodeThread;
import com.wtz.androidsocketchat.R;
import com.wtz.androidsocketchat.control.ClientHandler;

import static android.app.Activity.RESULT_OK;

public class FragmentClient extends Fragment {
    private final String TAG = FragmentClient.class.getName();

    private Button btnConnect;
    private Button btnDisconnect;
    private Button btnScanQrcode;

    private EditText etTargetIpInput;
    private EditText etTargetPortInput;
    private Button btnSent;

    private EditText etInput;
    private ScrollView svChatResult;
    private TextView tvChatResult;

    private ClientHandler mClientHandler;
    private Handler mUpdateHandler;

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

        btnDisconnect = (Button) view.findViewById(R.id.btn_client_disconnect);
        btnDisconnect.setOnClickListener(mOnclick);

        btnScanQrcode = (Button) view.findViewById(R.id.btn_scan_qrcode);
        btnScanQrcode.setOnClickListener(mOnclick);

        etTargetIpInput = (EditText) view.findViewById(R.id.et_client_target_ip_input);
        etTargetPortInput = (EditText) view.findViewById(R.id.et_client_target_port_input);

        svChatResult = (ScrollView) view.findViewById(R.id.sv_client_chat_result);
        tvChatResult = (TextView) view.findViewById(R.id.tv_client_chat_result);

        btnSent = (Button) view.findViewById(R.id.btn_client_send);
        btnSent.setOnClickListener(mOnclick);
        etInput = (EditText) view.findViewById(R.id.et_client_chat_input);

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

        mClientHandler = new ClientHandler();

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
        if (mClientHandler != null) {
            mClientHandler.disconnect();
        }
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

                case R.id.btn_client_disconnect:
                    clickDisconnect();
                    break;

                case R.id.btn_scan_qrcode:
                    clickScanQrcode();
                    break;

                default:
                    break;
            }
        }
    };

    private void clickScanQrcode() {
        startActivityForResult(new Intent(FragmentClient.this.getContext(), CaptureActivity.class), 0);
    }

    private void clickDisconnect() {
        mClientHandler.disconnect();
        showMsg(true, null, "已经断开");
    }

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
        mClientHandler.connectToServer(ipString, portString, mUpdateHandler);
    }

    public void clickSend() {
        Log.d(TAG, "clickSend...");
        String msgString = etInput.getText().toString();
        if (msgString.isEmpty()) {
            toast("请输入内容！");
            return;
        }
        mClientHandler.send(msgString);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult...requestCode=" + requestCode
                + ", resultCode=" + resultCode + ", data=" + data);
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            String scanResult = (extras != null) ? extras.getString("result") : "";
            String[] results = scanResult.split(":");
            if (results.length == 2) {
                mClientHandler.connectToServer(results[0], results[1], mUpdateHandler);
            } else {
                toast("扫描失败!");
            }
        } else {
            toast("扫描失败!");
        }
    }
}
