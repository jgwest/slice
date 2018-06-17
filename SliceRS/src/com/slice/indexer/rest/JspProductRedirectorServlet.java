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

package com.slice.indexer.rest;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.slice.indexer.shared.Product;
import com.slice.indexer.ui.ProductSingleton;
import com.slice.indexer.ui.ProductUI;

/** HTTP requests to /resources/* are directed to this servlet. This class principally serves JS/CSS/HTML to the user. */
public class JspProductRedirectorServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public JspProductRedirectorServlet() {
    }

    private void handleJspCss(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
    	String uri = request.getRequestURI();
    	String path = request.getServletPath();
    	
    	if(uri.trim().toLowerCase().endsWith(".css")) {
    		response.setContentType("text/css");
    	}
    	
		String afterPath = uri.substring(uri.indexOf(path)+path.length()+1);
		
		if(!afterPath.startsWith("/")) {
			afterPath = "/"+afterPath;
		}
		
		String resource = afterPath.substring(1);
		
		int slash = resource.indexOf("/");
		if(slash != -1) {
			resource = resource.substring(0, slash);
		}
		
		if(ProductSingleton.getInstance().getProduct(resource) == null) {
			request.getRequestDispatcher(afterPath).include(request, response);
		} else {
			String afterResource = afterPath.substring(slash+1);
//			System.out.println("afterResource:"+afterResource);
			request.getRequestDispatcher(afterResource).include(request, response);
		}
		
		
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// Make sure product is loaded at beginning of request
		ProductSingleton.getInstance();
		
		String uri = request.getRequestURI();
		
		if(uri.toLowerCase().endsWith(".js") || uri.toLowerCase().endsWith(".css")) {

			handleJspCss(request, response);
			return;
		}
				
		String path = request.getServletPath();
		
		
		if(!uri.endsWith(".css") && !uri.endsWith(".js") && !uri.endsWith("/")) {
			uri = uri + "/";
		}
		
		String afterPath = uri.substring(uri.indexOf(path)+path.length()+1);

		if(afterPath.trim().length() == 0) {
			// No resource specified;
			response.sendRedirect("..");
			return;
		}
		
		afterPath = afterPath.substring(0, afterPath.indexOf("/"));
		
		
		String resource = afterPath;
		
		ProductUI productUI = ProductSingleton.getInstance().getProduct(resource);
		
		if(productUI == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter().append("<html><body>Resource not found: "+resource+"</body></html>");
			return;
		}
		
		Product product =  productUI.getProduct();
		
		request.setAttribute("requestProduct", product);
		
		
//		if(uri.endsWith(".css") || uri.endsWith(".js")) {
//			String afterAfterPath = uri.substring(uri.indexOf(afterPath)+ afterPath.length());
//			request.getRequestDispatcher(afterAfterPath).include(request, response);
//			return;
//		}
		
		
		
		request.getRequestDispatcher("/index.jsp?resource="+resource).include(request, response);
	}


}
