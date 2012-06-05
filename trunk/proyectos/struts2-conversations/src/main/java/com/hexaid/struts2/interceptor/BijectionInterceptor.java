package com.hexaid.struts2.interceptor;

import com.hexaid.struts2.bijection.Bijector;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.opensymphony.xwork2.interceptor.PreResultListener;

/**
 * @author Gabriel Belingueres
 *
 */
public class BijectionInterceptor extends AbstractInterceptor {

	private static final long serialVersionUID = 1L;
	
	private transient Bijector fieldBijector;
	private transient Bijector methodBijector;
	
	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		final Object action = invocation.getAction();
		final Class<? extends Object> actionClass = action.getClass();

		fieldBijector.doInjections(action, actionClass);
		methodBijector.doInjections(action, actionClass);

		invocation.addPreResultListener(new PreResultListener() {
			@Override
			public void beforeResult(ActionInvocation invocation, String resultCode) {
				// perform Outjection
				fieldBijector.doOutjections(action, actionClass);
				methodBijector.doOutjections(action, actionClass);
			}
		});

		return invocation.invoke();
	}

	@Inject("field")
	public void setFieldBijector(Bijector fieldBijector) {
		this.fieldBijector = fieldBijector;
	}

	@Inject("method")
	public void setMethodBijector(Bijector methodBijector) {
		this.methodBijector = methodBijector;
	}

}
