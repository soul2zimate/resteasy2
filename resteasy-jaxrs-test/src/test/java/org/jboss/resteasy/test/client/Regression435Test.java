package org.jboss.resteasy.test.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;

import org.jboss.resteasy.client.ClientResponseFailure;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.test.BaseResourceTest;
import org.jboss.resteasy.test.TestPortProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class Regression435Test extends BaseResourceTest
{
   @Path("/test")
   public interface MyTest
   {
      @POST
      @Consumes("text/plain")
      public void postIt(String msg);
   }

   public static class MyTestResource implements MyTest
   {
      @Override
    public void postIt(String msg)
      {
         System.out.println("HERE: " + msg);
         throw new WebApplicationException(401);
      }
   }

   @Override
   @Before
   public void before() throws Exception {
      super.before();
      addPerRequestResource(MyTestResource.class);
   }

   @Test
   public void testMe() throws Exception
   {
      MyTest proxy = ProxyFactory.create(MyTest.class, TestPortProvider.generateURL(""));
      try
      {
         proxy.postIt("hello");
      }
      catch (ClientResponseFailure e)
      {
         Assert.assertEquals(401, e.getResponse().getStatus());
      }
   }

}
