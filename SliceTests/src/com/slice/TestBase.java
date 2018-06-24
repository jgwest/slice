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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.slice.datatypes.FileContentsDTO;
import com.slice.datatypes.QueryDTO;
import com.slice.datatypes.QueryStatusDTO;
import com.slice.datatypes.ResourceCreateResponseDTO;
import com.slice.datatypes.ResultEntryDTO;
import com.slice.datatypes.SearchResultListDTO;
import com.slice.filenameindexerservice.FileSearchResult;
import com.slice.filenameindexerservice.FileSearchResultList;
import com.slice.models.ModelConstants.QDRStatus;
import com.slice.models.ModelUtils;

/** Abstract parent class for all tests; takes the product id from the configuration xml as input to the constructor.*/
public abstract class TestBase {

	private final static String HOST = "http://localhost:9080";
	private final static String SECURE_HOST = "https://localhost:9443";

	private LoginInfo _info;

	private String _productId;
	
	public TestBase(String productId) {
		this._productId = productId;
		this._info = new LoginInfo("test-user", "test-password", SECURE_HOST, productId);		
	}
	
	
	@BeforeClass
	public static void initialSetup() {
		String libertyDir = System.getProperty("user.dir")+"/target/liberty/wlp";
		SITestServerListenerUtil.addServerMessagesListenerThread(libertyDir, "defaultServer");
	}
	
	@AfterClass
	public static void after() {
//		try {
//			Thread.sleep(10 * 1000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
	}

	@Test
	public void testQuery() {
		QueryInterfaceClient client = new QueryInterfaceClient(SECURE_HOST, _info);

		QueryDTO newQuery = client.getObjectFactory().createQueryDTO();

		newQuery.setCaseSensitive(false);
		newQuery.setWholeWordOnly(false);
		newQuery.getSearchTerm().add("jgw");

		newQuery.getSearchFileTypes().add("0");

		ResourceCreateResponseDTO rcr = client.invokeNewQuery(newQuery, _productId);

		assertNotNull(rcr);

		String uri = rcr.getUri();
		long queryId = Long.parseLong(uri.substring(uri.lastIndexOf("/") + 1).trim());

		assertTrue(queryId >= 0);

		QueryStatusDTO status = QueryInterfaceClient.waitForQueryComplete(queryId, client, _productId);
		assertNotNull(status);
		
		
		assertTrue("status is: "+status.getStatus()+ " query is: "+ModelUtils.debugMarshallQueryStatusDTO(status, false), status.getStatus().equalsIgnoreCase(QDRStatus.COMPLETE_SUCCESS.name()));

		assertTrue("num results is: " +status.getNumResults()+ " query is: "+ModelUtils.debugMarshallQueryStatusDTO(status, false), status.getNumResults() >= 3);

		
		SearchResultListDTO dto = client.invokeQueryResult(queryId, 0, Math.min(199, status.getNumResults() - 1),
				_productId);
	
		List<ResultEntryDTO> results = dto.getSearchResultListEntry();
	

		assertNotNull(results);

		assertTrue(results.size() > 0);

		for (ResultEntryDTO e : results) {

			assertNotEmpty(e.getPath());
			assertNotEmpty(e.getFileUrl());
			assertNotEmpty(e.getContent());
		}

	}

	@Test
	public void testFilenameIndexer() {
		FilenameIndexerClient client = new FilenameIndexerClient(SECURE_HOST, _info);

		FileSearchResultList list = client.invokeGet("Product", _productId);
		assertNotNull(list);

		assertNotNull(list.getResultList());

		assertTrue(list.getResultList().size() > 0);

		for (FileSearchResult e : list.getResultList()) {

			assertNotEmpty(e.getPath());
			assertNotEmpty(e.getUrl());
		}

	}

	@Test
	public void testFilenameContents() {
		FileContentsClient client = new FileContentsClient(SECURE_HOST, _info);
		
		FileContentsDTO fc = client.invokeGetFileContents("/SliceShared/src/com/slice/indexer/dice/KeyCompression.java", null, _productId);
		
		assertNotNull(fc);
		assertTrue(fc.getErrorText() == null || fc.getErrorText().trim().length() == 0);
		
		assertNotEmpty(fc.getContents());

		assertNotEmpty(fc.getFilename());
		assertNotEmpty(fc.getPath());
		
	}
	
	
	@Test
	public void testJsp() throws ClientProtocolException, IOException, KeyManagementException, NoSuchAlgorithmException,
			KeyStoreException, CertificateException {

		HttpGet httpget;
		
		httpget = new HttpGet(SECURE_HOST+"/SliceRS/resources/"+_productId);
		CloseableHttpResponse response = createClient().execute(httpget);
		assertTrue(response.getStatusLine().getStatusCode() == 200);
		
		httpget = new HttpGet(SECURE_HOST+"/SliceFilenameIndexerService/index.jsp?resourceId="+_productId);
		response = createClient().execute(httpget);
		assertTrue(response.getStatusLine().getStatusCode() == 200);

		httpget = new HttpGet(SECURE_HOST+"/SliceWeb/auth/?resourceId="+_productId);
		response = createClient().execute(httpget);
		assertTrue(response.getStatusLine().getStatusCode() == 200);

		
		httpget = new HttpGet(HOST+"/SliceRS/resources/"+_productId);
		response = createClient().execute(httpget);
		assertTrue(response.getStatusLine().getStatusCode() == 302);

		httpget = new HttpGet(HOST+"/SliceFilenameIndexerService/index.jsp?resourceId="+_productId);
		response = createClient().execute(httpget);
		assertTrue(response.getStatusLine().getStatusCode() == 302);
		
		httpget = new HttpGet(HOST+"/SliceWeb/auth/?resourceId="+_productId);
		response = createClient().execute(httpget);
		assertTrue(response.getStatusLine().getStatusCode() == 302);
		
	}

	
	private static CloseableHttpClient createClient() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		// Trust own CA and all self-signed certs
		SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(new TrustSelfSignedStrategy()).build();

		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" }, null,
				RestClientUtil.localHostHnv);
		CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).setRedirectStrategy(new StrictRedirectStrategy()).build();
		
		return httpclient;
	}
	
	private static void assertNotEmpty(String str) {
		assertNotNull(str);
		assertTrue(str.trim().length() > 5);
	}

	/** Prevent automatic redirect */
	private static class StrictRedirectStrategy implements RedirectStrategy {

		@Override
		public HttpUriRequest getRedirect(HttpRequest arg0, HttpResponse arg1, HttpContext arg2)
				throws ProtocolException {
			return null;
		}

		@Override
		public boolean isRedirected(HttpRequest arg0, HttpResponse arg1, HttpContext arg2) throws ProtocolException {
			return false;
		}
		
	}
	
	static String getTestId() {
		return null;
	}
}
