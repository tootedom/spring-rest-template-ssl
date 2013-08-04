package org.greencheek.spring.rest.servletmocks;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MockRedirectHttpHandler extends HttpServlet {

	private boolean useSeeOtherRedirect;
	private String redirectUrl;
	
	public MockRedirectHttpHandler(String redirectUrl)
	{
		this(redirectUrl,false);
	}
	
	public MockRedirectHttpHandler(String redirectUrl,boolean useSeeOther)
	{
		super();
		setUseSeeOtherRedirect(useSeeOther);
		setRedirectUrl(redirectUrl);
	}

	public void service(HttpServletRequest req, HttpServletResponse resp) {
		
		resp.setHeader("Location", getRedirectUrl());
		
		if(isUseSeeOtherRedirect())
		{
			resp.setStatus(HttpServletResponse.SC_SEE_OTHER);				
		}
		else
		{
			resp.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);			
		}
	}

	public void setUseSeeOtherRedirect(boolean useSeeOtherRedirect) {
		this.useSeeOtherRedirect = useSeeOtherRedirect;
	}

	public boolean isUseSeeOtherRedirect() {
		return useSeeOtherRedirect;
	}

	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}
}
