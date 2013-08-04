package org.greencheek.spring.rest.servletmocks;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;

public class MockMethodHttpHandler extends HttpServlet {

	private String method;
	
	
	public MockMethodHttpHandler(String method)
	{
		setMethod(method);
	}


	public void service(HttpServletRequest req, HttpServletResponse resp)
	{		
		String method = req.getMethod();
		resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("plain/text");
        resp.setHeader("X-METHOD", method);
        assertEquals("Invalid HTTP method", getMethod(),method);
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getMethod() {
		return method;
	}
}
