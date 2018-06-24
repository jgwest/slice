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

package com.slice.indexer.ui;

import java.io.IOException;

/**
 * To provide authentication and authorization of usernames and passwords, implementors should extend this class and
 * return an AuthServiceResponse based on whether the username/password is correct for the given product. 
 **/
public interface IAuthService {

	public AuthServiceResponse isAuthenticatedAndAuthorized(String username, String password, ProductUI product) throws IOException;
	
}
