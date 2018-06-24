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

package com.slice.indexer.shared.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.slice.datatypes.fileconfiguration.ConfigDTO;
import com.slice.datatypes.fileconfiguration.ConfigEntriesDTO;
import com.slice.datatypes.fileconfiguration.ProductDTO;
import com.slice.indexer.constants.IAllowRule;
import com.slice.indexer.constants.IAllowRule.AllowRuleType;
import com.slice.indexer.constants.IConfigConstants;
import com.slice.indexer.constants.ISearchFileType;
import com.slice.indexer.shared.ConfigConstantsProductDTOWrapper;
import com.slice.indexer.shared.Product;

/** Various utility methods used and shared by all components in the project. */
public class SearchIndexerUtil {
	
	
	public static List<String> sanitizeStringList(List<String> list, boolean allowPathChars, boolean replaceWithString) {
		if(list == null) { return null; }
		
		List<String> result = new ArrayList<String>(list.size());
		for(int x = 0; x < list.size(); x++) {
			result.add(sanitizeString(list.get(x), allowPathChars, replaceWithString));
		}
		
		return result;
		
	}
    
    public static String sanitizeString(String inputString, boolean allowPathChars, boolean replaceWithString) {
    	if(inputString == null) { return null; }
    	
    	StringBuilder sb = new StringBuilder();
    	
    	for(int x = 0; x < inputString.length(); x++) {
    		char c = inputString.charAt(x);
    		if(Character.isLetterOrDigit(c) || c == '.' || c == '*' || c == '_' || c == ',' || c == ' ' || c == '-' || c == '[' || c == ']' 
    				|| (  allowPathChars && ( c == '/' || c == '\\') )) {
    			sb.append(c);
    		} else {
    			
    			if(replaceWithString) {
    				sb.append(" ");
    			}
    			
    		}
    	}
    	
    	String result = sb.toString();

    	return sanitizePathTraversalChars(result);

    	
    }

    public static String sanitizePathTraversalChars(String result) {

		// Remove any attempts at directory traversal
		while(result.contains("..")) {
			result = result.replace("..", "");
		}
		
		// Remove leading slashes
		result = result.trim();
		while(result.startsWith("/") || result.startsWith("\\")) {
			result = result.substring(1);
			result = result.trim();
		}
    	
    	return result;
    	
    }

    
    public static String replaceAllNonJavaIdentifierExceptPeriodsWithSpaces(String inputString) {
    	if(inputString == null) { return null; }
    	
    	StringBuilder sb = new StringBuilder();
    	
    	for(int x = 0; x < inputString.length(); x++) {
    		char c = inputString.charAt(x);
    		if(Character.isJavaIdentifierPart(c) || c == '.') {
    			sb.append(c);
    		} else {    			
    			sb.append(" ");    			
    		}
    	}
    	
    	String result = sb.toString();

    	return result;

    	
    }

    
	
	public static boolean containsWholeWordOnly(String source, String searchTerm, JavaSrcSearchQuery query) {
		
		boolean match = false;
		
		for(int x =0 ; x < source.length() && source.length()-x-searchTerm.length() >= 0; x++) {
					
			String preCurr = source.substring(0, x);
			String curr = source.substring(x, x+searchTerm.length());
			String postcurr = source.substring(x + searchTerm.length());
		
			if(query.isCaseSensitive()) {
				if(curr.equals(searchTerm)) {
					match = arePreAndPostWhitespaceOrEmpty(preCurr, postcurr);
				}
			} else {
			
				if(curr.equalsIgnoreCase(searchTerm)) {
					match = arePreAndPostWhitespaceOrEmpty(preCurr, postcurr);
				}
			}
			
			if(match) {
				break;
			}
		}
		
		return match;
	}
	
