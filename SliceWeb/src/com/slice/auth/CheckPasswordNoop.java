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

import com.slice.indexer.ui.AuthServiceResponse;
import com.slice.indexer.ui.IAuthService;
import com.slice.indexer.ui.ProductUI;

/** This class will authenticate and authorize any user. */
public class CheckPasswordNoop implements IAuthService {

	@Override
	public AuthServiceResponse isAuthenticatedAndAuthorized(String username, String password, ProductUI product)
			throws IOException {

		System.err.println("WARNING: "+CheckPasswordNoop.class.getName()+" Password was not checked for user "+username);
		
		AuthServiceResponse response = new AuthServiceResponse();
		response.setAuthenticatedAndAuthorized(true);
		
		return response;
	}

}
