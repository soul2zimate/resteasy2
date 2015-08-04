package org.jboss.resteasy.spring.test.javaconfig;

import static org.junit.Assert.*;

import java.io.InputStream;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

import com.meterware.servletunit.ServletRunner;

/**
 * This test will verify that the resource invoked by RESTEasy has been
 * initialized by spring when defined using spring's JavaConfig.
 */
public class JavaConfigTest {
   private static final String CONTEXT_PATH = "/";
   private static final String BASE_URL = "http://localhost:9092";
   private static final String PATH = "/rest/invoke";

   private Server server;
   private WebAppContext context;

   @Before
   public void before() throws Exception {
      server = new Server(9092);
      context = new WebAppContext();
      context.setResourceBase("src/test/resources/javaconfig");
      context.setContextPath(CONTEXT_PATH);
      context.setParentLoaderPriority(true);
      server.setHandler(context);
      server.start();
   }

   @After
   public void after() throws Exception
   {
       server.stop();
       context.stop();
   }

   @Test
   public void test() throws Exception {
       ClientRequest request = new ClientRequest(BASE_URL + CONTEXT_PATH + PATH);
       @SuppressWarnings("unchecked")
       ClientResponse<String> response = request.get();
       assertEquals("unexpected response code", 200, response.getResponseStatus().getStatusCode());
       assertEquals("unexpected response msg", "hello", response.getEntity(String.class));
   }
}
