package com.cleargrass.testgooseble;

import java.util.Arrays;

/**
 * Created by yueqian on 17/5/23.
 */

public class GooseBleProtocol1 {

    public static class ProtocalException extends Exception
    {
        public ProtocalException(String msg)
        {
            super(msg);
        }
    }

    public static byte[] getTokenSendData(){
        byte [] tokendata = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};
        return new BaseCmdProtocol(0, 1, 8, tokendata).getSendData();
    }

    public static byte[] getHistoryDataCountSendData(){
        return new BaseCmdProtocol(0x01, 0x01, 0, new byte[0]).getSendData();
    }

    public static byte[] changeDataSaveTimeSendData(int min){
        return new BaseCmdProtocol(0x02, 0x01, 0x02, Utils.shortToByteArray((short) min)).getSendData();
    }

    public static byte[] resendPackage(){
        return new byte[0];
    }

    public static byte[] getHistoryDataSendData(){
        return new BaseCmdProtocol(0x04, 0x01, 0x00, new byte[0]).getSendData();
    }

    public static byte[] bindTokenSendData(){
        byte [] tokendata = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};
        return new BaseCmdProtocol(0x05, 0x01, 0x08, tokendata).getSendData();
    }

    public static byte[] checkBindTokenSendData() {
        byte [] tokendata = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};
        return new BaseCmdProtocol(0x06, 0x01, 0x08, tokendata).getSendData();
    }


    public static class BaseCmdProtocol{
        public byte [] mHeader = {0x43, 0x47};
        public byte [] mCmd = new byte[1];
        public byte [] mFront = new byte[1];
        public byte [] mDatalength = new byte[1];
        public byte [] mData = new byte[0];


        public BaseCmdProtocol(int cmd, int front, int length, byte[] data)
        {
            mCmd[0] = (byte)cmd;
            mFront[0] = (byte)front;
            mDatalength[0] = (byte)length;
            mData = Arrays.copyOf(data, data.length);
        }

        public BaseCmdProtocol(byte[] data) throws ProtocalException {
            parseData(data);
        }


        public byte[] getSendData() {
            return Utils.concatByte(mHeader, mCmd, mFront, mDatalength, mData);
        }

        public String getDataString() {
            return Utils.bytesToHexString(getSendData());
        }

        public void parseData(byte [] data) throws ProtocalException {
            if(data.length < 5)
            {
                throw new ProtocalException("data length is " + data.length + " less than 5");
            }
            if(!Arrays.equals(mHeader, Arrays.copyOfRange(data, 0, 2)))
            {
                throw new ProtocalException("protocol header error");
            }
            mCmd = Arrays.copyOfRange(data, 2, 3);
            mFront = Arrays.copyOfRange(data, 3, 4);
            if(mFront[0] != 0x02 && mFront[0] != 0x01)
            {
                throw new ProtocalException("message front is " + mFront[0] + " not 1 or 2");
            }
            mDatalength = Arrays.copyOfRange(data, 4, 5);
            if(data.length != 5 + mDatalength[0])
            {
                throw new ProtocalException("data length is not suit data, the data length = " + 5 + mDatalength[5] + " and the real length = " + data.length);
            }
            mData = Arrays.copyOfRange(data,5, data.length);
        }
    }




}
