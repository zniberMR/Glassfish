// $Id: ConstraintDescriptor.java 17004 2009-07-06 13:00:57Z epbernard $
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package javax.validation.metadata;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintPayload;

/**
 * Describes a single constraint and its composing constraints.
 * T is the constraint's annotation type.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public interface ConstraintDescriptor<T extends Annotation> {
	/**
	 * Returns the annotation describing the constraint declaration.
	 * If a composing constraint, attribute values are reflecting
	 * the overridden attributes of the composing constraint
	 *
	 * @return The annotation for this constraint.
	 */
	T getAnnotation();

	/**
	 * The Set of groups the constraint is applied on.
	 * If the constraint declares no group, a set with only the <code>Default</code>
	 * group is returned.
	 *
	 * @return The groups the constraint is applied on.
	 */
	Set<Class<?>> getGroups();

	/**
	 * The Set of payload the constraint hosts.
	 *
	 * @return payload classes hosted on the constraint or an empty set if none.
	 */
	Set<Class<ConstraintPayload>> getPayload();

	/**
	 * Immutable list of the constraint validation implementation classes.
	 *
	 * @return list of the constraint validation implementation classes.
	 */
	List<Class<? extends ConstraintValidator<T, ?>>>
	getConstraintValidatorClasses();

	/**
	 * Returns a map containing the annotation attribute names as keys and the
	 * annotation attribute values as value.
	 * If this constraint is used as part of a composed constraint, attribute
	 * values are reflecting the overridden attribute of the composing constraint.
	 *
	 * @return a map containing the annotation attribute names as keys
	 *         and the annotation attribute values as value.
	 */
	Map<String, Object> getAttributes();

	/**
	 * Return a set of composing <code>ConstraintDescriptor</code>s where each
	 * descriptor describes a composing constraint. <code>ConstraintDescriptor</code>
	 * instances of composing constraints reflect overridden attribute values in
	 * {@link #getAttributes()}  and {@link #getAnnotation()}.
	 *
	 * @return a set of <code>ConstraintDescriptor<code> objects or an empty set
	 *         in case there are no composing constraints.
	 */
	Set<ConstraintDescriptor<?>> getComposingConstraints();

	/**
	 * @return true if the constraint is annotated with @ReportAsSingleViolation
	 */
	boolean isReportAsSingleViolation();
}