package com.cleargrass.testgooseble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    BleService mBleService;
    TextView mTestText;
    Button mButton;
    ScrollView mScrollView;
    Handler mHandler = new MyHandler();


    class MyHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            String str = (String) msg.obj;
            mTestText.append(str + "\n");
            ScrollView scrollView = mScrollView;
            TextView view = mTestText;

            if (scrollView == null || view == null) {
                return;
            }
            int offset = view.getMeasuredHeight()
                    - scrollView.getMeasuredHeight();
            if (offset < 0) {
                offset = 0;
            }
            scrollView.scrollTo(0, offset);

        }
    }

    BleService.BleServiceCallback mServiceCallback = new BleService.BleServiceCallback() {
        @Override
        public void searchedDevice(ScanResult result, int type) {
            BluetoothDevice device = result.getDevice();
            Message msg = new Message();
            msg.obj = "found device!";
            mHandler.sendMessage(msg);
            mBleService.addBleConnection(device, type);
        }


    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BleDeviceContent.initialize();
        mBleService = new BleService(getApplicationContext(), mServiceCallback);
        mTestText = (TextView) findViewById(R.id.testtext);
        mButton = (Button) findViewById(R.id.btn);
        mScrollView = (ScrollView) findViewById(R.id.scroll_view);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                mGooseService.sendHistoryDataCount();
            }
        });
        Handler scanHandler = new Handler(){
            @Override
            public void handleMessage(Message msg)
            {
                mBleService.scan();
            }
        };
        scanHandler.sendEmptyMessageDelayed(0, 1000);

    }
}
