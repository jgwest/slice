package com.slice.auth;

import java.io.IOException;

import org.w3c.dom.Element;

import com.slice.indexer.ui.AuthServiceResponse;
import com.slice.indexer.ui.AuthServiceResponse.AuthError;
import com.slice.indexer.ui.IAuthService;
import com.slice.indexer.ui.ProductUI;

/** This class will read passwords from the file configuration XML, in the form of:
 * 	<User name="" password=""/>
 * 
 *  name may be a generic text username, or an email address.
 *  (username or user are also accepted as names for this attribute)
 **/
public class ConfigXmlPasswordService implements IAuthService {

	public ConfigXmlPasswordService() {
	}
	
	@Override
	public AuthServiceResponse isAuthenticatedAndAuthorized(String usernameParam, String passwordParam, ProductUI product) throws IOException {
		
		AuthServiceResponse response = new AuthServiceResponse();

		boolean match = false;
		
		for(Element e : product.getProduct().getConfigEntries()) {
			
			if(e.getNodeName().equals("User")) {
				
				// We will accept any user name from the following attributes
				String attrUsername = e.getAttribute("name");
				if(isEmpty(attrUsername)) {
					attrUsername = e.getAttribute("username");
				}
				if(isEmpty(attrUsername)) {
					attrUsername = e.getAttribute("user");
				}

				String attrPassword = e.getAttribute("password");
				
				if(isEmpty(attrPassword)) {
					attrPassword = e.getAttribute("pass");
				}
				
				if(attrUsername != null && attrPassword != null) {
					
					match = attrUsername.equals(usernameParam) && attrPassword.equals(passwordParam);
					
				}
				
			} 
			
			if(match) { break; }
			
		}
		
		if(!match) {
			response.setErrorType(AuthError.UNAUTHENTICATED);
			response.setAuthenticatedAndAuthorized(false);
			return response;
		}

		response.setAuthenticatedAndAuthorized(true);
		
		return response;
	}

	
	private static boolean isEmpty(String str) {
		if(str == null) { return true;} 
		return str.trim().isEmpty();
	}
}
