package org.resteasy;

import org.resteasy.specimpl.UriBuilderImpl;
import org.resteasy.spi.ClientHttpOutput;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PathParamMarshaller implements Marshaller
{
   private String paramName;

   public PathParamMarshaller(String paramName)
   {
      this.paramName = paramName;
   }

   public void marshall(Object object, UriBuilderImpl uri, ClientHttpOutput output)
   {
      uri.uriParam(paramName, object.toString());
   }
}