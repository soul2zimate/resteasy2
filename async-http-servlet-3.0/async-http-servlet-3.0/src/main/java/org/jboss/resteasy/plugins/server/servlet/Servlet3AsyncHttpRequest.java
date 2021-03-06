package org.jboss.resteasy.plugins.server.servlet;

import java.lang.annotation.Annotation;

import org.jboss.resteasy.core.AbstractAsynchronousResponse;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.spi.AsynchronousResponse;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.interception.MessageBodyWriterInterceptor;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class Servlet3AsyncHttpRequest extends HttpServletInputMessage
{
   protected HttpServletResponse response;
   protected HttpServletRequest theRequest;

   public Servlet3AsyncHttpRequest(HttpServletRequest httpServletRequest, HttpServletResponse response, HttpResponse httpResponse, HttpHeaders httpHeaders, UriInfo uriInfo, String s, SynchronousDispatcher synchronousDispatcher)
   {
      super(httpServletRequest, httpResponse, httpHeaders, uriInfo, s, synchronousDispatcher);
      this.response = response;
      this.theRequest = httpServletRequest;
   }

   @Override
   public void initialRequestThreadFinished()
   {
   }

   @Override
   public AsynchronousResponse createAsynchronousResponse(long l)
   {
      suspended = true;
      final AsyncContext context = theRequest.startAsync(request, response);
      if (l >= 0)
      {
         context.setTimeout(l);
      }
      final TimeoutListener timeoutListener = new TimeoutListener();
      context.addListener(timeoutListener);
      asynchronousResponse = new AbstractAsynchronousResponse()
      {
         public void setResponse(Response response)
         {
            if (timeoutListener.timedOut())
            {
               return;
            }
            try
            {
               setupResponse((ServerResponse) response);
               dispatcher.asynchronousDelivery(Servlet3AsyncHttpRequest.this, httpResponse, response);
            }
            finally
            {
               context.complete();
            }
         }

         @Override
         public void setFailure(Exception ex)
         {
            if (timeoutListener.timedOut())
            {
               return;
            }
            try
            {
               dispatcher.asynchronousDelivery(Servlet3AsyncHttpRequest.this, httpResponse, ex);
            }
            finally
            {
               context.complete();
            }

         }
      };
      asynchronousResponse.setAnnotations((Annotation[]) getAttribute(Annotation.class.getName()));
      asynchronousResponse.setMessageBodyWriterInterceptors((MessageBodyWriterInterceptor[]) getAttribute(MessageBodyWriterInterceptor.class.getName()));
      asynchronousResponse.setPostProcessInterceptors((PostProcessInterceptor[]) getAttribute((PostProcessInterceptor.class.getName())));
      removeAttribute(Annotation.class.getName());
      removeAttribute(MessageBodyWriterInterceptor.class.getName());
      removeAttribute(PostProcessInterceptor.class.getName());
      return asynchronousResponse;
   }

}
