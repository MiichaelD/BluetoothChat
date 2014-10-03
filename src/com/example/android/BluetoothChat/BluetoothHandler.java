package com.example.android.BluetoothChat;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class BluetoothHandler extends Handler{

    // Array adapter for the conversation thread
    private CustomArrayAdapter mConversationArrayAdapter;
    
    // Name of the connected device
    private String mConnectedDeviceName = null;

    private Context context;
    
    
    BluetoothHandler(Context ctx){
    	context = ctx;
    }
    
    private static BluetoothHandler _instance;
    
    public static boolean isSetup(){
    	return _instance != null;
    }
    
    
    public static BluetoothHandler setup(Context ctx){
    	if(isSetup())
    		throw new IllegalStateException(
    				"You should setup the BluetoothHandler just ONCE");
    	
    	return (_instance = new BluetoothHandler(ctx));
    }
    
    public static BluetoothHandler getIns(){
    	if(!isSetup())
    		throw new IllegalStateException(
    				"You should setup the BluetoothChatService before accesing its members");
    	
    	return _instance;
    }
    
    
    public void setConversationAdapter(CustomArrayAdapter adapter){
    	mConversationArrayAdapter = adapter;
    }
    
    
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
        case Constants.MESSAGE_STATE_CHANGE:
            if(Constants.D) Log.i(Constants.TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
            switch (msg.arg1) {
            case BluetoothChatService.STATE_CONNECTED:                    
                mConversationArrayAdapter.clear();
                break;
            case BluetoothChatService.STATE_CONNECTING:
                break;
            case BluetoothChatService.STATE_LISTEN:
            	
            case BluetoothChatService.STATE_NONE:
                break;
            }
            break;
        case Constants.MESSAGE_WRITE:
            byte[] writeBuf = (byte[]) msg.obj;
            // construct a string from the buffer
            String writeMessage = new String(writeBuf);
            mConversationArrayAdapter.add("Me:  " + writeMessage);
            break;
        case Constants.MESSAGE_READ:
            byte[] readBuf = (byte[]) msg.obj;
            // construct a string from the valid bytes in the buffer
            if(msg.arg1>0) {
            	String readMessage = new String(readBuf, 0, msg.arg1);
            	mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
            }
            break;
        case Constants.MESSAGE_DEVICE_NAME:
            // save the connected device's name
            mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
            Toast.makeText(context, "Connected to "
                           + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
            break;
        case Constants.MESSAGE_TOAST:
            Toast.makeText(context, msg.getData().getString(Constants.TOAST),
                           Toast.LENGTH_SHORT).show();
            break;
        }
    }
	
}
