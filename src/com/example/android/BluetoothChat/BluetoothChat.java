/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.BluetoothChat;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothChat extends Activity {

    private static final String MSG_KEY = "msg_key";
    
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;

    // Array adapter for the conversation thread
    private CustomArrayAdapter mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;


 // The Handler that gets information back from the BluetoothChatService
    private static BluetoothHandler mHandler;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Constants.D) Log.e(Constants.TAG, "+++ ON CREATE +++");
        setContentView(R.layout.main);
        
        
        if(BluetoothHandler.isSetup())
        	mHandler = BluetoothHandler.getIns();
        else
        	mHandler = BluetoothHandler.setup(this);
        
        
        
        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new CustomArrayAdapter(this, R.layout.message);
        mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton = (Button) findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                TextView view = (TextView) findViewById(R.id.edit_text_out);
                String message = view.getText().toString();
                sendMessage(message);
            }
        });
        
        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");

        mHandler.setConversationAdapter(mConversationArrayAdapter);
        

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        /*
        // this works but it is Deprecated
		Object obj = getLastNonConfigurationInstance();

	    if(null != obj){//if there is saved adapter - restore it
	    	mChatService = (BluetoothChatService)obj;
	    }
	    */
        
    }
    
    /*
    @Override
	public Object onRetainNonConfigurationInstance() {
	    return mChatService;
	}*/

    @Override
    public void onStart() {
        super.onStart();
        if(Constants.D) Log.e(Constants.TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
        	if(!BluetoothChatService.isSetup())
        		setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(Constants.D) Log.e(Constants.TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (BluetoothChatService.isSetup()) {
        	BluetoothChatService.getIns().onResume();
        }
    }
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.e(Constants.TAG, "ORIENTATION_LANDSCAPE");
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.e(Constants.TAG, "ORIENTATION_PORTRAIT");
        }
    }
    private void setupChat() {
        Log.d(Constants.TAG, "setupChat()");

        // Initialize the BluetoothChatService to perform bluetooth connections
        BluetoothChatService.setup(this, mHandler);
        //mChatService = new BluetoothChatService(this, mHandler);
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(Constants.D) Log.e(Constants.TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(Constants.D) Log.e(Constants.TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(Constants.D) Log.e(Constants.TAG, "--- ON DESTROY ---");
    }

    /* (non-Javadoc)
	 * @see android.app.Activity#onBackPressed()*/
	@Override
	public void onBackPressed() {
		super.onBackPressed();
        if(Constants.D) Log.e(Constants.TAG, "--- ON BACK KEY PRESSED ---");
		stopChatService();
	}
	
	private void stopChatService(){
        //TODO fix rotation bug
		System.out.println("Closing chat Service");
        if (BluetoothChatService.isSetup())
        	BluetoothChatService.getIns().stop();
	}

	private void ensureDiscoverable() {
        if(Constants.D) Log.d(Constants.TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (BluetoothChatService.getIns().getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            BluetoothChatService.getIns().write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener =
        new TextView.OnEditorActionListener() {
    	@Override
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
        	try{
	            if(actionId == EditorInfo.IME_ACTION_SEND) {
	            	/*
	            	InputMethodManager imm = (InputMethodManager)getSystemService(
	            		      Context.INPUT_METHOD_SERVICE);
	            		imm.hideSoftInputFromWindow(mOutEditText.getWindowToken(), 0);
	            		*/
	                String message = view.getText().toString();
	                sendMessage(message);
	            }
	            if(Constants.D) Log.i(Constants.TAG, "END onEditorAction");
	            return true;
        	}catch(Exception e){
        		if(Constants.D) Log.e(Constants.TAG, "END onEditorAction");
        		e.printStackTrace();
        	}
    		return false;
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(Constants.D) Log.d(Constants.TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                BluetoothChatService.getIns().connect(device);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupChat();
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(Constants.TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.scan:
            // Launch the DeviceListActivity to see devices and do scan
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return true;
        case R.id.discoverable:
            // Ensure this device is discoverable by others
            ensureDiscoverable();
            return true;
        }
        return false;
    }

    
    /** Save data from ListView when rotating the device 
	 * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle) */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		 if(Constants.D) Log.i(Constants.TAG,"onRestoreInstanceState restoring  values");
		// Initialize the array adapter for the conversation thread
        if (savedInstanceState != null) {
        	String[] values =  savedInstanceState.getStringArray(MSG_KEY);
        	if(mConversationArrayAdapter != null){
	            for (String msg : values) 
	            	mConversationArrayAdapter.add(msg);
	        }
        	else{
        		mConversationArrayAdapter = new CustomArrayAdapter(this, R.layout.message, values);
        	}
        }
	}
	
	
	/** Save data from ListView when rotating the device */
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
        if(mConversationArrayAdapter != null){
	        int operations = mConversationArrayAdapter.getCount();
	        if(Constants.D) Log.i(Constants.TAG,"onSaveInstanceState saving "+operations+" values");
	        
	        String[] values =  new String[operations];
	        for(int i =0 ; i < operations;i++)
	        	values[i] = mConversationArrayAdapter.getItem(i);
	        
	        savedState.putStringArray(MSG_KEY, values);
        }
    }
}