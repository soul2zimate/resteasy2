package org.jboss.resteasy.plugins.cache.server;

import org.jboss.resteasy.annotations.interception.RedirectPrecedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@ServerInterceptor
@RedirectPrecedence
public class ServerCacheHitInterceptor implements PreProcessInterceptor
{
   protected ServerCache cache;
   public static final String DO_NOT_CACHE_RESPONSE = "DO NOT CACHE RESPONSE";

   public ServerCacheHitInterceptor(ServerCache cache)
   {
      this.cache = cache;
   }

   @Context
   protected Request validation;

   public ServerResponse preProcess(HttpRequest request, ResourceMethod method) throws Failure, WebApplicationException
   {
      String key = request.getUri().getRequestUri().toString();
      if (request.getHttpMethod().equalsIgnoreCase("GET"))
      {
         return handleGET(request, method, key);
      }
      else if (request.getHttpMethod().equalsIgnoreCase("PUT")
              || request.getHttpMethod().equalsIgnoreCase("POST")
              || request.getHttpMethod().equalsIgnoreCase("DELETE"))
      {
         // if PUT, POST, DELETE, automatically clear cache
         cache.remove(key);
      }
      return null;
   }

   private ServerResponse handleGET(HttpRequest request, ResourceMethod method, String key)
   {
      MediaType chosenType = method.resolveContentType(request, null);
      ServerCache.Entry entry = cache.get(key, chosenType);
      if (entry != null)
      {
         if (entry.isExpired())
         {
            cache.remove(key);
            return null;
         }
         else
         {
            // validation if client sent
            Response.ResponseBuilder builder = validation.evaluatePreconditions(new EntityTag(entry.getEtag()));
            CacheControl cc = new CacheControl();
            cc.setMaxAge(entry.getExpirationInSeconds());
            if (builder != null)
            {
               return (ServerResponse) builder.cacheControl(cc).build();
            }


            ServerResponse serverResponse = new ServerResponse();
            serverResponse.setEntity(entry.getCached());
            MultivaluedMapImpl<String, Object> headers = new MultivaluedMapImpl<String, Object>();
            headers.putAll(entry.getHeaders());
            headers.putSingle(HttpHeaders.CACHE_CONTROL, cc);
            serverResponse.setMetadata(headers);
            request.setAttribute(DO_NOT_CACHE_RESPONSE, true);
            return serverResponse;
         }
      }
      return null;
   }
}
