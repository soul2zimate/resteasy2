package org.resteasy.specimpl;

import org.resteasy.spi.ResteasyProviderFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWorkers;
import javax.ws.rs.ext.MessageBodyWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class MessageBodyWorkersImpl implements MessageBodyWorkers
{
   public <T> MessageBodyReader<T> getMessageBodyReader(Class<T> type, Type genericType, Annotation annotations[], MediaType mediaType)
   {
      return ResteasyProviderFactory.getInstance().createMessageBodyReader(type, genericType, annotations, mediaType);
   }

   public <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> type, Type genericType, Annotation annotations[], MediaType mediaType)
   {
      return ResteasyProviderFactory.getInstance().createMessageBodyWriter(type, genericType, annotations, mediaType);
   }
}