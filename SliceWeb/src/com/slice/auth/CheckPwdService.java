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

package com.slice.auth;

import java.io.IOException;
import java.util.ServiceLoader;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import com.slice.indexer.shared.util.SearchIndexerUtil;
import com.slice.indexer.ui.AuthServiceResponse;
import com.slice.indexer.ui.AuthServiceResponse.AuthError;
import com.slice.indexer.ui.IAuthService;
import com.slice.indexer.ui.ProductSingleton;
import com.slice.indexer.ui.ProductUI;

/** JAX-RS service (targeted by a <form> on our HTML login page) that checks that the login/password matches what is required
 * for the product, then sets a cookie if it matches (otherwise an error is reported). */
@Path("/CheckPwd")
public class CheckPwdService {

	private final static String ROOT = "/SliceWeb/auth/";
	
	@Context private HttpServletRequest request;
	@Context private HttpServletResponse response;
	
	@POST
	public void post(@FormParam("resourceId") String productId, @FormParam("username") String username, @FormParam("password") String password) throws IOException {		
		
		IAuthService service = null;
		for(IAuthService aus : ServiceLoader.load(IAuthService.class)) {
			service = aus;
			break;
		}
		
		if(service == null) {
			throw new RuntimeException("Unable to locate user authentication/authorization service. Ensure that SliceConfigPasswordService is included in the EAR, and its configuration elements are in the file configuration xml.");

			// Uncomment this line to allow any password:
			// service = new CheckPasswordNoop();
		}
		
		if (!request.isSecure()) {
			response.sendError(426,
					"SSL/TLS connection required for password checking. Ensure the server is configured to use HTTPS URL.");
			return;
		}

		if (productId == null || productId.trim().length() == 0) {
			response.setStatus(500);
			return;
		}

		ProductUI product = ProductSingleton.getInstance().getProduct(productId);
		if (product == null) {
			response.setStatus(500);
			return;
		}

//		IConfigConstants constants = product.getProduct().getConstants();

		boolean isValid = false;

		if(SearchIndexerUtil.isDebugTestModeEnabled()) {
			isValid = true;
		}
		
		if (!isValid) {
			
			AuthServiceResponse authResponse = service.isAuthenticatedAndAuthorized(username, password, product);

			if(!authResponse.isAuthenticatedAndAuthorized()) {
				
				if(authResponse.getErrorType().isPresent()) {
					
					if(authResponse.getErrorType().get() == AuthError.UNAUTHENTICATED) {
						// Sorry, the login/password you provided are incorrect!
						response.sendRedirect(ROOT + "InvalidPwd.jsp?resourceId=" + productId);		
					
					} else if(authResponse.getErrorType().get() == AuthError.UNAUTHORIZED) {
						// Your log-in and password were valid, but your email address wasn't in the authorized users list.
						response.sendRedirect(ROOT + "UnregisteredUser.jsp?resourceId=" + productId);
						
					} else if(authResponse.getErrorType().get() == AuthError.SETUP_ERROR) {
						response.getWriter().println("Error: The password service in the Slice configuration file is not configured properly." );
						return;
						
					} else {
						throw new IllegalArgumentException("Invalid error type");
					}
					
				} else { 
					throw new IllegalArgumentException("Unset error type");
				}
				
				return;
			}
			
			isValid = authResponse.isAuthenticatedAndAuthorized();
						
		}

		if (isValid) {
			setCookie(product, response);
			response.sendRedirect(ROOT + "LoginComplete.jsp?resourceId=" + productId);

		}

	}
	
	private static void setCookie(ProductUI p, HttpServletResponse response) {
		Cookie cookie1 = new Cookie(p.getProduct().getConstants().getCookieName(), "true");
		cookie1.setPath("/");
		cookie1.setMaxAge(24 * 60 * 60 * 365 * 10); // 10 years
		response.addCookie(cookie1);
	}

}
