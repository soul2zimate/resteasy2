package org.jboss.resteasy.plugins.providers;

import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@ProduceMime("*/*")
public class StreamingOutputProvider implements MessageBodyWriter<StreamingOutput>
{
   public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations)
   {
      return StreamingOutput.class.isAssignableFrom(type);
   }

   public long getSize(StreamingOutput streamingOutput)
   {
      return -1;
   }

   public void writeTo(StreamingOutput streamingOutput, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException
   {
      streamingOutput.write(entityStream);
   }
}