package com.hexaid.struts2.bijection;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.StrutsException;

import com.opensymphony.xwork2.util.AnnotationUtils;

/**
 * @author Gabriel Belingueres
 *
 */
public class MethodBijector extends AbstractBijector<Method> {

	@Override
	protected List<Method> getElementsWithAnnotation(	Class<? extends Annotation> annotationClass,
														Class<?> actionClass) {
		final List<Method> allOutMethods = new ArrayList<Method>();
		AnnotationUtils.addAllMethods(annotationClass, actionClass, allOutMethods);
		return allOutMethods;
	}

	@Override
	protected String resolveName(String valueDeclaredInAnnotation, Method element) {
		String name = null;
		if (StringUtils.isEmpty(valueDeclaredInAnnotation)) {
			name = AnnotationUtils.resolvePropertyName(element);
		}
		else {
			name = valueDeclaredInAnnotation;
		}

		if (name == null) {
			throw new IllegalArgumentException("The method <"+ element.toGenericString() +"> can not be injected since there is no suitable name found");
		}
		
		return name;
	}

	@Override
	protected Class<?> getClassToInject(Method element) {
		return element.getParameterTypes()[0];
	}

	@Override
	protected void inject(Method element, Object action, Object valueToInject) {
		try {
			element.invoke(action, valueToInject);
		} catch (IllegalArgumentException e) {
			throw new StrutsException("An illegal argument to the method <" + element.toGenericString() +"> has been tried on injection", e);
		} catch (IllegalAccessException e) {
			throw new StrutsException("An illegal access to the method <" + element.toGenericString() +"> has occurred on injection", e);
		} catch (InvocationTargetException e) {
			throw new StrutsException("An exception has been throwed by the method <" + element.toGenericString() +"> on injection", e);
		}
	}

	@Override
	protected Object getValueToOutject(Method element, Object action) {
		try {
			return element.invoke(action, (Object[])null);
		} catch (IllegalArgumentException e) {
			throw new StrutsException("An illegal argument to the method <" + element.getName() +"> has been tried on outjection", e);
		} catch (IllegalAccessException e) {
			throw new StrutsException("An illegal access to the method <" + element.getName() +"> has occurred on outjection", e);
		} catch (InvocationTargetException e) {
			throw new StrutsException("An exception has been throwed by the method <" + element.getName() +"> on outjection", e);
		}
	}

}
