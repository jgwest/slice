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

import java.util.Optional;

/** Returned by an implementor of the authentication/authorization service.*/
public class AuthServiceResponse {

	public static enum AuthError { UNAUTHENTICATED, UNAUTHORIZED, SETUP_ERROR }
	
	private Optional<Integer> httpStatusCode = Optional.empty();
	private Optional<String> httpStatusMessage = Optional.empty();

	private boolean isAuthenticatedAndAuthorized  = false;
	
	private Optional<AuthError> errorType = Optional.empty();
	
	public AuthServiceResponse() {
	}

	public Optional<Integer> getHttpStatusCode() {
		return httpStatusCode;
	}

	public void setHttpStatusCode(int httpStatusCode) {
		this.httpStatusCode = Optional.of(httpStatusCode);
	}

	public Optional<String> getHttpStatusMessage() {
		return httpStatusMessage;
	}

	public void setHttpStatusMessage(String httpStatusMessage) {
		this.httpStatusMessage = Optional.of(httpStatusMessage);
	}

	public boolean isAuthenticatedAndAuthorized() {
		return isAuthenticatedAndAuthorized;
	}
	
	public void setAuthenticatedAndAuthorized(boolean isAuthenticatedAndAuthorized) {
		this.isAuthenticatedAndAuthorized = isAuthenticatedAndAuthorized;
	}
	
	public void setErrorType(AuthError errorType) {
		this.errorType = Optional.of(errorType);
	}
	
	public Optional<AuthError> getErrorType() {
		return errorType;
	}
	
}