	/** pre and post should either be:
	 *  - empty (size 0)
	 *  OR
	 *     - for pre: the last character should be whitespace
	 *     AND
	 *     - for post: the first character should be whitespace */
	public static boolean arePreAndPostWhitespaceOrEmpty(String preCurr, String postCurr) {
		
		boolean match = false;
		
		boolean preCurrMatch = false;
		boolean postCurrMatch = false;
		
		if(postCurr.length() > 0) {
			char ch =  postCurr.charAt(0);
			
			if(	!Character.isJavaIdentifierPart(ch) || ch == '.'	) {
				postCurrMatch = true;
			}
			
		} else {
			postCurrMatch = true;
		}
		
		if(preCurr.length() > 0 ) {
			char ch = preCurr.charAt(preCurr.length()-1);
			if(!Character.isJavaIdentifierPart(ch) || ch == '.') {
				preCurrMatch = true;
			}
			
		} else {
			preCurrMatch = true;
		}
		
		match = preCurrMatch && postCurrMatch;
		
		return match;
		
	}
	

	
	public static boolean allowPathAccess(IConfigConstants constants, File path) {
		
		if(constants.getAllowRules() == null) {
			return false;
		}
		
		String str = path.getPath();
		
		boolean allow = false;
		
		for(IAllowRule rule : constants.getAllowRules()) {
			if(rule.getType() == AllowRuleType.ALLOW && str.contains(rule.getValue())) {
				allow = true;
			}
		}
		
		return allow;
	}

	
	public static boolean isJarSearchSupportedFile(String path, String[] jarSearchSupportedExtensions) {
		
		path = path.toLowerCase();
		
		boolean matchedExtension = false;
		for(String s : jarSearchSupportedExtensions) {
			
			if(path.endsWith(s.toLowerCase())) {
				matchedExtension = true;
				break;
			}
		}
		
		return matchedExtension;
		
	}

	public static boolean isSearchSupportedFile(String path, List<ISearchFileType> supportedExtensions) {
		
		path = path.toLowerCase();
		
		boolean matchedExtension = false;
		for(ISearchFileType s : supportedExtensions) {
			for(String extension : s.getSupportedExtensionsList()) {
				if(path.endsWith(extension.toLowerCase())) {
					matchedExtension = true;
					break;
				}
			}
		}
		
		return matchedExtension;
		
	}
	
	public static boolean isWindows() {
		boolean isWindows = (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0);
		return isWindows;
	}
	
	/** Utility method: Convert exception/throwable stack trace to String. */
	public static String convertStackTraceToString(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		String stackTrace = sw.toString(); // stack trace as a string
		
		return stackTrace;
		
	}
	
	
	public static String[] INVALID_TERMS = new String[] {
		"ArrayList", "boolean", "catch", "char", "class", "continue", "Double",
			"else", "Exception", "extends", "false", "File", "final", "Float",
			"float", "for", "if", "implements", "import", "InputStream", "int",
			"Integer", "interface", "Iterator", "LinkedList", "long", "Map",
			"OutputStream", "Override", "package", "private", "protected",
			"public", "Queue", "return", "static", "String", "substring",
			"true", "try", "void", "while"

	};	
	
	public static boolean isValidTerm(String str) {
		
		// Rules
		if(str.trim().length() <= 2) {
			return false;
		}

		str = str.toLowerCase();
		
		for(String invalidTerm : INVALID_TERMS) {
			invalidTerm = invalidTerm.toLowerCase();
			
			if(str.equals(invalidTerm)) {
				return false;
			}
		}
		
		return true;
	}	
	
	
	private static int[] findNextEnvVarInString(String str) {
		
		
		int envVarStart = str.indexOf("${");
		if(envVarStart == -1) {
			return null;
		}
		
		int envVarEnd = str.indexOf("}",envVarStart+2);
		
		if(envVarEnd != -1) {
			
			return new int[] { envVarStart, envVarEnd};
			
		}
		
		return null;
	}
	
	public static String replaceEnvVar(String str) {
		if(str == null) {
			return str;
		}
		
		int[] nextEnvVar;
		
		boolean envVarReplaced = false;
		
//		System.out.println("replaceEnvVar called: "+str);
		
		
		while(null != (nextEnvVar = findNextEnvVarInString(str)) ) {
			
			String envVar = str.substring(nextEnvVar[0]+2, nextEnvVar[1]);
			
			
			String envVarContents = System.getenv(envVar) ;
			if(envVarContents == null || envVarContents.trim().isEmpty()) {
				// If the env var isn't set, see if it is mentioned in the server.xml
				try {
					envVarContents = (String) new InitialContext().lookup("slice/"+envVar);
				} catch (NamingException e) {
					// Ignore.
				}
			}
			if(envVarContents == null || envVarContents.trim().isEmpty()) {
				envVarContents = System.getProperty(envVar);
			}
			
			if(envVarContents == null || envVarContents.trim().length() == 0) {
				System.err.println("Unable to locate enviroment variable: "+envVar);
				throw new RuntimeException("Unable to locate environment variable: "+envVar);
			}
		
			str = str.substring(0, nextEnvVar[0]) + envVarContents+str.substring(nextEnvVar[1]+1);
			envVarReplaced = true;
		}
		
		if(envVarReplaced) {
//			System.out.println("New env-var replaced string: "+str);
		}
		
		return str;
		
	}
	
