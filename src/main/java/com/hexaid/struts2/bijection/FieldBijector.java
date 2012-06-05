package com.hexaid.struts2.bijection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.StrutsException;

import com.opensymphony.xwork2.util.AnnotationUtils;

/**
 * @author Gabriel Belingueres
 *
 */
public class FieldBijector extends AbstractBijector<Field> {

	@Override
	protected List<Field> getElementsWithAnnotation(Class<? extends Annotation> annotationClass,
													Class<?> actionClass) {
		final List<Field> allInFields = new ArrayList<Field>();
		AnnotationUtils.addAllFields(annotationClass, actionClass, allInFields);
		return allInFields;
	}

	@Override
	protected String resolveName(String valueDeclaredInAnnotation, Field element) {
		return StringUtils.isEmpty(valueDeclaredInAnnotation) ? element.getName() : valueDeclaredInAnnotation;
	}

	@Override
	protected Class<?> getClassToInject(Field element) {
		return element.getType();
	}

	@Override
	protected void inject(Field element, Object action, Object valueToInject) {
		try {
			element.set(action, valueToInject);
		} catch (IllegalArgumentException e) {
			throw new StrutsException("An illegal argument to the field <" + element.getName() +"> has been tried on injection", e);
		} catch (IllegalAccessException e) {
			throw new StrutsException("An illegal access to the field <" + element.getName() +"> has occurred on injection", e);
		}
	}

	@Override
	protected Object getValueToOutject(Field element, Object action) {
		try {
			return element.get(action);
		} catch (IllegalArgumentException e) {
			throw new StrutsException("An illegal argument to the field <" + element.getName() +"> has been tried on outjection", e);
		} catch (IllegalAccessException e) {
			throw new StrutsException("An illegal access to the field <" + element.getName() +"> has occurred on outjection", e);
		}
	}

}
