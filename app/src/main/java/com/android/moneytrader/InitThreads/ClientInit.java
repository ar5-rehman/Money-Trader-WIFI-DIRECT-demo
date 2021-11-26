package com.android.moneytrader.InitThreads;

import com.android.moneytrader.MainActivity;
import com.android.moneytrader.MoneyTradingActivity;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientInit extends Thread{
	private static final int SERVER_PORT = 8888;
	private InetAddress mServerAddr;

	public ClientInit(InetAddress serverAddr){
		mServerAddr = serverAddr;
	}

	@Override
	public void run() {
		Socket socket = new Socket();
		try {
			socket.bind(null);
			socket.connect(new InetSocketAddress(mServerAddr, SERVER_PORT),1000);
			/*MainActivity.sendRecieve = new SendRecieve(socket);
			MainActivity.sendRecieve.start();*/
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void interrupt() {
		super.interrupt();

	}
}
