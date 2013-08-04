package org.greencheek.spring.rest.servletmocks;


import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class MockRedirectSeeOtherServlet extends GenericServlet {

		private final String location;

		public MockRedirectSeeOtherServlet(String location) {
            this.location = location;
		}

		@Override
		public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
			HttpServletRequest request = (HttpServletRequest) req;
			HttpServletResponse response = (HttpServletResponse) res;
			response.setStatus(HttpServletResponse.SC_SEE_OTHER);
			StringBuilder builder = new StringBuilder(36);
			builder.append(request.getScheme()).append("://");
			builder.append(request.getServerName()).append(':').append(request.getServerPort());
			builder.append(location);
			response.addHeader("Location", builder.toString());
		}
	}