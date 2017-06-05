package com.cleargrass.testgooseble;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by yueqian on 17/6/1.
 */

public class BleConnection {
    BleDeviceContent.BleDeviceType mDeviceType;

    BluetoothDevice mDevice;
    protected BluetoothGatt mGatt;
    protected BluetoothGattService mService;
    protected BluetoothGattCharacteristic mCmdChar, mResChar;

    protected ArrayList<BluetoothGattCharacteristic> mDataChars = new ArrayList<>();
    protected Map<String, DataCharacteristicCallback> mDataCallbacks = new HashMap<>();

    protected boolean mConnected = false;
    protected boolean mKeepAlive = true;


    protected BaseConnectionCallback mCallback = null;
    protected List<BaseBleProtocol> mProtoList = new LinkedList<>();
    protected Context mContext;


    public interface BaseConnectionCallback {
        public void onConnected(BleConnection c);
    }

    public interface DataCharacteristicCallback {
        public void receiveData(byte[] data);
    }


    public void setKeepAlive(boolean keepAlive)
    {
        mKeepAlive = keepAlive;
    }

    public BluetoothDevice getDevice() {
        return mDevice;
    }

    public BleDeviceContent.BleDeviceType getDeviceType() {
        return mDeviceType;
    }



    public BleConnection(Context c, BluetoothDevice device, int type, BaseConnectionCallback callback)
    {
        mContext = c;
        mDeviceType = BleDeviceContent.findDeviceTypeByType(type);
        mCallback = callback;
        mDevice = device;
    }

    public void connect(final BluetoothDevice device) {
        mGatt = device.connectGatt(mContext, false, mBluetoothGattCallback);
        Log.d("Connect", "startConnect");
    }



    public void AddDataCharacteristic(String dataUUid, DataCharacteristicCallback callback)
    {
        BluetoothGattCharacteristic characteristic = mService.getCharacteristic(UUID.fromString(dataUUid));
        enableCharacteristicNotification(characteristic);
        mDataCallbacks.put(dataUUid, callback);
    }

    public void sendProtocol(BaseBleProtocol proto)
    {
        if(proto.isNeedResponse())
            mProtoList.add(proto);
        mCmdChar.setValue(proto.getSendData());
        mCmdChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mGatt.writeCharacteristic(mCmdChar);
    }




    BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mGatt.discoverServices();
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
                        mService = mGatt.getService(UUID.fromString(mDeviceType.mServiceUUid));
                        mCmdChar = mService.getCharacteristic(UUID.fromString(mDeviceType.mCmdUUid));
                        mResChar = mService.getCharacteristic(UUID.fromString(mDeviceType.mResUUid));
                        enableCharacteristicNotification(mResChar);
                        mConnected = true;
                        mCallback.onConnected(BleConnection.this);

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
                    for(BaseBleProtocol proto : mProtoList)
                    {
                        if (proto.isSameType(result))
                        {
                            proto.parseReceiveData(result);
                            proto.onReceiveData();
                            mProtoList.remove(proto);
                            break;
                        }
                    }
                } catch (BaseBleProtocol.ProtocolException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                DataCharacteristicCallback callback = mDataCallbacks.get(characteristic.getUuid().toString());
                if(callback != null)
                {
                    callback.receiveData(characteristic.getValue());
                }
            }
        }


        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            broadcastUpdate(ACTION_DATA_WRITE, characteristic);
//            Log.d("State", "CharWrite");
        }


    };



    protected void enableCharacteristicNotification(BluetoothGattCharacteristic characteristic)
    {
        mGatt.setCharacteristicNotification(characteristic,true);
        for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
            if (descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            }
            mGatt.readDescriptor(descriptor);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mGatt.writeDescriptor(descriptor);
        }
    }





}
