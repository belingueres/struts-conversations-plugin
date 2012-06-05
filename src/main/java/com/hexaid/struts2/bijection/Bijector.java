package com.hexaid.struts2.bijection;

public interface Bijector {

	public void doOutjections(Object action, Class<?> actionClass);

	public void doInjections(Object action, Class<?> actionClass);

}