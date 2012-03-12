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

import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.ServerFailure;

import com.jonathanhester.gtvhub.client.MyRequestFactory;
import com.jonathanhester.gtvhub.client.MyRequestFactory.RegistrationInfoRequest;
import com.jonathanhester.gtvhub.shared.RegistrationInfoProxy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import android.util.Log;

/**
 * Register/unregister with the third-party App Engine server using
 * RequestFactory.
 */
public class DeviceRegistrar {
    public static final String ACCOUNT_NAME_EXTRA = "AccountName";

    public static final String STATUS_EXTRA = "Status";

    public static final int REGISTERED_STATUS = 1;

    public static final int UNREGISTERED_STATUS = 2;

    public static final int ERROR_STATUS = 3;

    private static final String TAG = "DeviceRegistrar";

    public static void registerOrUnregister(final Context context,
            final String deviceRegistrationId, final boolean register) {
        final SharedPreferences settings = Util.getSharedPreferences(context);
        final String accountName = settings.getString(Util.ACCOUNT_NAME, "Unknown");
        final Intent updateUIIntent = new Intent(Util.UPDATE_UI_INTENT);

        RegistrationInfoRequest request = getRequest(context);
        RegistrationInfoProxy proxy = request.create(RegistrationInfoProxy.class);
        proxy.setDeviceRegistrationId(deviceRegistrationId);

        String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        proxy.setDeviceId(deviceId);

        Request<String> req;
        if (register) {
            req = request.register().using(proxy);
        } else {
            req = request.unregister().using(proxy);
        }

        req.fire(new Receiver<String>() {
            private void clearPreferences(SharedPreferences.Editor editor) {
                editor.remove(Util.ACCOUNT_NAME);
                editor.remove(Util.DEVICE_REGISTRATION_ID);
                editor.remove(Util.DEVICE_CODE);
            }

            @Override
            public void onFailure(ServerFailure failure) {
                Log.w(TAG, "Failure, got :" + failure.getMessage());
                // Clean up application state
                SharedPreferences.Editor editor = settings.edit();
                clearPreferences(editor);
                editor.commit();

                updateUIIntent.putExtra(ACCOUNT_NAME_EXTRA, accountName);
                updateUIIntent.putExtra(STATUS_EXTRA, ERROR_STATUS);
                context.sendBroadcast(updateUIIntent);
            }

            @Override
            public void onSuccess(String response) {
                SharedPreferences settings = Util.getSharedPreferences(context);
                SharedPreferences.Editor editor = settings.edit();
                if (register) {
                    editor.putString(Util.DEVICE_REGISTRATION_ID, deviceRegistrationId);
                    editor.putString(Util.DEVICE_CODE, response);
                    
                } else {
                    clearPreferences(editor);
                }
                editor.commit();
                updateUIIntent.putExtra(ACCOUNT_NAME_EXTRA, accountName);
                updateUIIntent.putExtra(STATUS_EXTRA, register ? REGISTERED_STATUS
                        : UNREGISTERED_STATUS);
                context.sendBroadcast(updateUIIntent);
            }
        });
    }

    private static RegistrationInfoRequest getRequest(Context context) {
        MyRequestFactory requestFactory = Util.getRequestFactory(context, MyRequestFactory.class);
        RegistrationInfoRequest request = requestFactory.registrationInfoRequest();
        return request;
    }
}
