package com.hexaid.struts2.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.hexaid.struts2.common.ScopeType;

/**
 * @author Gabriel Belingueres
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.FIELD, ElementType.METHOD })
public @interface Out {
	/**
	 * The variable name used as a key to save the value
	 * in the scope where the value will be outjected.
	 * Defauls to the variable name if applied to a property,
	 * or to the property name if applied to a setter method.
	 */
	String value() default "";
	
	/**
	 * The scope where the value should be outjected to.
	 * Defaults to CONVERSATION.
	 * @see ScopeType
	 */
	ScopeType scope() default ScopeType.CONVERSATION;

	/**
	 * Specifies if the outjected value is required to be
	 * not null. Default to false.
	 */
	boolean required() default false;
}
