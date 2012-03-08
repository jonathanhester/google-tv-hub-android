/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.jonathanhester.gtvhub;

import com.google.android.c2dm.C2DMessaging;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences.Editor;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Account selections activity - handles device registration and unregistration.
 */
public class AccountsActivity extends Activity {

    /**
     * Tag for logging.
     */
    private static final String TAG = "AccountsActivity";

    /**
     * The selected position in the ListView of accounts.
     */
    private int mAccountSelectedPosition = 0;

    /**
     * True if we are waiting for App Engine authorization.
     */
    private boolean mPendingAuth = false;

    /**
     * The current context.
     */
    private Context mContext = this;

    /**
     * Begins the activity.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = Util.getSharedPreferences(mContext);
        String deviceRegistrationID = prefs.getString(Util.DEVICE_REGISTRATION_ID, null);
        if (deviceRegistrationID == null) {
            // Show the 'connect' screen if we are not connected
            setScreenContent(R.layout.connect);
        } else {
            // Show the 'disconnect' screen if we are connected
            setScreenContent(R.layout.disconnect);
        }
    }

    /**
     * Resumes the activity.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (mPendingAuth) {
            mPendingAuth = false;
            String regId = C2DMessaging.getRegistrationId(mContext);
            if (regId != null && !"".equals(regId)) {
                DeviceRegistrar.registerOrUnregister(mContext, regId, true);
            } else {
                C2DMessaging.register(mContext, Setup.SENDER_ID);
            }
        }
    }

    // Manage UI Screens

    /**
     * Sets up the 'connect' screen content.
     */
    private void setConnectScreenContent() {
        final Button connectButton = (Button) findViewById(R.id.connect);
        connectButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Set "connecting" status
                SharedPreferences prefs = Util.getSharedPreferences(mContext);
                prefs.edit().putString(Util.CONNECTION_STATUS, Util.CONNECTING).commit();
                register();
                finish();
            }
        });
    }

    /**
     * Sets up the 'disconnected' screen.
     */
    private void setDisconnectScreenContent() {
        final SharedPreferences prefs = Util.getSharedPreferences(mContext);
        String accountName = prefs.getString(Util.ACCOUNT_NAME, "Unknown");

        // Format the disconnect message with the currently connected account
        // name
        TextView disconnectText = (TextView) findViewById(R.id.disconnect_text);
        String message = getResources().getString(R.string.disconnect_text);
        String formatted = String.format(message, accountName);
        disconnectText.setText(formatted);

        final Button disconnectButton = (Button) findViewById(R.id.disconnect);
        disconnectButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Unregister
                C2DMessaging.unregister(mContext);
                finish();
            }
        });
    }

    /**
     * Sets the screen content based on the screen id.
     */
    private void setScreenContent(int screenId) {
        setContentView(screenId);
        switch (screenId) {
            case R.layout.disconnect:
                setDisconnectScreenContent();
                break;
            case R.layout.connect:
                setConnectScreenContent();
                break;
        }
    }

    // Register and Unregister

    /**
     * Registers for C2DM messaging with the given account name.
     * 
     * @param accountName a String containing a Google account name
     */
    private void register() {
    	final String accountName = getAccountName();
        // Store the account name in shared preferences
        final SharedPreferences prefs = Util.getSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Util.ACCOUNT_NAME, accountName);
        editor.remove(Util.DEVICE_REGISTRATION_ID);
        editor.commit();

        C2DMessaging.register(mContext, Setup.SENDER_ID);
    }
    
    private String getAccountName() {
    	return "hester";
    }

}
