package org.jboss.resteasy.test.providers.datasource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Hashtable;

import javax.activation.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.test.EmbeddedContainer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * RESTEASY-1182
 * 
 * @author <a href="ron.sigal@jboss.com">Ron Sigal</a>
 * @version $Revision: 1.1 $
 *
 * Copyright Jul 24, 2015
 */
public class DataSourceTempFileTest
{
   protected ResteasyDeployment deployment;
   protected Dispatcher dispatcher;
   
   @Path("/")
   public static class TestResource
   {
      @Path("post")
      @Consumes(MediaType.APPLICATION_OCTET_STREAM)
      @POST
      public Response post(DataSource source) throws Exception
      {
         InputStream is = source.getInputStream();  
         while (is.read() > -1)
            ;
         source.getInputStream();
         return Response.ok().entity(countTempFiles()).build();
      }
   }
   
   @Before
   public void before() throws Exception
   {
      Hashtable<String,String> initParams = new Hashtable<String,String>();
      Hashtable<String,String> contextParams = new Hashtable<String,String>();
      deployment = EmbeddedContainer.start(initParams, contextParams);
      dispatcher = deployment.getDispatcher();
      deployment.getRegistry().addPerRequestResource(TestResource.class);
   }

   @After
   public void after() throws Exception
   {
      EmbeddedContainer.stop();
      dispatcher = null;
      deployment = null;
   }
   
   @Test
   public void testFileNotFound() throws Exception
   {
      ClientRequest request = new ClientRequest("http://localhost:8081/post");
      ByteArrayOutputStream baos = new ByteArrayOutputStream(5000);
      for (int i = 0; i < 5000; i++)
      {
         baos.write(i);
      }
      request.body(MediaType.APPLICATION_OCTET_STREAM, baos.toByteArray());
      ClientResponse<?> response = request.post();
      System.out.println(response.getStatus());
      Assert.assertEquals(200, response.getStatus());
      int counter = response.getEntity(int.class);
      int updated = countTempFiles();
      System.out.println("counter from server: " + counter);
      System.out.println("counter updated: " + countTempFiles());
      Assert.assertTrue(counter > updated);
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
