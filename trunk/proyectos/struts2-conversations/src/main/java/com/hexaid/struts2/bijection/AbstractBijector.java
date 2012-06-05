package com.hexaid.struts2.bijection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.StrutsException;

import com.hexaid.struts2.annotations.In;
import com.hexaid.struts2.annotations.Out;
import com.hexaid.struts2.common.ConversationUtils;
import com.hexaid.struts2.conversations.Conversation;

/**
 * @author Gabriel Belingueres
 *
 */
public abstract class AbstractBijector<T extends AccessibleObject & Member> implements Bijector {

	@Override
	public void doInjections(Object action, Class<?> actionClass) {
		List<T> elements = getElementsWithAnnotation(In.class, actionClass);
		
		for(final T element : elements) {
			// allow to set private and protected fields
			element.setAccessible(true);
			final In annotationIn = element.getAnnotation(In.class);
			
			final String name = resolveName(annotationIn.value(), element);
			
			final Class<?> classToInject = getClassToInject(element);

			Object valueToInject = injectSpecialObjectByType(classToInject);
			
			if (valueToInject == null) {
				valueToInject = annotationIn.scope().get(name);
			}

			if (valueToInject == null) {
				if (annotationIn.required()) {
					throw new IllegalArgumentException("The value to inject in the " + element.getClass() + " <" + element.getName() + "> can not be null");
				}
				else if (annotationIn.create()) {
					// create an instance of the type that must be injected
					try {
						valueToInject = classToInject.newInstance();
					} catch (InstantiationException e) {
						throw new StrutsException("An instantiation exception happened when trying to instantiate an object of class <" + classToInject.getName() +"> during injection", e);
					} catch (IllegalAccessException e) {
						throw new StrutsException("An illegal access happened when trying to instantiate an object of class <" + classToInject.getName() +"> during injection", e);
					}
				}
			}

			inject(element, action, valueToInject);
		}
	}

	@Override
	public void doOutjections(Object action, Class<?> actionClass) {
		final List<T> elements = getElementsWithAnnotation(Out.class, actionClass);
		for(final T element : elements) {
			// allow to set private and protected fields
			element.setAccessible(true);
			final Out annotationOut = element.getAnnotation(Out.class);
			
			final String name = resolveName(annotationOut.value(), element);
			
			final Object valueToOutject = getValueToOutject(element, action);

			if (annotationOut.required() && valueToOutject == null) {
				throw new IllegalArgumentException("The value to outject from the "+element.getClass().getSimpleName()+" <" + element.getName() + "> can not be null");
			}

			annotationOut.scope().put(name, valueToOutject);
		}
	}
	
	protected Object injectSpecialObjectByType(final Class<?> type) {
		if (type.isAssignableFrom(ServletContext.class)) {
			return ServletActionContext.getServletContext();
		}
		else if (type.isAssignableFrom(HttpSession.class)) {
			return ServletActionContext.getRequest().getSession(false);
		}
		else if (type.isAssignableFrom(Conversation.class)) {
			return ConversationUtils.getCurrentConversation();
		}
		else if (type.isAssignableFrom(HttpServletRequest.class)) {
			return ServletActionContext.getRequest();
		}
		return null;
	}
	
	// general
	protected abstract List<T> getElementsWithAnnotation(final Class<? extends Annotation> annotationClass, final Class<?> actionClass);

	protected abstract String resolveName(String valueDeclaredInAnnotation, T element);

	// injection related
	protected abstract Class<?> getClassToInject(T element);

	protected abstract void inject(T element, Object action, Object valueToInject);

	// outjection related
	protected abstract Object getValueToOutject(T element, Object action);

}
