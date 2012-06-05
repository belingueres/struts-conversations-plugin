package com.hexaid.struts2.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.hexaid.struts2.common.ConversationAttributeType;

/**
 * @author Gabriel Belingueres
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.METHOD })
public @interface ConversationControl {
	
	/**
	 * Attribute type declared in ConversationAttributeType which states
	 * in which way the conversation will be handled. Default is REQUIRED.
	 */
	ConversationAttributeType value() default ConversationAttributeType.REQUIRED;
	
}
