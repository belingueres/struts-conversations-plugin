package com.hexaid.struts2.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.opensymphony.xwork2.ActionSupport;

/**
 * @author Gabriel Belingueres
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.METHOD })
public @interface End {
	
	/**
	 * Set if the current conversation must retain its values
	 * after an HTTP redirect (default) or not (false).
	 */
	boolean beforeRedirect() default false;
	
	/**
	 * Set if the current conversation must commit its
	 * transaction (default) or must rollback all persistence
	 * related operations (false).
	 */
	boolean commit() default true;
	
	/**
	 * Only ends the conversation if the Action result is
	 * the specified here. Defaults to ActionSupport.SUCCESS.
	 */
	String endResult() default ActionSupport.SUCCESS;
}
