package org.greencheek.spring.rest.servletmocks;

import org.springframework.util.FileCopyUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

public class MockEchoHttpHandler extends HttpServlet {


	public void service(HttpServletRequest req, HttpServletResponse resp)
	{
		String path = req.getRequestURI();
		path = path.replace("/echo/", "");
		
		
		Enumeration<String> headernames = req.getHeaderNames();
		while(headernames.hasMoreElements())
		{
			String s = headernames.nextElement();
			Enumeration<String> values = req.getHeaders(s);
			while(values.hasMoreElements())
			{
				resp.addHeader(s, values.nextElement());
			}
		}

		resp.setHeader("X-PATH", path);
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("plain/text");
		try
		{
			FileCopyUtils.copy(req.getInputStream(), resp.getOutputStream());
		}
		catch(IOException e){}
		

	}


}
