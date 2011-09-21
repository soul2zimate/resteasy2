package org.jboss.resteasy.plugins.server.resourcefactory;

import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.InjectorFactory;
import org.jboss.resteasy.spi.ResourceFactory;

/**
 * VERY simple implementation that just returns the instance the SingleResource was created with
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SingletonResource implements ResourceFactory
{
   private Object obj;

   public SingletonResource(Object obj)
   {
      this.obj = obj;
   }

   public void registered(InjectorFactory factory)
   {
      factory.createPropertyInjector(obj.getClass()).inject(obj);
   }

   public Object createResource(HttpRequest request, HttpResponse response, InjectorFactory factory)
   {
      return obj;
   }

   public void unregistered()
   {
   }

   public Class<?> getScannableClass()
   {
      return obj.getClass();
   }

   public void requestFinished(HttpRequest request, HttpResponse response, Object resource)
   {
   }
}