	@SuppressWarnings("unchecked")
	public static List<Product> readProductConfigFile(InputStream fileConfigXml, InputStream fileConfigXsd) {
		
		JAXBContext jaxbContext;
		try {
			jaxbContext = JAXBContext.newInstance("com.slice.datatypes.fileconfiguration");
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			
			Schema schema = factory.newSchema(new StreamSource(fileConfigXsd));
			
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			jaxbUnmarshaller.setSchema(schema);
			
			JAXBElement<ConfigDTO> ts = ((JAXBElement<ConfigDTO>) jaxbUnmarshaller.unmarshal(fileConfigXml));
			
			List<Element> configElements = new ArrayList<>();
			ConfigEntriesDTO ced = ts.getValue().getConfigEntries(); 
			if(ced != null && ced.getAny() != null) {
				for(Object o : ced.getAny()) {
					if(o instanceof Element) {
						Element e = (Element)o;
						configElements.add(e);
					}
				}
			}
			List<Product> result = new ArrayList<Product>();
			
			for (ProductDTO pDTO : ts.getValue().getProduct()) {
				Product product = new Product(new ConfigConstantsProductDTOWrapper(pDTO), pDTO.getId(), configElements);
				// product.setProductId();
				// product.setConstants();
				// product.setConfigEntries();
				result.add(product);
			}
			
			return result;
			
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}

	
	public static boolean doesFileExtensionMatchSearchFileType(File file, ISearchFileType type) {
		
		String path = file.getPath().toLowerCase();
		
		for(String str : type.getSupportedExtensionsList()) {
			if(path.endsWith(str)) {
				return true;
			}
			
		}
		
		return false;
		
	}
	
	
	public static void quietClose(InputStream is) {
		if(is == null) { return; }
		
		try {
			is.close();
		} catch (IOException e) {
			// Ignore.
		}
	}
	
	public static String readFromStream(InputStream is) {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		
		String str;
		try {
			while(null != (str = br.readLine())) {
				
				sb.append(str+"\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return sb.toString();
		
	}
	
	public static String getConfigXmlPath() {
		String result = null;
		
		Object jndiConstant;
		try {
			jndiConstant = new InitialContext().lookup("slice/config_xml_path");
			result = (String) jndiConstant;
		} catch (NamingException e2) {
			result = null;
		}
		
		if(result == null && isDebugTestModeEnabled()) {
			File tmpDirPropFile = new File(System.getProperty("java.io.tmpdir")+"/Slice-Test.properties");
			
			// Only use the file if it was created in the last 5 minutes.
			if(tmpDirPropFile.exists() && 
					(System.currentTimeMillis() - tmpDirPropFile.lastModified()) 
						< TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES) ) {
				try {
					FileInputStream fis = new FileInputStream(tmpDirPropFile);
					Properties props = new Properties();
					props.load(fis);
					fis.close();
					
					result = props.getProperty("test-config-xml");
					
					if(result != null && !result.isEmpty()) {

						String nodeRootNewValue = new File(result).getParentFile().getParent();
						System.setProperty("NODE_ROOT", nodeRootNewValue);
						
					}
					
					System.out.println("Debug mode: Using test directory: "+result);
					
				} catch(IOException ioe) {
					result = null;
				}
			}
		}

		return result;
	}
	
	private final static boolean DEBUG_TEST_MODE_ENABLED;
	static {
		
		boolean isDebugTest = false;
		String debugTestEnvVar = System.getenv("DEBUG_TEST_MODE_ENABLED");
				
		if(debugTestEnvVar == null) {
			debugTestEnvVar = System.getProperty("DEBUG_TEST_MODE_ENABLED");
		}
		
		if(debugTestEnvVar != null && debugTestEnvVar.equalsIgnoreCase("true")) {
			isDebugTest = true;
			System.err.println("* Debug/test mode is enabled.");	
		} 

		DEBUG_TEST_MODE_ENABLED = isDebugTest;
	}
	
	public static boolean isDebugTestModeEnabled() {
		return DEBUG_TEST_MODE_ENABLED;
	}
}
