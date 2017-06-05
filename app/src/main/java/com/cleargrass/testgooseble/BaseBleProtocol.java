package com.cleargrass.testgooseble;

import java.util.Arrays;

/**
 * Created by yueqian on 17/6/1.
 */

public class BaseBleProtocol {

    BaseSingleProtocol mSendProtocol;
    BaseSingleProtocol mReceiveProtocol;
    boolean mNeedResponse = false;
    boolean mIsSend = true;

    public BaseBleProtocol(boolean needResponse, BaseSingleProtocol sendProto)
    {
        mNeedResponse = needResponse;
        mIsSend = true;
        mSendProtocol = sendProto;
    }

    public boolean isNeedResponse(){
        return mNeedResponse;
    }

    public byte[] getSendData()
    {
        return mSendProtocol.getSendData();
    }

    public void parseReceiveData(byte[] receiveData) throws ProtocolException {
        mReceiveProtocol = new BaseSingleProtocol(receiveData);
    }

    public void onReceiveData(){

    }

    public boolean isSameType(byte[] receiveData) throws ProtocolException {
        BaseSingleProtocol proto = new BaseSingleProtocol(receiveData);
        if(mSendProtocol.getCmd() ==  proto.getCmd())
            return true;
        else
            return false;
    }


    public static class ProtocolException extends Exception
    {
        public ProtocolException(String msg)
        {
            super(msg);
        }
    }

    public static class BaseSingleProtocol {
        public byte [] mHeader = {0x43, 0x47};
        public byte [] mCmd = new byte[1];
        public byte [] mFront = new byte[1];
        public byte [] mDatalength = new byte[1];
        public byte [] mData = new byte[0];


        public BaseSingleProtocol(byte[] header, int cmd, int front, int length, byte[] data)
        {
            mHeader = header;
            mCmd[0] = (byte)cmd;
            mFront[0] = (byte)front;
            mDatalength[0] = (byte)length;
            mData = Arrays.copyOf(data, data.length);
        }

        public BaseSingleProtocol(byte[] data) throws ProtocolException {
            parseData(data);
        }


        public byte[] getSendData() {
            return Utils.concatByte(mHeader, mCmd, mFront, mDatalength, mData);
        }

        public String getDataString() {
            return Utils.bytesToHexString(getSendData());
        }

        public byte getCmd(){
            return mCmd[0];
        }

        public void parseData(byte [] data) throws ProtocolException {
            if(data.length < 5)
            {
                throw new ProtocolException("data length is " + data.length + " less than 5");
            }
            if(!Arrays.equals(mHeader, Arrays.copyOfRange(data, 0, 2)))
            {
                throw new ProtocolException("protocol header error");
            }
            mCmd = Arrays.copyOfRange(data, 2, 3);
            mFront = Arrays.copyOfRange(data, 3, 4);
            if(mFront[0] != 0x02 && mFront[0] != 0x01)
            {
                throw new ProtocolException("message front is " + mFront[0] + " not 1 or 2");
            }
            mDatalength = Arrays.copyOfRange(data, 4, 5);
            if(data.length != 5 + mDatalength[0])
            {
                throw new ProtocolException("data length is not suit data, the data length = " + 5 + mDatalength[5] + " and the real length = " + data.length);
            }
            mData = Arrays.copyOfRange(data,5, data.length);
        }
    }
}


