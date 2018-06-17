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

import java.io.InputStream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.slice.searchindexdb.NewQueryIdListDTO;
import com.slice.searchindexdb.QueryIDResultDTO;
import com.slice.searchindexdb.QueryIdStatusDTO;
import com.slice.indexer.constants.IConfigConstants;
import com.slice.indexer.shared.util.SearchIndexerUtil;

/** Query the database service to create a new query, delete an existing query, or get results, all using the JAX-RS Client. */
public class QueryServiceClient {

	private final String resourceUrl;

	public QueryServiceClient(String resourceUrl) {
		this.resourceUrl = resourceUrl;
	}

	public Response deleteQuery(IConfigConstants product, long queryId) {
		Client client = RestDbClientUtil.generateClient();
		
		WebTarget target = client.target(resourceUrl + "/resources").path("{ProductId}").path("Query")
				.path("{Id}");
		target = target.resolveTemplate("ProductId", product.getProductId()).resolveTemplate("Id", queryId);

		Invocation.Builder builder = target.request();
		RestDbClientUtil.addPskHeader(builder, product);
		Response response = builder.delete();
		return response;
	}

	public QueryIDResultDTO invokeGetResults(IConfigConstants product, long queryId, String keyIdList /** optional */) {
		Client client = RestDbClientUtil.generateClient();
		
		WebTarget target = client.target(resourceUrl + "/resources").path("{ProductId}").path("Query").path("{Id}")
				.path("Result");
		target = target.queryParam("ids", keyIdList);

		target = target.resolveTemplate("ProductId", product.getProductId()).resolveTemplate("Id", queryId);

		Invocation.Builder builder = target.request("application/xml");
		RestDbClientUtil.addPskHeader(builder, product);
		Response response = builder.get();
		
		InputStream is = (InputStream)response.getEntity();
		
		return (QueryIDResultDTO)parseXmlWithJaxb(is);
	}

	public QueryIdStatusDTO getQueryStatus(IConfigConstants product, long queryId) {
		Client client = RestDbClientUtil.generateClient();
		
		WebTarget target = client.target(resourceUrl + "/resources").path("{ProductId}").path("Query").path("{Id}")
				.path("Status");
		target = target.resolveTemplate("ProductId", product.getProductId()).resolveTemplate("Id", queryId);

		Invocation.Builder builder = target.request("application/xml");
		RestDbClientUtil.addPskHeader(builder, product);
		Response response = builder.get();
		
		InputStream is = (InputStream)response.getEntity();
		
		return (QueryIdStatusDTO)parseXmlWithJaxb(is);
	}


	
	private static Object parseXmlWithJaxb(InputStream entityStream) {
		

		InputStream xsdIs = null;
		JAXBContext jaxbContext;
		try {
			xsdIs = SearchIndexerUtil.class.getResourceAsStream("/META-INF/SearchIndexDB.xsd");
			
			jaxbContext = JAXBContext.newInstance("com.slice.searchindexdb");
//			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Unmarshaller um = jaxbContext.createUnmarshaller();
//			Schema schema = factory.newSchema(new StreamSource(xsdIs));
			
//			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
//			jaxbUnmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE,
//					"application/json");
			
//			jaxbUnmarshaller.setSchema(schema);
			
			Object o = um.unmarshal( entityStream );
			
			
			return o;
			
//			return jaxbUnmarshaller.unmarshal(entityStream);

		} catch (JAXBException e) {
			e.printStackTrace();
			return null;
		} finally {
			SearchIndexerUtil.quietClose(xsdIs);
		}
	}
	
	public long createNewQuery(IConfigConstants product, NewQueryIdListDTO query) {
		Client client = RestDbClientUtil.generateClient();
		
		WebTarget target = client.target(resourceUrl + "/resources").path("{ProductId}").path("Query");
		target = target.resolveTemplate("ProductId", product.getProductId());
		
		String url = resourceUrl+"resources/"+product.getProductId()+"/Query";
		System.out.println("cnq: "+url);
		target = client.target(url);

		Invocation.Builder builder = target.request("application/json");
		RestDbClientUtil.addPskHeader(builder, product);
		Response response = builder.post(Entity.entity(query, "application/xml"));

		return response.readEntity(Long.class);
	}

}
