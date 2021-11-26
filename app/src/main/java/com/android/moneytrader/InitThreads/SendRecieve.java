package com.android.moneytrader.InitThreads;

import static com.android.moneytrader.MoneyTradingActivity.MESSAGE_READ;
import static com.android.moneytrader.MoneyTradingActivity.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SendRecieve extends Thread {

    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    public SendRecieve(Socket skt){
        this.socket = skt;

        try{

            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

        }catch(Exception e){

        }
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        int bytes;

        while (socket!=null){

            try{
                bytes  =inputStream.read(buffer);
                if(bytes>0){
                    handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                }
            }catch (Exception e){

            }

        }
    }

    public void write(byte[] bytes){
        try {
            outputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
