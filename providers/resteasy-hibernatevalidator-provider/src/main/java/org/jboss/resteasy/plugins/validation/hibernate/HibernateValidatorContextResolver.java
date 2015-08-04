package org.jboss.resteasy.plugins.validation.hibernate;

import javax.validation.Validation;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.spi.validation.ValidatorAdapter;

@Provider
public class HibernateValidatorContextResolver implements
		ContextResolver<ValidatorAdapter> {

	private static final HibernateValidatorAdapter adapter = new HibernateValidatorAdapter(Validation.buildDefaultValidatorFactory().getValidator());
	
	@Override
	public ValidatorAdapter getContext(Class<?> type) {
		return adapter; 
	}

}
