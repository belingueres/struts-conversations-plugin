package com.hexaid.struts2.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Gabriel Belingueres
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.METHOD })
public @interface Begin {
	
	/**
	 * The name of an initialization method inside the Action class
	 * that should be executed before the action method itself to
	 * allow initialization of conversation related code.
	 */
	String initialization() default "";
	
	/**
	 * If not empty, it means the newly created conversation will
	 * be a natural conversation, where the natural conversation
	 * Id will be extracted from the OGNL specified.<br/>
	 * If empty, the newly created conversation will have a 
	 * synthetic conversation Id.
	 */
	String naturalIdExpression() default "";
}
