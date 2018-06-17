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

import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

/** Not thread-safe */
public class LoginInfo {

	private final String login;
	private final String password;
	private final String hostUrl;
	private final String productId;

	Map<String, NewCookie> loginCookies = null;
	
	public LoginInfo(String login, String password, String hostUrl, String productId) {
		this.login = login;
		this.password = password;
		this.hostUrl = hostUrl;
		this.productId = productId;
	}

	private boolean logIn() {
		
		Client client = RestClientUtil.generateClient();

		WebTarget target = client.target(hostUrl+"/SliceWeb/jaxrs" + "/CheckPwd");
		
		Form form = new Form();
		form = form.param("resourceId", productId).param("username", login).param("password", password);

		Response r = target.request().post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
//		System.out.println(r.getStatusInfo().getStatusCode());
//		System.out.println(r.getHeaderString("Location"));

		this.loginCookies = r.getCookies();

		return r.getStatusInfo().getStatusCode() == 302 &&  !r.getHeaderString("Location").toLowerCase().contains("invalid");
		
	}

	
	public void addCookieToBuilder(Builder builder) {
		
		if(loginCookies == null) {
			boolean loggedIn = logIn();
			if(!loggedIn) {
				throw new RuntimeException("Unable to log-in.");
			}
		}
		
		for(Map.Entry<String, NewCookie> e : loginCookies.entrySet()) {
			builder = builder.cookie(e.getValue().getName(), e.getValue().getValue());
		}
		
	}
	
}
