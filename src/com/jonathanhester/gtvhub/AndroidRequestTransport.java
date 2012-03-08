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

import com.google.web.bindery.requestfactory.shared.RequestTransport;
import com.google.web.bindery.requestfactory.shared.ServerFailure;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;

/**
 * An implementation of RequestTransport for use between an Android client and a
 * Google AppEngine server.
 */
public class AndroidRequestTransport implements RequestTransport {

    private final URI uri;

    private final String cookie;

    /**
     * Constructs an AndroidRequestTransport instance.
     * 
     * @param uri the URI for the RequestFactory service
     * @param cookie the ACSID or SACSID cookie used for authentication
     */
    public AndroidRequestTransport(URI uri, String cookie) {
        this.uri = uri;
        this.cookie = cookie;
    }

    public void send(String payload, TransportReceiver receiver) {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost();
        post.setHeader("Content-Type", "application/json;charset=UTF-8");
        post.setHeader("Cookie", cookie);

        post.setURI(uri);
        Throwable ex;
        try {
            post.setEntity(new StringEntity(payload, "UTF-8"));
            HttpResponse response = client.execute(post);
            if (200 == response.getStatusLine().getStatusCode()) {
                String contents = readStreamAsString(response.getEntity().getContent());
                receiver.onTransportSuccess(contents);
            } else {
                receiver.onTransportFailure(new ServerFailure(response.getStatusLine()
                        .getReasonPhrase()));
            }
            return;
        } catch (UnsupportedEncodingException e) {
            ex = e;
        } catch (ClientProtocolException e) {
            ex = e;
        } catch (IOException e) {
            ex = e;
        }
        receiver.onTransportFailure(new ServerFailure(ex.getMessage()));
    }

    /**
     * Reads an entire input stream as a String. Closes the input stream.
     */
    private String readStreamAsString(InputStream in) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
            byte[] buffer = new byte[1024];
            int count;
            do {
                count = in.read(buffer);
                if (count > 0) {
                    out.write(buffer, 0, count);
                }
            } while (count >= 0);
            return out.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("The JVM does not support the compiler's default encoding.",
                    e);
        } catch (IOException e) {
            return null;
        } finally {
            try {
                in.close();
            } catch (IOException ignored) {
            }
        }
    }
}
