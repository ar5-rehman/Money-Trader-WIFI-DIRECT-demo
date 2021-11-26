package com.android.moneytrader.Entities;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class Message implements Serializable{
	private static final String TAG = "Message";
	private String mText;
	private String userName;
	private InetAddress senderAddress;
	private ArrayList<String> user_record;

	public String getmText() { return mText; }
	public String getUserName() { return userName; }
	public InetAddress getSenderAddress() { return senderAddress; }
	public void setSenderAddress(InetAddress senderAddress) { this.senderAddress = senderAddress; }
	public void setUser_record(String user_name) {
		this.user_record.add(user_name);
	}

	public Message(String text, InetAddress sender, String name){
		mText = text;
		senderAddress = sender;
		userName = name;
		user_record = new ArrayList<>();
	}
}
