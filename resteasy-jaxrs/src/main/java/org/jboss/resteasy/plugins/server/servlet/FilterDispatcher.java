package org.jboss.resteasy.plugins.server.servlet;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.specimpl.UriInfoImpl;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.NotFoundException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class FilterDispatcher implements Filter, HttpRequestFactory, HttpResponseFactory
{
   protected ServletContainerDispatcher servletContainerDispatcher;

   public Dispatcher getDispatcher()
   {
      return servletContainerDispatcher.getDispatcher();
   }


   public void init(FilterConfig servletConfig) throws ServletException
   {
      servletContainerDispatcher = new ServletContainerDispatcher();
      FilterBootstrap bootstrap = new FilterBootstrap(servletConfig);
      servletContainerDispatcher.init(servletConfig.getServletContext(), bootstrap, this, this);
      servletContainerDispatcher.getDispatcher().getDefaultContextObjects().put(FilterConfig.class, servletConfig);

   }

   public HttpRequest createResteasyHttpRequest(String httpMethod, HttpServletRequest request, HttpHeaders headers, UriInfoImpl uriInfo, HttpResponse theResponse, HttpServletResponse response)
   {
      return new HttpServletInputMessage(request, theResponse, headers, uriInfo, httpMethod.toUpperCase(), (SynchronousDispatcher) getDispatcher());
   }


   public HttpResponse createResteasyHttpResponse(HttpServletResponse response)
   {
      return new HttpServletResponseWrapper(response, getDispatcher().getProviderFactory());
   }

   public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
   {
      try
      {
         servletContainerDispatcher.service(((HttpServletRequest) servletRequest).getMethod(), (HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse, false);
      }
      catch (NotFoundException e)
      {
         filterChain.doFilter(servletRequest, servletResponse);
      }
   }

   public void destroy()
   {
      servletContainerDispatcher.destroy();
   }


}
