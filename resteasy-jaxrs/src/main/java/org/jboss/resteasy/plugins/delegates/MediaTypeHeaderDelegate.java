package org.jboss.resteasy.plugins.delegates;

import org.jboss.resteasy.util.HeaderParameterParser;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.RuntimeDelegate;
import java.util.HashMap;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class MediaTypeHeaderDelegate implements RuntimeDelegate.HeaderDelegate
{
   public Object fromString(String type) throws IllegalArgumentException
   {
      if (type == null) throw new IllegalArgumentException("MediaType value is null");
      return parse(type);
   }


   public static MediaType parse(String type)
   {
      String params = null;
      int idx = type.indexOf(";");
      if (idx > -1)
      {
         params = type.substring(idx + 1).trim();
         type = type.substring(0, idx);
      }
      String major = null;
      String subtype = null;
      String[] paths = type.split("/");
      if (paths.length < 2 && type.equals("*"))
      {
         major = "*";
         subtype = "*";

      }
      else if (paths.length != 2
            || "".equals(paths[0]) || "".equals(paths[1])
            || paths[0].contains(" ") || paths[1].contains(" "))
      {
         throw new IllegalArgumentException("Failure parsing MediaType string: " + type);
      }
      else if (paths.length == 2)
      {
         major = paths[0];
         subtype = paths[1];
      }
      if (params != null && !params.equals(""))
      {
         HashMap<String, String> typeParams = new HashMap<String, String>();

         int start = 0;

         while (start < params.length())
         {
            start = HeaderParameterParser.setParam(typeParams, params, start);
         }
         return new MediaType(major, subtype, typeParams);
      }
      else
      {
         return new MediaType(major, subtype);
      }
   }

   public String toString(Object o)
   {
      MediaType type = (MediaType) o;
      String rtn = type.getType().toLowerCase() + "/" + type.getSubtype().toLowerCase();
      if (type.getParameters() == null || type.getParameters().size() == 0) return rtn;
      for (String name : type.getParameters().keySet())
      {
         String val = type.getParameters().get(name);
         rtn += ";" + name + "=\"" + val + "\"";
      }
      return rtn;
   }
}
