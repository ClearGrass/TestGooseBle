package com.cleargrass.testgooseble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by yueqian on 17/5/31.
 */

public class BleService {

    protected BluetoothManager mBluetoothManager = null;
    protected BluetoothAdapter mBluetoothAdapter = null;
    protected Context mContext;
    protected BleServiceCallback mCallback;
    protected boolean mFiltered = true;
    protected ArrayList<BleConnection> mConnections = new ArrayList<>();
    protected ArrayList<String> mServiceUUids = new ArrayList<>();




    public void setNeedFilter(boolean filter)
    {
        mFiltered = filter;
    }
    public void setFilterServiceUUids(ArrayList<String> uuids) {
        mServiceUUids = uuids;
    }



    public interface BleServiceCallback{
        public void searchedDevice(ScanResult result, int type);

    }

    public BleService(Context c, BleServiceCallback callback)
    {
        mContext = c;
        mCallback = callback;
        mBluetoothManager = (BluetoothManager) c.getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager == null) {
            Log.e("Error", "Unable to initialize BluetoothManager.");
            return;
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e("Error", "Unable to obtain a BluetoothAdapter.");
            return;
        }
    }

    public void scan() {
        mBluetoothAdapter.enable();
        BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
        scanner.startScan(mLeScanCallback);
    }

    public void stopScan() {

    }



    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            int type = BleDeviceContent.DEVICE_TYPE_UNSUPPORT;
            List<ParcelUuid> uuids = result.getScanRecord().getServiceUuids();
            for(ParcelUuid uuid : uuids){
                type = BleDeviceContent.findDeviceTypeByServiceUUid(uuid.getUuid().toString());
                if(type != BleDeviceContent.DEVICE_TYPE_UNSUPPORT)
                {
                    break;
                }

            }
            if(mFiltered && type == BleDeviceContent.DEVICE_TYPE_UNSUPPORT)
            {
                return;
            }
            if(mCallback != null)
                mCallback.searchedDevice(result, type);

        }

    };


    public void addBleConnection(BluetoothDevice device, int type) {
        BleConnection connection = new BleConnection(mContext, device, type, new BleConnection.BaseConnectionCallback() {
            @Override
            public void onConnected(BleConnection c) {

            }
        });
    }


}
