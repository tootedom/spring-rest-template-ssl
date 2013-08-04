package org.greencheek.spring.rest.servletmocks;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class MockStatusHttpHandler extends HttpServlet {
	private volatile int status;
	
	public MockStatusHttpHandler(int status)
	{
		setStatus(status);

	}

	public void service(HttpServletRequest req, HttpServletResponse resp)
	{
		resp.setStatus(getStatus());
	}
	
	public void setStatus(int status) {
		this.status = status;
	}

	public int getStatus() {
		return status;
	}
}
