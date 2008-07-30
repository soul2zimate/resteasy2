package org.jboss.resteasy.annotations.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Set response Cache-Control header automatically.
 * <p/>
 * "public" will automatically be set if this is a non-authenticated request, otherwise "private" is the default.
 * You can override the default public/private settings with the @Private and @Public annotations
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache
{
   int maxAge() default -1;

   int sMaxAge() default -1;

   boolean noStore() default false;

   boolean noTransform() default false;

   boolean mustRevalidate() default false;

   boolean proxyRevalidate() default false;

}