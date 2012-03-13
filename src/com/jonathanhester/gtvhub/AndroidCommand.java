package com.jonathanhester.gtvhub;

import com.jonathanhester.gtvhub.shared.AndroidCommandTypes;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class AndroidCommand {
	
	public static void execute(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String sender = (String) extras.get("sender");
            int command = Integer.parseInt((String)extras.get("message"));
            switch(command) {
	            case AndroidCommandTypes.OPEN_LINK:
	            	String link = (String) extras.get("link");
	            	openLink(context, sender, link);
	            }
        }
	}
	
	private static void openLink(Context context, String sender, String link) {
		String uriString = link;
		Uri uri = Uri.parse(uriString);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			context.startActivity(intent);
		} catch (Exception e) {
			Log.d("", "Invalid link");
		}
	}

}
