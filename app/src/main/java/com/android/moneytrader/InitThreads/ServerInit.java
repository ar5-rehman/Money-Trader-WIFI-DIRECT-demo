package com.android.moneytrader.InitThreads;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import android.util.Log;

import com.android.moneytrader.MainActivity;
import com.android.moneytrader.MoneyTradingActivity;

public class ServerInit extends Thread{
	private static final String TAG = "ServerInit";
	private static final int SERVER_PORT = 8888;
	public static ArrayList<InetAddress> clients;
	private ServerSocket serverSocket;

	public ServerInit(){
		clients = new ArrayList<>();
	}

	@Override
	public void run() {
		clients.clear();
		try {
			serverSocket = new ServerSocket(SERVER_PORT);
			//Socket clientSocket = serverSocket.accept();
			/*MainActivity.sendRecieve = new SendRecieve(clientSocket);
			MainActivity.sendRecieve.start();*/
		    while(true) {
		       Socket clientSocket = serverSocket.accept();
		       if(!clients.contains(clientSocket.getInetAddress())){
		    	   clients.add(clientSocket.getInetAddress());
		       }
		       clientSocket.close();
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void interrupt() {
		super.interrupt();
		try {
			serverSocket.close();
			Log.v(TAG, "Server init process interrupted");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
