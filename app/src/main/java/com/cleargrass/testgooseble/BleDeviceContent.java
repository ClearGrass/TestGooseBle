package com.cleargrass.testgooseble;

import java.util.ArrayList;

/**
 * Created by yueqian on 17/6/2.
 */

public class BleDeviceContent {
    public static final int DEVICE_TYPE_UNSUPPORT = 0;
    public static final int DEVICE_TYPE_GOOSE = 1;

    public static final BleDeviceType UNSUPPORT_DEVICE = new BleDeviceType(DEVICE_TYPE_UNSUPPORT, "", "", "", new ArrayList<String>());


    private static String GOOSE_SERVICE_UUID = "226C0000-6476-4566-7562-66734470666D";
    private static String CMD_CHARACTERIC_UUID = "226C0024-6476-4566-7562-66734470666D";
    private static String GET_CHARACTERIC_UUID = "226C0012-6476-4566-7562-66734470666D";
    private static String RESPONSE_SERVICE_UUID = "226C0036-6476-4566-7562-66734470666D";

    public static ArrayList<BleDeviceType> mDeviceTypeList = new ArrayList<>();

    public static void initialize()
    {
        mDeviceTypeList.add(UNSUPPORT_DEVICE);
        ArrayList<String> dataUUids = new ArrayList<>();
        dataUUids.add(GET_CHARACTERIC_UUID);
        mDeviceTypeList.add(new BleDeviceType(DEVICE_TYPE_GOOSE, GOOSE_SERVICE_UUID, CMD_CHARACTERIC_UUID, RESPONSE_SERVICE_UUID, dataUUids));

    }

    public static BleDeviceType findDeviceTypeByType(int type)
    {
        for(BleDeviceType device : mDeviceTypeList){
            if(device.mType == type)
                return device;
        }
        return UNSUPPORT_DEVICE;
    }

    public static int findDeviceTypeByServiceUUid(String serviceUUid)
    {
        for(BleDeviceType device : mDeviceTypeList){
            if(device.mServiceUUid.equals(serviceUUid))
                return device.mType;
            if(device.mServiceUUid.toLowerCase().equals(serviceUUid))
                return device.mType;
        }
        return DEVICE_TYPE_UNSUPPORT;
    }



    public static class BleDeviceType {
        public String mServiceUUid;
        public String mCmdUUid;
        public String mResUUid;
        public ArrayList<String> mDataUUids;
        public int mType;

        public BleDeviceType(int type, String serviceUUid, String cmdUUid, String resUUid, ArrayList<String> dataUUIds)
        {
            mServiceUUid = serviceUUid;
            mCmdUUid = cmdUUid;
            mResUUid = resUUid;
            mDataUUids = dataUUIds;
            mType = type;
        }

    }
}
