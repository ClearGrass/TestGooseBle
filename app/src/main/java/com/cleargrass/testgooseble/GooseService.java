package com.cleargrass.testgooseble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;

import java.util.UUID;

/**
 * Created by yueqian on 17/5/22.
 */

public class GooseService {

    private static String GOOSE_SERVICE_UUID = "226C0000-6476-4566-7562-66734470666D";
    private static String CMD_CHARACTERIC_UUID = "226C0024-6476-4566-7562-66734470666D";
    private static String GET_CHARACTERIC_UUID = "226C0012-6476-4566-7562-66734470666D";
    private static String RESPONSE_SERVICE_UUID = "226C0036-6476-4566-7562-66734470666D";


    protected BluetoothGatt mBluetoothGatt;
    protected BluetoothManager mBluetoothManager = null;
    protected BluetoothAdapter mBluetoothAdapter = null;
    GooseServiceCallback mCallback = null;
    Context mContext = null;

    private BluetoothGattService mGooseService;
    private BluetoothGattCharacteristic mCmdChar, mGetChar, mResChar;


    public interface GooseServiceCallback {
        public void searchDevice(BluetoothDevice device, int rssi);
        public void onConnected();
        public void onReceiveResponse(GooseBleProtocol1.BaseCmdProtocol proto);
    }

    public void enableCharacteristicNotification(BluetoothGattCharacteristic characteristic)
    {
        mBluetoothGatt.setCharacteristicNotification(characteristic,true);
        for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
            if (descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            }
            mBluetoothGatt.readDescriptor(descriptor);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }



    BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mBluetoothGatt.discoverServices();


                Log.d("Connected", "onConnected");
            } else {
                Log.d("Connected", "DisConnected: " + status);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("State", "ServicesDiscovered");

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mGooseService = mBluetoothGatt.getService(UUID.fromString(GOOSE_SERVICE_UUID));
                        mCmdChar = mGooseService.getCharacteristic(UUID.fromString(CMD_CHARACTERIC_UUID));
                        mGetChar = mGooseService.getCharacteristic(UUID.fromString(GET_CHARACTERIC_UUID));
                        mResChar = mGooseService.getCharacteristic(UUID.fromString(RESPONSE_SERVICE_UUID));
                        enableCharacteristicNotification(mGetChar);
                        enableCharacteristicNotification(mResChar);
                    }
                });
                thread.start();


            } else {
                Log.d("State", "SvcDiscoveredFailed: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("State", "CharRead");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d("read notification", Utils.bytesToHexString(characteristic.getValue()));
            if(characteristic == mResChar)
            {
                byte[] result = characteristic.getValue();
                try {
                    GooseBleProtocol1.BaseCmdProtocol proto = new GooseBleProtocol1.BaseCmdProtocol(result);
                    if(mCallback != null)
                        mCallback.onReceiveResponse(proto);
                } catch (GooseBleProtocol1.ProtocalException e) {
                    e.printStackTrace();
                }
            }
//            if (characteristic == humitureCharacteristic) {
//                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic, HUMITURE_DATA);
//            } else if (characteristic == tokenCha) {
//                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic, TOKEN_DATA);
//            }
        }


        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            broadcastUpdate(ACTION_DATA_WRITE, characteristic);
//            Log.d("State", "CharWrite");
        }


    };


    public boolean initialize(Context c, GooseServiceCallback callback) {
        mContext = c;
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) c.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e("Error", "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e("Error", "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        mCallback = callback;



        return true;
    }


    public void scan()
    {
        mBluetoothAdapter.enable();
        BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
        scanner.startScan(mLeScanCallback);
    }


    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if(mCallback != null)
                mCallback.searchDevice(result.getDevice(), result.getRssi());
        }

    };


    public void stopBleConnect() {
        mBluetoothGatt.disconnect();
        mBluetoothGatt.close();
        Log.d("state", "gatt.close");
    }

    public void connect(final BluetoothDevice device) {
        if(mContext == null)
            return;
        mBluetoothGatt = device.connectGatt(mContext, false, bluetoothGattCallback);
        Log.d("Connect", "startConnect");
    }


    public void sendToken(){
        byte [] data = GooseBleProtocol1.getTokenSendData();

        mCmdChar.setValue(data);
        mCmdChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        boolean result = mBluetoothGatt.writeCharacteristic(mCmdChar);
        Log.d("sendToken data", Utils.bytesToHexString(data) + "   " + result);
    }

    public void sendHistoryDataCount(){
//        enableCharacteristicNotification(mResChar);
        byte [] data = GooseBleProtocol1.getHistoryDataCountSendData();
        mGooseService = mBluetoothGatt.getService(UUID.fromString(GOOSE_SERVICE_UUID));
        mCmdChar = mGooseService.getCharacteristic(UUID.fromString(CMD_CHARACTERIC_UUID));
        mCmdChar.setValue(data);
        mCmdChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        boolean result = mBluetoothGatt.writeCharacteristic(mCmdChar);
        Log.d("sendToken data", Utils.bytesToHexString(data) + "   " + result);
    }

    public void sendChangeSaveTime(int min){
        mCmdChar.setValue(GooseBleProtocol1.changeDataSaveTimeSendData(min));
        mCmdChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mBluetoothGatt.writeCharacteristic(mCmdChar);
    }

    public byte[] resendPackage(){
        return new byte[0];
    }

    public void sendGetHistoryData(){
        byte [] data = GooseBleProtocol1.getHistoryDataSendData();
        mCmdChar.setValue(data);
        mCmdChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        boolean result = mBluetoothGatt.writeCharacteristic(mCmdChar);
        Log.d("sendToken data", Utils.bytesToHexString(data) + "   " + result);
    }

    public void sendBindToken(){
        mCmdChar.setValue(GooseBleProtocol1.bindTokenSendData());
        mCmdChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mBluetoothGatt.writeCharacteristic(mCmdChar);
    }

    public void sendCheckBindToken() {
        mCmdChar.setValue(GooseBleProtocol1.checkBindTokenSendData());
        mCmdChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mBluetoothGatt.writeCharacteristic(mCmdChar);
    }



}
