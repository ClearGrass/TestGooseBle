package com.cleargrass.testgooseble;

import java.util.ArrayList;

/**
 * Created by yueqian on 17/6/1.
 */

public class GooseBleProtocol {
    public static final byte[] GOOSE_HEADER = {0x43, 0x47};

    public static class GetTokenProtocol extends BaseBleProtocol
    {
        public GetTokenProtocol(byte[] token) {
            super(true, new BaseSingleProtocol(GOOSE_HEADER, 0, 1, 8, token));
        }

        @Override
        public void onReceiveData() {

        }
    }

    public static class GetHistoryDataCountProtocol extends BaseBleProtocol{
        public GetHistoryDataCountProtocol() {
            super(true, new BaseSingleProtocol(GOOSE_HEADER, 1, 1, 0, new byte[0]));
        }

        @Override
        public void onReceiveData() {

        }
    }

    public static class ChangeDataSaveTimeProtocol extends  BaseBleProtocol{
        public ChangeDataSaveTimeProtocol(int min){
            super(true, new BaseSingleProtocol(GOOSE_HEADER, 2, 1, 2, Utils.shortToByteArray((short) min)));
        }

        @Override
        public void onReceiveData() {

        }
    }

    public static class ResendPackageProtocol extends BaseBleProtocol {
        public ResendPackageProtocol(byte[] pack){
            super(false, new BaseSingleProtocol(GOOSE_HEADER, 3, 1, pack.length, pack));
        }
    }

    public static class GetHistoryDataProtocol extends BaseBleProtocol {
        public GetHistoryDataProtocol() {
            super(false, new BaseSingleProtocol(GOOSE_HEADER, 4, 1, 0, new byte[0]));
        }
    }


    public static class BindTokenProtocol extends BaseBleProtocol {
        public BindTokenProtocol(byte[] token) {
            super(true, new BaseSingleProtocol(GOOSE_HEADER, 5, 1, 8, token));
        }

        @Override
        public void onReceiveData(){

        }
    }

    public static class CheckTokenProtocol extends BaseBleProtocol {
        public CheckTokenProtocol(byte[] token) {
            super(true, new BaseSingleProtocol(GOOSE_HEADER, 6, 1, 8, token));
        }

        public void onReceiveData(){

        }
    }

}
