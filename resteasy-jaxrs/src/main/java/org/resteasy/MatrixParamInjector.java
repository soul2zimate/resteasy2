package org.resteasy;

import org.resteasy.spi.HttpRequest;
import org.resteasy.spi.HttpResponse;

import javax.ws.rs.MatrixParam;
import javax.ws.rs.core.PathSegment;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class MatrixParamInjector extends StringParameterInjector implements ValueInjector
{
   public MatrixParamInjector(Class type, Type genericType, AccessibleObject target, String paramName, String defaultValue)
   {
      super(type, genericType, paramName, "@" + MatrixParam.class.getSimpleName(), defaultValue, target);
   }

   public Object inject(HttpRequest request, HttpResponse response)
   {
      ArrayList<String> values = new ArrayList<String>();
      for (PathSegment segment : request.getUri().getPathSegments())
      {
         List<String> list = segment.getMatrixParameters().get(paramName);
         if (list != null) values.addAll(list);
      }
      if (values.size() == 0) return extractValues(null);
      else return extractValues(values);
   }

   public Object inject()
   {
      throw new RuntimeException("It is illegal to inject a @MatrixParam into a singleton");
   }
}