/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.validator.internal.cfg;

import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.context.TypeConstraintMappingContext;
import org.hibernate.validator.internal.cfg.context.ConstraintMappingContext;
import org.hibernate.validator.internal.cfg.context.TypeConstraintMappingContextImpl;
import org.hibernate.validator.internal.util.Contracts;

import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

/**
 * Default implementation of {@link ConstraintMapping}.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public class DefaultConstraintMapping implements ConstraintMapping {
	private ConstraintMappingContext context;

	public DefaultConstraintMapping() {
		context = new ConstraintMappingContext();
	}

	@Override
	public final <C> TypeConstraintMappingContext<C> type(Class<C> beanClass) {
		Contracts.assertNotNull( beanClass, MESSAGES.beanTypeMustNotBeNull() );
		return new TypeConstraintMappingContextImpl<C>( beanClass, context );
	}

	public ConstraintMappingContext getContext() {
		return context;
	}
}
