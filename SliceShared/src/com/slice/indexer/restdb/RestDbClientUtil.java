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

package com.slice.indexer.restdb;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.naming.InitialContext;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;

import com.slice.indexer.constants.IConfigConstants;
import com.slice.indexer.shared.util.SearchIndexerUtil;

/** Utility method for JAX-RS client invocations. */
public class RestDbClientUtil {
	
	private static final KeyStore ks = getKeyStore(); 
	private static final HostnameVerifier localHostHnv = new HostnameVerifier() {
		
		@Override
		public boolean verify(String hostname, SSLSession session) {
			if (hostname.equalsIgnoreCase("localhost")) {
				return true;
			} else {
				return false;
			}
		}
	}; 

	
	private static Client debugOnlyGenerateClient() {
		try {
			SSLContext context = SSLContext.getInstance("TLSv1");

			TrustManager[] trustManagerArray = { new NullX509TrustManager() };

			context.init(null, trustManagerArray, null);
			ClientBuilder jcb = ClientBuilder.newBuilder();
			Client client = jcb.sslContext(context).hostnameVerifier(localHostHnv).build();
			return client;

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (KeyManagementException e) {
			throw new RuntimeException(e);		
		}
	}

	
	public static Client generateClient() {
		
		if(SearchIndexerUtil.isDebugTestModeEnabled()) {
			return debugOnlyGenerateClient();
		}
		
		ClientBuilder jcb = ClientBuilder.newBuilder();
		
		jcb = jcb.hostnameVerifier(localHostHnv);
		
		if(ks != null) {
			jcb = jcb.trustStore(ks);
						
		}
	
		Client client = jcb.build();
		
		return client;
	}

	private static KeyStore getKeyStore() {
		try {
			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

			InitialContext ctx = new InitialContext();
			
			java.io.FileInputStream fis = null;
			try {
				fis = new java.io.FileInputStream((String)ctx.lookup("slice/keystore_location"));
				ks.load(fis, ((String)ctx.lookup("slice/keystore_password")).toCharArray());
			} finally {
				if (fis != null) {
					fis.close();
				}
			}
			return ks;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}

	public static void addPskHeader(Invocation.Builder builder, IConfigConstants product) {
		if(product.getDatabasePresharedKey() != null) {
			builder.header("db-preshared-key", product.getDatabasePresharedKey());
		}
	}



	/** Trust manager that will trust anything. */
	static class NullX509TrustManager implements X509TrustManager {
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}

	}
}

