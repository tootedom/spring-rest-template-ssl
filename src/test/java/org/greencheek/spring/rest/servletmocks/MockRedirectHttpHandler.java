package org.greencheek.spring.rest.servletmocks;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MockRedirectHttpHandler extends HttpServlet {

	private final boolean useSeeOtherRedirect;
	private final String redirectUrl;
	
	public MockRedirectHttpHandler(String redirectUrl)
	{
		this(redirectUrl,false);
	}
	
	public MockRedirectHttpHandler(String redirectUrl,boolean useSeeOther)
	{
		super();
        this.useSeeOtherRedirect = useSeeOther;
		this.redirectUrl = redirectUrl;
	}

	public void service(HttpServletRequest req, HttpServletResponse resp) {
		
		resp.setHeader("Location", redirectUrl);
		
		if(useSeeOtherRedirect)
		{
			resp.setStatus(HttpServletResponse.SC_SEE_OTHER);				
		}
		else
		{
			resp.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);			
		}
	}


}
