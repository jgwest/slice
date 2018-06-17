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

package com.slice.models;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.slice.datatypes.ObjectFactory;
import com.slice.datatypes.QueryDTO;
import com.slice.datatypes.QueryStatusDTO;
import com.slice.datatypes.ResourceCreateResponseDTO;
import com.slice.datatypes.SearchResultListDTO;

/** Convert various DTO objects to JSON or XML */
public class ModelUtils {
	
	public static String debugMarshallQueryDTO(QueryDTO param, boolean toJSON) {
		ObjectFactory of = new ObjectFactory();

		StringWriter writer = new StringWriter();
		JAXBContext context;
		try {
			context = JAXBContext.newInstance(QueryDTO.class);
			Marshaller m = context.createMarshaller();
//			
//			JSONMarshaller marshaller = JSONJAXBContext.getJSONMarshaller( m );
			
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			if(toJSON) {
		        m.setProperty(Marshaller.JAXB_ENCODING, "application/json");
			}
			m.marshal(of.createQuery(param), writer);

			return writer.toString();
			
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
		return null;
	}


	public static String debugMarshallQueryStatusDTO(QueryStatusDTO param, boolean toJSON) {
		ObjectFactory of = new ObjectFactory();

		StringWriter writer = new StringWriter();
		JAXBContext context;
		try {
			context = JAXBContext.newInstance(QueryStatusDTO.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			if(toJSON) {
		        m.setProperty(Marshaller.JAXB_ENCODING, "application/json");
			}
			m.marshal(of.createQueryStatus(param), writer);

			return writer.toString();
			
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public static String debugMarshallSearchResultListDTO(SearchResultListDTO param, boolean toJSON) {
		ObjectFactory of = new ObjectFactory();

		StringWriter writer = new StringWriter();
		JAXBContext context;
		try {
			context = JAXBContext.newInstance(SearchResultListDTO.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			if(toJSON) {
		        m.setProperty(Marshaller.JAXB_ENCODING, "application/json");
			}
			m.marshal(of.createSearchResultList(param), writer);

			return writer.toString();
			
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public static String debugMarshallResourceCreateResponseDTO(ResourceCreateResponseDTO param, boolean toJSON) {
		ObjectFactory of = new ObjectFactory();

		StringWriter writer = new StringWriter();
		JAXBContext context;
		try {
			context = JAXBContext.newInstance(ResourceCreateResponseDTO.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			if(toJSON) {
		        m.setProperty(Marshaller.JAXB_ENCODING, "application/json");
			}
			m.marshal(of.createResourceCreateResponse(param), writer);

			return writer.toString();
			
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/*
	public static String debugMarshallResultEntryDTO(ResultEntryDTO param, boolean toJSON) {
		ObjectFactory of = new ObjectFactory();

		StringWriter writer = new StringWriter();
		JAXBContext context;
		try {
			context = JAXBContext.newInstance(ResultEntryDTO.class);
			Marshaller m = context.createMarshaller();

			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			if(toJSON) {
		        m.setProperty(Marshaller.JAXB_ENCODING, "application/json");
			}
			m.marshal(of.createResultEntryDTO(), writer);

			return writer.toString();
			
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
		return null;
	}

*/

}
