package org.jboss.resteasy.core;

import org.jboss.resteasy.specimpl.RequestImpl;
import org.jboss.resteasy.spi.ApplicationException;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.LoggableFailure;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@SuppressWarnings("unchecked")
public class ContextParameterInjector implements ValueInjector
{
   private Class type;
   private ResteasyProviderFactory factory;

   public ContextParameterInjector(Class type, ResteasyProviderFactory factory)
   {
      this.type = type;
      this.factory = factory;
   }

   public Object inject(HttpRequest request, HttpResponse response)
   {
      if (type.equals(HttpHeaders.class)) return request.getHttpHeaders();
      if (type.equals(UriInfo.class)) return request.getUri();
      if (type.equals(Request.class)) return new RequestImpl(request);
      if (type.equals(HttpRequest.class)) return request;
      if (type.equals(Providers.class)) return factory;
      else return ResteasyProviderFactory.getContextData(type);
   }

   private class GenericDelegatingProxy implements InvocationHandler
   {
      public Object invoke(Object o, Method method, Object[] objects) throws Throwable
      {
         try
         {
            Object delegate = ResteasyProviderFactory.getContextData(type);
            if (delegate == null)
               throw new LoggableFailure("Unable to find contextual data of type: " + type.getName());
            return method.invoke(delegate, objects);
         }
         catch (IllegalAccessException e)
         {
            throw new RuntimeException(e);
         }
         catch (IllegalArgumentException e)
         {
            throw new RuntimeException(e);
         }
         catch (InvocationTargetException e)
         {
            throw new ApplicationException(e.getCause());
         }
      }
   }

   public Object inject()
   {
      if (type.equals(Providers.class)) return factory;
      if (!type.isInterface())
      {
         Object delegate = ResteasyProviderFactory.getContextData(type);
         if (delegate != null) return delegate;
         throw new RuntimeException("Illegal to inject a non-interface type into a singleton");
      }


      Class[] intfs = {type};


      return Proxy.newProxyInstance(type.getClassLoader(), intfs, new GenericDelegatingProxy());
   }
}
