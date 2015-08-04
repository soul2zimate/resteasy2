package org.jboss.resteasy.plugins.server.sun.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsServer;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.specimpl.HttpHeadersImpl;
import org.jboss.resteasy.specimpl.PathSegmentImpl;
import org.jboss.resteasy.specimpl.UriBuilderImpl;
import org.jboss.resteasy.specimpl.UriInfoImpl;
import org.jboss.resteasy.util.HttpHeaderNames;
import org.jboss.resteasy.util.MediaTypeHelper;
import org.jboss.resteasy.util.PathHelper;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class HttpExchangeUtil
{
   public static UriInfoImpl extractUriInfo(HttpExchange exchange)
   {
      String host = exchange.getLocalAddress().getHostName();
      if (exchange.getLocalAddress().getPort() != 80
              && exchange.getLocalAddress().getPort() != 443)
      {
         host += ":" + exchange.getLocalAddress().getPort();
      }
      String uri = exchange.getRequestURI().toString();

      String protocol = exchange.getHttpContext().getServer() instanceof HttpsServer ? "https" : "http";

      URI absolutePath = null;
      try // extract without query param
      {
         URL absolute = new URL(protocol + "://" + host + uri);

         UriBuilderImpl builder = new UriBuilderImpl();
         builder.scheme(absolute.getProtocol());
         builder.host(absolute.getHost());
         builder.port(absolute.getPort());
         builder.path(absolute.getPath());
         builder.replaceQuery(absolute.getQuery());
         absolutePath = builder.build();
      }
      catch (MalformedURLException e)
      {
         throw new RuntimeException(e);
      }

      String contextPath = exchange.getHttpContext().getPath();
      String path = PathHelper.getEncodedPathInfo(absolutePath.getRawPath(), contextPath);
      if (!path.startsWith("/"))
      {
         path = "/" + path;
      }
      List<PathSegment> pathSegments = PathSegmentImpl.parseSegments(path, false);

      URI baseURI = absolutePath;
      if (!path.trim().equals(""))
      {
         String tmpContextPath = contextPath;
         if (!tmpContextPath.endsWith("/")) tmpContextPath += "/";
         baseURI = UriBuilder.fromUri(absolutePath).replacePath(tmpContextPath).build();
      }
      //System.out.println("path: " + path);
      //System.out.println("query string: " + request.getQueryString());
      UriInfoImpl uriInfo = new UriInfoImpl(absolutePath, baseURI, path, absolutePath.getRawQuery(), pathSegments);
      return uriInfo;
   }

   public static javax.ws.rs.core.HttpHeaders extractHttpHeaders(HttpExchange request)
   {
      HttpHeadersImpl headers = new HttpHeadersImpl();

      MultivaluedMap<String, String> requestHeaders = extractRequestHeaders(request);
      headers.setRequestHeaders(requestHeaders);
      List<MediaType> acceptableMediaTypes = extractAccepts(requestHeaders);
      List<String> acceptableLanguages = extractLanguages(requestHeaders);
      headers.setAcceptableMediaTypes(acceptableMediaTypes);
      headers.setAcceptableLanguages(acceptableLanguages);
      headers.setLanguage(requestHeaders.getFirst(HttpHeaderNames.CONTENT_LANGUAGE));

      String contentType = requestHeaders.getFirst(HttpHeaderNames.CONTENT_TYPE);
      if (contentType != null) headers.setMediaType(MediaType.valueOf(contentType));

      Map<String, Cookie> cookies = extractCookies(requestHeaders);
      headers.setCookies(cookies);
      return headers;

   }

   static Map<String, Cookie> extractCookies(MultivaluedMap<String, String> headers)
   {
      Map<String, Cookie> cookies = new HashMap<String, Cookie>();
      List<String> cookieHeaders = headers.get("Cookie");
      if (cookieHeaders == null) return cookies;

      for (String cookieVal : cookieHeaders)
      {
         Cookie cookie = Cookie.valueOf(cookieVal);
         cookies.put(cookie.getName(), cookie);
      }
      return cookies;
   }

   public static List<MediaType> extractAccepts(MultivaluedMap<String, String> requestHeaders)
   {
      List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
      List<String> accepts = requestHeaders.get(HttpHeaderNames.ACCEPT);
      if (accepts == null) return acceptableMediaTypes;

      for (String accept : accepts)
      {
         acceptableMediaTypes.addAll(MediaTypeHelper.parseHeader(accept));
      }
      return acceptableMediaTypes;
   }

   public static List<String> extractLanguages(MultivaluedMap<String, String> requestHeaders)
   {
      List<String> acceptable = new ArrayList<String>();
      List<String> accepts = requestHeaders.get(HttpHeaderNames.ACCEPT_LANGUAGE);
      if (accepts == null) return acceptable;

      for (String accept : accepts)
      {
         String[] splits = accept.split(",");
         for (String split : splits) acceptable.add(split.trim());
      }
      return acceptable;
   }

   public static MultivaluedMap<String, String> extractRequestHeaders(HttpExchange request)
   {
      Headers<String> requestHeaders = new Headers<String>();

      for (Map.Entry<String, List<String>> header : request.getRequestHeaders().entrySet())
      {
         for (String val : header.getValue())
         {
            requestHeaders.add(header.getKey(), val);
         }
      }
      return requestHeaders;
   }
}
