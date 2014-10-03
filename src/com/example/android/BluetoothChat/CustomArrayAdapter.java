package com.example.android.BluetoothChat;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/** Custom ArrayAdapter to display views more complicated than just
 * a TextView; In this case, 2 TextViews. You know, just to practice.
 * 
 * More info:
 * https://github.com/thecodepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView */
public class CustomArrayAdapter extends ArrayAdapter<String>{

	int res_id;
	Object[] data;
	
	public CustomArrayAdapter(Context context, int resource) {
		super(context, resource);
		res_id = resource;
	}
	
	public CustomArrayAdapter(Context context, int resource, String[] arr) {
		super(context, resource, arr);
		res_id = resource;
		data = arr;
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
       // Get the data item for this position
		String str = getItem(position);   
		
		ViewHolder viewHolder;
		
       // Check if an existing view is being reused, otherwise inflate the view
       if (convertView == null) {
          convertView = LayoutInflater.from(getContext()).inflate(res_id, parent, false);
          
          viewHolder = new ViewHolder();
          viewHolder.message = (TextView) convertView;
          convertView.setTag(viewHolder);
       } else {
    	   //if it was already reused 
           viewHolder = (ViewHolder) convertView.getTag();
       }
       // Populate the data into the template view using the data object
       viewHolder.message.setText(str);
       
       if (str.startsWith("Me:  ")){
    	   viewHolder.message.setTextColor(0xFF0099CC);
    	   if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
    		   viewHolder.message.setGravity(Gravity.RIGHT);
       }
       else{
    	   viewHolder.message.setTextColor(0xFF15B72E);
    	   if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
    		   viewHolder.message.setGravity(Gravity.LEFT);
       }
       
       // Return the completed view to render on screen
       return convertView;
   }
	
	/** This class is used to improve performance, we should modify the custom adapter
	 * by applying the ViewHolder pattern which speeds up the population of the
	 * ListView considerably by caching view lookups for smoother, faster loading:*/
	private static class ViewHolder{
		TextView message;
	}

}
