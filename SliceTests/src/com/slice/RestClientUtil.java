/*
 * Copyright 2018 Jonathan West
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
*/

package com.slice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

/** Utility methods for JAX-RS REST clients */
public class RestClientUtil {

//	private static final KeyStore ks = getKeyStore();
	protected static final HostnameVerifier localHostHnv = new HostnameVerifier() {

		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
//			if (hostname.equalsIgnoreCase("localhost")) {
//				return true;
//			} else {
//				return false;
//			}
		}
	};

	public static Client generateClient() {
		try {
			SSLContext context = SSLContext.getInstance("TLSv1");

			TrustManager[] trustManagerArray = { new NullX509TrustManager() };

			context.init(null, trustManagerArray, null);
			ClientBuilder jcb = ClientBuilder.newBuilder();
			Client client = jcb/*.trustStore(ks)*/.sslContext(context).hostnameVerifier(localHostHnv).build();
			return client;

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (KeyManagementException e) {
			throw new RuntimeException(e);
		}
	}

	public static String readResponse(Response response) {
		InputStream is = (InputStream) response.getEntity();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		StringBuilder sb = new StringBuilder();
		String str;
		try {
			while (null != (str = br.readLine())) {
				sb.append(str);
			}

			return sb.toString();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return null;
		}

	}
	
}

/** Trust everything */
class NullX509TrustManager implements X509TrustManager {
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	}

	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	}

	public X509Certificate[] getAcceptedIssuers() {
		return new X509Certificate[0];
	}

}