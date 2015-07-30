package org.jboss.resteasy.test.providers.datasource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import javax.activation.DataSource;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.core.AsynchronousDispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.test.EmbeddedContainer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * RESTEASY-1182
 * 
 * @author <a href="ron.sigal@jboss.com">Ron Sigal</a>
 * @version $Revision: 1.1 $
 *
 * Copyright Jul 24, 2015
 */
public class DataSourceTempFileAsynchTest
{
   private static CountDownLatch latch;
   private static AsynchronousDispatcher dispatcher;

   @Path("/")
   public static class TestResource
   {
      @Path("oneway")
      @Consumes(MediaType.APPLICATION_OCTET_STREAM)
      @POST
      public void oneway(DataSource source) throws Exception
      {
         InputStream is = source.getInputStream();  
         while (is.read() > -1)
            ;
         source.getInputStream();
         latch.await();
         System.out.println("oneway() returning");
      }
      
      @Path("asynch")
      @Consumes(MediaType.APPLICATION_OCTET_STREAM)
      @POST
      public Response asynch(DataSource source) throws Exception
      {
         System.out.println("entering asynch()");
         InputStream is = source.getInputStream();  
         while (is.read() > -1)
            ;
         source.getInputStream();
         latch.await();
         System.out.println("asynch() returning");
         return Response.ok().entity("ok").build();
      }
   }

   @BeforeClass
   public static void before() throws Exception
   {
      ResteasyDeployment deployment = new ResteasyDeployment();
      deployment.setAsyncJobServiceEnabled(true);
      EmbeddedContainer.start(deployment);

      dispatcher = (AsynchronousDispatcher) deployment.getDispatcher();
      dispatcher.getRegistry().addPerRequestResource(TestResource.class);
   }

   @AfterClass
   public static void after() throws Exception
   {
      EmbeddedContainer.stop();
   }

   @Test
   public void testOneway() throws Exception
   {
      ClientRequest request = new ClientRequest("http://localhost:8081/oneway?oneway=true");
      request.body(MediaType.APPLICATION_OCTET_STREAM, getEntity());
      ClientResponse<?> response = null;
      try
      {
         latch = new CountDownLatch(1);
         response = request.post();
         Assert.assertEquals(HttpServletResponse.SC_ACCEPTED, response.getStatus());
         Thread.sleep(1000);
         int count = countTempFiles();
         System.out.println("count: " + count);
         latch.countDown();
         Thread.sleep(2000);
         int newCount = countTempFiles();
         System.out.println("newCount: " + newCount);
         Assert.assertTrue(count > newCount);
      }
      finally
      {
         response.releaseConnection();
      }
   }

   @Test
   public void testAsynch() throws Exception
   {
      ClientResponse<?> response = null;
      ClientRequest request = null;
      
      {
         latch = new CountDownLatch(1);
         request = new ClientRequest("http://localhost:8081/asynch?asynch=true");
         request.body(MediaType.APPLICATION_OCTET_STREAM, getEntity());
         response = request.post();
         Assert.assertEquals(HttpServletResponse.SC_ACCEPTED, response.getStatus());
         String jobUrl = response.getHeaders().getFirst(HttpHeaders.LOCATION);
         System.out.println("JOB: " + jobUrl);
         response.releaseConnection();

         Thread.sleep(2000);
         int count = countTempFiles();
         System.out.println("count: " + count);
         latch.countDown();
         
         Thread.sleep(2000);
         request = new ClientRequest(jobUrl);
         response = request.post();
         System.out.println("status: " + response.getStatus());
         String result = response.getEntity(String.class);
         System.out.println("response: " + result);
         Assert.assertEquals(200, response.getStatus());
         Assert.assertEquals("ok", result);
         response.releaseConnection();
         
         int newCount = countTempFiles();
         System.out.println("count, updated: " + newCount);
         Assert.assertTrue(count > newCount);
      }
   }

   static byte[] getEntity()
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(5000);
      for (int i = 0; i < 5000; i++)
      {
         baos.write(i);
      }
      return baos.toByteArray();
   }
   
   static int countTempFiles() throws Exception
   {
      String tmpdir = System.getProperty("java.io.tmpdir");
//      System.out.println("tmpdir: " + tmpdir);
      File dir = new File(tmpdir);
      int counter = 0;
      for (File file : dir.listFiles())
      {  
         if (file.getName().startsWith("resteasy-provider-datasource"))
         {
            counter++;
         }
      }
      return counter;
   }
}
