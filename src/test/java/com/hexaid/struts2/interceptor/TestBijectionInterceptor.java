package com.hexaid.struts2.interceptor;

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.StrutsException;
import org.apache.struts2.StrutsJUnit4TestCase;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;

import com.hexaid.struts2.annotations.In;
import com.hexaid.struts2.annotations.Out;
import com.hexaid.struts2.bijection.FieldBijector;
import com.hexaid.struts2.bijection.MethodBijector;
import com.hexaid.struts2.common.ScopeType;
import com.hexaid.struts2.conversations.Conversation;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.interceptor.annotations.After;
import com.opensymphony.xwork2.mock.MockActionInvocation;

/**
 * @author Gabriel Belingueres
 *
 */
public class TestBijectionInterceptor extends StrutsJUnit4TestCase {
	
	MockActionInvocation invocation;
	ActionContext context;
	BijectionInterceptor interceptor;
	Map<String, Object> session;
	
	@Before
	public void setUp() throws Exception {
		invocation = new MockActionInvocation();
		
		context = new ActionContext(new HashMap<String, Object>());
		ActionContext.setContext(context);
		context.setContextMap(new HashMap<String, Object>());
		session = new HashMap<String, Object>();
		context.setSession(session);
		context.setActionInvocation(invocation);
		invocation.setInvocationContext(context);

		interceptor = new BijectionInterceptor();
		
		// set injectors
		interceptor.setFieldBijector(new FieldBijector());
		interceptor.setMethodBijector(new MethodBijector());
	}
	
	@After
	public void tearDown() {
		invocation = null;
		ActionContext.setContext(null);
		context = null;
		interceptor = null;
		session = null;
	}

	@Test
	public void testInterceptAtInAtSessionScope() {
		TestActionSessionScope action = new TestActionSessionScope();
		action.n = 5;
		session.put("n", 8);

		invocation.setAction(action);

		callInterceptor();
		
		Map<String, Object> sessionMap = invocation.getInvocationContext().getSession();
		assertEquals("los objetos session son distintos", sessionMap, session);
		
		Integer nExpected = (Integer) sessionMap.get("n");
		assertNotNull(nExpected);
		assertEquals("El valor en session es incorrecto", 8, nExpected.intValue());
		
		assertEquals("El valor injectado es incorrecto", 8, action.n.intValue());
	}

	@Test
	public void testInterceptAtInAtSessionScopeWithMethods() {
		TestActionSessionScopeMethods action = new TestActionSessionScopeMethods();
		action.setN(5);
		session.put("n", 8);

		invocation.setAction(action);

		callInterceptor();
		
		Map<String, Object> sessionMap = invocation.getInvocationContext().getSession();
		assertEquals("los objetos session son distintos", sessionMap, session);
		
		Integer nExpected = (Integer) sessionMap.get("n");
		assertNotNull(nExpected);
		assertEquals("El valor en session es incorrecto", 8, nExpected.intValue());
		
		assertEquals("El valor injectado es incorrecto", 8, action.getN().intValue());
	}

	@Test
	public void testInterceptAtOutAtSessionScope() {
		TestActionSessionScope action = new TestActionSessionScope();
		action.msg = "hello";
		session.put("msg", "world");

		invocation.setAction(action);

		callInterceptor();
		
		Map<String, Object> sessionMap = invocation.getInvocationContext().getSession();
		assertEquals("los objetos session son distintos", sessionMap, session);
		
		String msgExpected = (String) sessionMap.get("msg");
		
		assertEquals("El valor outjectado es incorrecto", "hello", msgExpected);
	}

	@Test
	public void testInterceptAtOutAtSessionScopeWithMethods() {
		TestActionSessionScopeMethods action = new TestActionSessionScopeMethods();
		action.setMsg("hello");
		session.put("msg", "world");

		invocation.setAction(action);

		callInterceptor();
		
		Map<String, Object> sessionMap = invocation.getInvocationContext().getSession();
		assertEquals("los objetos session son distintos", sessionMap, session);
		
		String msgExpected = (String) sessionMap.get("msg");
		
		assertEquals("El valor outjectado es incorrecto", "hello", msgExpected);
	}

	@Test
	public void testInterceptAtInAtConversationScope() {
		TestActionConversationScope action = new TestActionConversationScope();
		action.n = null;

		invocation.setAction(action);

		HttpSession httpSession = new MockHttpSession();
		
		// set conversationId parameter
		String conversationId = "1234";
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter(Conversation.CONVERSATION_ID_PARAM, conversationId);
		request.setSession(httpSession);
		ServletActionContext.setRequest(request);
		
		// value to inject
		Integer injectedValue = 33;
		Conversation conversation = new Conversation(conversationId);
		conversation.getMap().put("n", injectedValue);
		Map<String,Conversation> conversationsMap = new HashMap<String, Conversation>();
		conversationsMap.put(conversationId, conversation);
		httpSession.setAttribute(Conversation.CONVERSATIONS_MAP_KEY, conversationsMap);
		
		callInterceptor();
		
		assertEquals("El valor injectado es incorrecto", injectedValue, action.n);
	}

	@Test
	public void testInterceptAtInAtConversationScopeWithSuperclass() {
		TestActionWithSuperclass action = new TestActionWithSuperclass();
		action.n = null;
		action.nSuper = null;

		invocation.setAction(action);

		HttpSession httpSession = new MockHttpSession();
		
		// set conversationId parameter
		String conversationId = "1234";
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter(Conversation.CONVERSATION_ID_PARAM, conversationId);
		request.setSession(httpSession);
		ServletActionContext.setRequest(request);
		
		// value to inject
		Integer injectedValue = 33;
		Integer injectedSuperValue=55;
		Conversation conversation = new Conversation(conversationId);
		conversation.getMap().put("n", injectedValue);
		conversation.getMap().put("nSuper", injectedSuperValue);
		Map<String,Conversation> conversationsMap = new HashMap<String, Conversation>();
		conversationsMap.put(conversationId, conversation);
		httpSession.setAttribute(Conversation.CONVERSATIONS_MAP_KEY, conversationsMap);
		
		callInterceptor();
		
		assertEquals("El valor injectado es incorrecto", injectedValue, action.n);
		assertEquals("El valor injectado es incorrecto", injectedSuperValue, action.nSuper);
	}

	@Test
	public void testInterceptAtInAtConversationScopeWithSuperclassAndMethods() {
		TestActionWithSuperclassAndMethods action = new TestActionWithSuperclassAndMethods();
		// set properties to inject in superclass and subclass 
		action.setN(null);
		action.setIntProperty(null);

		invocation.setAction(action);

		HttpSession httpSession = new MockHttpSession();
		
		// set conversationId parameter
		String conversationId = "1234";
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter(Conversation.CONVERSATION_ID_PARAM, conversationId);
		request.setSession(httpSession);
		ServletActionContext.setRequest(request);
		
		Conversation conversation = new Conversation(conversationId);

		// value to inject
		Integer injectedValue = 33;
		Integer injectedSuperValue = 51;
		conversation.getMap().put("intProperty", injectedValue);
		session.put("n", injectedSuperValue);

		Map<String,Conversation> conversationsMap = new HashMap<String, Conversation>();
		conversationsMap.put(conversationId, conversation);
		httpSession.setAttribute(Conversation.CONVERSATIONS_MAP_KEY, conversationsMap);
		
		callInterceptor();
		
		assertEquals("El valor injectado es incorrecto", injectedValue, action.getIntProperty());
		assertEquals("El valor injectado es incorrecto", injectedSuperValue, action.getN());
	}


	@Test
	public void testInterceptAtInAtConversationScopeWithSuperclassPrivate() {
		TestActionWithSuperclassPrivate action = new TestActionWithSuperclassPrivate();
		action.n = null;
		action.setnSuper(null);

		invocation.setAction(action);

		HttpSession httpSession = new MockHttpSession();
		
		// set conversationId parameter
		String conversationId = "1234";
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter(Conversation.CONVERSATION_ID_PARAM, conversationId);
		request.setSession(httpSession);
		ServletActionContext.setRequest(request);
		
		// value to inject
		Integer injectedValue = 33;
		Integer injectedSuperValue=55;
		Conversation conversation = new Conversation(conversationId);
		conversation.getMap().put("n", injectedValue);
		conversation.getMap().put("nSuper", injectedSuperValue);
		Map<String,Conversation> conversationsMap = new HashMap<String, Conversation>();
		conversationsMap.put(conversationId, conversation);
		httpSession.setAttribute(Conversation.CONVERSATIONS_MAP_KEY, conversationsMap);
		
		callInterceptor();
		
		assertEquals("El valor injectado es incorrecto", injectedValue, action.n);
		assertEquals("El valor injectado es incorrecto", injectedSuperValue, action.getnSuper());
	}
	
	@Test
	public void testInterceptAtInAtOutWithPrivateMethods() {
		TestActionWithPrivateMethods action = new TestActionWithPrivateMethods();
		
		invocation.setAction(action);
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setAttribute("privateProperty", 5);
		request.setSession(new MockHttpSession());
		ServletActionContext.setRequest(request);

		callInterceptor();
		
		assertEquals(5+18, request.getAttribute("privateProperty"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInterceptActionWithWrongInMethodNames() throws Exception {
		TestActionWithWrongInMethodNames action = new TestActionWithWrongInMethodNames();
		
		invocation.setAction(action);
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setAttribute("__not_a_setter", 5);
		request.setSession(new MockHttpSession());
		ServletActionContext.setRequest(request);

		interceptor.init();
		interceptor.intercept(invocation);
		interceptor.destroy();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testInterceptActionWithWrongOutMethodNames() throws Exception {
		TestActionWithWrongOutMethodNames action = new TestActionWithWrongOutMethodNames();
		
		invocation.setAction(action);
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setAttribute("__not_a_getter", 5);
		request.setSession(new MockHttpSession());
		ServletActionContext.setRequest(request);

		interceptor.init();
		interceptor.intercept(invocation);
		interceptor.destroy();
	}

	@Test
	public void testInterceptAtInAtOutWithDifferentNames() {
		TestActionWithDifferentNames action = new TestActionWithDifferentNames();

		invocation.setAction(action);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setAttribute("field-in", 5);
		request.setAttribute("method-in", 9);
		request.setSession(new MockHttpSession());
		ServletActionContext.setRequest(request);

		callInterceptor();
		
		assertEquals(5, action.n1.intValue());
		
		Integer n1Out = (Integer) session.get("field-out");
		assertEquals(5, n1Out.intValue());
		
		assertEquals(9, action.getN2().intValue());
		
		Integer n2Out = (Integer) request.getAttribute("method-out");
		assertEquals(9, n2Out.intValue());
	}

	@Test
	public void testInterceptActionWithRequiredOutFieldButIsNotNull() throws Exception {
		TestActionRequiredFieldOut action = new TestActionRequiredFieldOut();

		invocation.setAction(action);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(new MockHttpSession());
		ServletActionContext.setRequest(request);

		callInterceptor();
		
		// should not be deleted from request
		Integer out = (Integer) request.getAttribute("n");
		assertEquals(5, out.intValue());
	}

	@Test
	public void testInterceptActionWithRequiredInFieldButIsNotNull() throws Exception {
		TestActionRequiredFieldIn action = new TestActionRequiredFieldIn();

		invocation.setAction(action);

		MockHttpServletRequest request = new MockHttpServletRequest();
		// not null
		request.setAttribute("n", 5);
		request.setSession(new MockHttpSession());
		ServletActionContext.setRequest(request);

		callInterceptor();
		
		// should not be deleted from request
		assertEquals(5, action.n.intValue());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testInterceptActionWithRequiredInFieldButIsNull() throws Exception {
		Object action = new Object() {
			@In(required=true, scope=ScopeType.REQUEST)
			private Integer n;
		};

		invocation.setAction(action);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(new MockHttpSession());
		ServletActionContext.setRequest(request);

		interceptor.init();
		interceptor.intercept(invocation);
		interceptor.destroy();
	}

	@Test
	public void testInterceptActionWithRequiredOutMethodButIsNotNull() throws Exception {
		TestActionRequiredMethodOut action = new TestActionRequiredMethodOut();

		invocation.setAction(action);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(new MockHttpSession());
		ServletActionContext.setRequest(request);

		callInterceptor();
		
		// should not be deleted from request
		Integer out = (Integer) request.getAttribute("data");
		assertEquals(5, out.intValue());
	}

	@Test
	public void testInterceptActionWithRequiredInMethodButIsNotNull() throws Exception {
		TestActionRequiredMethodIn action = new TestActionRequiredMethodIn();

		invocation.setAction(action);

		MockHttpServletRequest request = new MockHttpServletRequest();
		// not null
		request.setAttribute("data", 5);
		request.setSession(new MockHttpSession());
		ServletActionContext.setRequest(request);

		callInterceptor();
		
		// should not be deleted from request
		assertEquals(5, action.getData().intValue());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testInterceptActionWithRequiredInMethodButIsNull() throws Exception {
		Object action = new Object() {
			@In(required=true, scope=ScopeType.REQUEST)
			public void setData(Integer n) {
				// nothing
			}
		};

		invocation.setAction(action);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(new MockHttpSession());
		ServletActionContext.setRequest(request);

		interceptor.init();
		interceptor.intercept(invocation);
		interceptor.destroy();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testInterceptActionWithRequiredOutFieldButIsNull() throws Exception {
		Object action = new Object() {
			@Out(required=true, scope=ScopeType.REQUEST)
			private Integer n;
		};

		invocation.setAction(action);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(new MockHttpSession());
		ServletActionContext.setRequest(request);

		interceptor.init();
		interceptor.intercept(invocation);
		interceptor.destroy();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testInterceptActionWithRequiredOutMethodButIsNull() throws Exception {
		Object action = new Object() {
			@Out(required=true, scope=ScopeType.REQUEST)
			public Integer getData() {
				return null;
			}
		};

		invocation.setAction(action);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(new MockHttpSession());
		ServletActionContext.setRequest(request);

		interceptor.init();
		interceptor.intercept(invocation);
		interceptor.destroy();
	}
	
	@Test
	public void testInterceptActionWithInAndCreateField() {
		TestActionFieldWithCreate action = new TestActionFieldWithCreate();

		invocation.setAction(action);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(new MockHttpSession());
		ServletActionContext.setRequest(request);

		callInterceptor();
		
		// should be created
		assertNotNull(action.data);
		assertEquals("", action.data);
	}

	@Test(expected=StrutsException.class)
	public void testInterceptActionWithInAndCreateFieldFail() throws Exception {
		TestActionFieldWithCreateFail action = new TestActionFieldWithCreateFail();

		invocation.setAction(action);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(new MockHttpSession());
		ServletActionContext.setRequest(request);

		interceptor.init();
		interceptor.intercept(invocation);
		interceptor.destroy();
	}

	@Test
	public void testInterceptActionWithInAndCreateMethod() {
		TestActionMethodWithCreate action = new TestActionMethodWithCreate();

		invocation.setAction(action);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(new MockHttpSession());
		ServletActionContext.setRequest(request);

		callInterceptor();
		
		// should be created
		assertNotNull(action.getData());
		assertEquals("", action.getData());
	}

	@Test(expected=StrutsException.class)
	public void testInterceptActionWithInAndCreateMethodFail() throws Exception {
		TestActionMethodWithCreateFail action = new TestActionMethodWithCreateFail();

		invocation.setAction(action);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(new MockHttpSession());
		ServletActionContext.setRequest(request);

		interceptor.init();
		interceptor.intercept(invocation);
		interceptor.destroy();
	}
	
	@Test
	public void testInterceptorActionInjectSpecialFields() {
		TestActionSpecialFields action = new TestActionSpecialFields();
		
		invocation.setAction(action);

		HttpSession httpSession = new MockHttpSession();
		
		// set conversationId parameter
		String conversationId = "1234";
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter(Conversation.CONVERSATION_ID_PARAM, conversationId);
		request.setSession(httpSession);
		ServletActionContext.setRequest(request);
		
		ServletContext servletContext = new MockServletContext();
		ServletActionContext.setServletContext(servletContext);
		
		// value to inject
		Integer injectedValue = 33;
		Integer injectedSuperValue=55;
		Conversation conversation = new Conversation(conversationId);
		conversation.getMap().put("n", injectedValue);
		conversation.getMap().put("nSuper", injectedSuperValue);
		Map<String,Conversation> conversationsMap = new HashMap<String, Conversation>();
		conversationsMap.put(conversationId, conversation);
		httpSession.setAttribute(Conversation.CONVERSATIONS_MAP_KEY, conversationsMap);
		
		callInterceptor();

		assertNotNull(action.request);
		assertEquals(request, action.request);
		
		assertNotNull(action.conversation);
		assertEquals(conversation, action.conversation);
		
		assertNotNull(action.session);
		assertEquals(httpSession, action.session);
		
		assertNotNull(action.context);
		assertEquals(ServletActionContext.getServletContext(), action.context);
	}

	@Test
	public void testInterceptorActionInjectSpecialMethods() {
		TestActionSpecialMethods action = new TestActionSpecialMethods();
		
		invocation.setAction(action);

		HttpSession httpSession = new MockHttpSession();
		
		// set conversationId parameter
		String conversationId = "1234";
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter(Conversation.CONVERSATION_ID_PARAM, conversationId);
		request.setSession(httpSession);
		ServletActionContext.setRequest(request);
		
		ServletContext servletContext = new MockServletContext();
		ServletActionContext.setServletContext(servletContext);
		
		// value to inject
		Integer injectedValue = 33;
		Integer injectedSuperValue=55;
		Conversation conversation = new Conversation(conversationId);
		conversation.getMap().put("n", injectedValue);
		conversation.getMap().put("nSuper", injectedSuperValue);
		Map<String,Conversation> conversationsMap = new HashMap<String, Conversation>();
		conversationsMap.put(conversationId, conversation);
		httpSession.setAttribute(Conversation.CONVERSATIONS_MAP_KEY, conversationsMap);
		
		callInterceptor();

		assertNotNull(action.getRequest());
		assertEquals(request, action.getRequest());
		
		assertNotNull(action.getConversation());
		assertEquals(conversation, action.getConversation());
		
		assertNotNull(action.getSession());
		assertEquals(httpSession, action.getSession());
		
		assertNotNull(action.getContext());
		assertEquals(ServletActionContext.getServletContext(), action.getContext());
	}
	
	@Test
	public void testActionWithCookieInjection() {
		TestActionWithCookieIn action = new TestActionWithCookieIn();

		invocation.setAction(action);

		HttpSession httpSession = new MockHttpSession();
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(httpSession);
		ServletActionContext.setRequest(request);

		Cookie testCookie = new Cookie("mycookie", "somevalue");
		request.setCookies(new Cookie[] {testCookie});
		
		callInterceptor();
		
		assertNotNull(action.mycookie);
		assertEquals("somevalue", action.mycookie.getValue());
	}

	@Test
	public void testActionWithCookieOutjectionNotSet() {
		TestActionWithCookieOut action = new TestActionWithCookieOut();

		invocation.setAction(action);

		HttpSession httpSession = new MockHttpSession();
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(httpSession);
		ServletActionContext.setRequest(request);
		
		MockHttpServletResponse response = new MockHttpServletResponse();
		ServletActionContext.setResponse(response);
		
		callInterceptor();
		
		assertNotNull(response.getCookies());
		assertEquals(0, response.getCookies().length);
	}

	@Test
	public void testActionWithCookieOutjection() {
		TestActionWithCookieOut action = new TestActionWithCookieOut();
		
		action.mycookie = new Cookie("mycookie", "somevalue");
		
		action.mycookieValue = "hello";

		invocation.setAction(action);

		HttpSession httpSession = new MockHttpSession();
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(httpSession);
		ServletActionContext.setRequest(request);
		
		MockHttpServletResponse response = new MockHttpServletResponse();
		ServletActionContext.setResponse(response);
		
		callInterceptor();
		
		assertNotNull(response.getCookies());
		assertEquals(2, response.getCookies().length);
		assertEquals("mycookie", response.getCookies()[0].getName());
		assertEquals("somevalue", response.getCookies()[0].getValue());
		assertEquals("mycookieValue", response.getCookies()[1].getName());
		assertEquals("hello", response.getCookies()[1].getValue());
	}

	private void callInterceptor() {
		try {
			interceptor.init();
			interceptor.intercept(invocation);
			interceptor.destroy();
		} catch (Exception e) {
			e.printStackTrace();
			fail("Excepción elevada por el interceptor: " + e.getMessage());
		}
	}

	private static class TestActionSessionScope {
		@In(scope=ScopeType.SESSION)
		private Integer n;
		
		@Out(scope=ScopeType.SESSION)
		private String msg;
	}

	private static class TestActionConversationScope {
		@In
		private Integer n;
		
		@Out
		private String msg;
	}
	
	private static class ActionSuperclass {
		@In
		protected Integer nSuper;
		
		@Out
		protected String msgSuper;
	}

	private static class ActionSuperclassPrivate {
		@In
		private Integer nSuper;
		
		@Out
		private String msgSuper;

		public Integer getnSuper() {
			return nSuper;
		}

		public void setnSuper(Integer nSuper) {
			this.nSuper = nSuper;
		}
	}

	private static class TestActionWithSuperclass extends ActionSuperclass {
		@In
		private Integer n;
		
		@Out
		private String msg;
	}

	private static class TestActionWithSuperclassPrivate extends ActionSuperclassPrivate {
		@In
		private Integer n;
		
		@Out
		private String msg;
	}
	
	private static class TestActionSessionScopeMethods {
		private Integer n;
		private String msg;
		public Integer getN() {
			return n;
		}
		@In(scope=ScopeType.SESSION)
		public void setN(Integer n) {
			this.n = n;
		}
		@Out(scope=ScopeType.SESSION)
		public String getMsg() {
			return msg;
		}
		public void setMsg(String msg) {
			this.msg = msg;
		}
	}
	
	private static class TestActionWithSuperclassAndMethods extends TestActionSessionScopeMethods {
		private Integer intProperty;

		public Integer getIntProperty() {
			return intProperty;
		}

		@In
		public void setIntProperty(Integer intProperty) {
			this.intProperty = intProperty;
		}
	}
	
	private static class TestActionWithPrivateMethods {
		private Integer privateProperty;

		@Out(scope=ScopeType.REQUEST)
		private Integer getPrivateProperty() {
			return 18 + privateProperty;
		}

		@In(scope=ScopeType.REQUEST)
		private void setPrivateProperty(Integer privateProperty) {
			this.privateProperty = privateProperty;
		}
	}
	
	private static class TestActionWithWrongInMethodNames {

		@In(scope=ScopeType.REQUEST)
		public void __not_a_setter(Integer n) {
		}
		
	}

	private static class TestActionWithWrongOutMethodNames {

		@Out(scope=ScopeType.REQUEST)
		public Integer __not_a_getter__() {
			return null;
		}
	}

	private static class TestActionWithDifferentNames {
		@In(value="field-in", scope=ScopeType.REQUEST)
		@Out(value="field-out", scope=ScopeType.SESSION)
		private Integer n1;
		
		private Integer n2;

		@Out(value="method-out", scope=ScopeType.REQUEST)
		public Integer getN2() {
			return n2;
		}

		@In(value="method-in", scope=ScopeType.REQUEST)
		public void setN2(Integer n2) {
			this.n2 = n2;
		}
	}
	
	private static class TestActionRequiredFieldIn {
		@In(required=true, scope=ScopeType.REQUEST)
		private Integer n;
	}

	private static class TestActionRequiredFieldOut {
		@Out(required=true, scope=ScopeType.REQUEST)
		private Integer n = 5;
	}
	
	private static class TestActionRequiredMethodIn {
		private Integer data;

		public Integer getData() {
			return data;
		}

		@In(required=true, scope=ScopeType.REQUEST)
		public void setData(Integer data) {
			this.data = data;
		}
	}

	private static class TestActionRequiredMethodOut {
		private Integer data = 5;

		@Out(required=true, scope=ScopeType.REQUEST)
		public Integer getData() {
			return data;
		}

		@SuppressWarnings("unused")
		public void setData(Integer data) {
			this.data = data;
		}
	}
	
	private static class TestActionFieldWithCreate {
		@In(create=true)
		private String data;
	}

	private static class TestActionFieldWithCreateFail {
		// File doesn't have a no-arg constructor
		@In(create=true)
		private File file;
	}
	
	private static class TestActionMethodWithCreate {
		private String data;

		public String getData() {
			return data;
		}

		@In(create=true)
		public void setData(String data) {
			this.data = data;
		}
	}

	private static class TestActionMethodWithCreateFail {
		private File file;

		@SuppressWarnings("unused")
		public File getFile() {
			return file;
		}

		@In(create=true)
		public void setFile(File file) {
			this.file = file;
		}
	}
	
	private static class TestActionSpecialFields {
		@In
		private HttpServletRequest request;
		@In
		private Conversation conversation;
		@In
		private HttpSession session;
		@In
		private ServletContext context;
	}
	
	private static class TestActionSpecialMethods {
		private HttpServletRequest request;
		private Conversation conversation;
		private HttpSession session;
		private ServletContext context;
		public HttpServletRequest getRequest() {
			return request;
		}
		@In
		public void setRequest(HttpServletRequest request) {
			this.request = request;
		}
		public Conversation getConversation() {
			return conversation;
		}
		@In
		public void setConversation(Conversation conversation) {
			this.conversation = conversation;
		}
		public HttpSession getSession() {
			return session;
		}
		@In
		public void setSession(HttpSession session) {
			this.session = session;
		}
		public ServletContext getContext() {
			return context;
		}
		@In
		public void setContext(ServletContext context) {
			this.context = context;
		}
	}
	
	private static class TestActionWithCookieIn {
		@In(scope=ScopeType.COOKIE)
		private Cookie mycookie;
	}
	
	private static class TestActionWithCookieOut {
		@Out(scope=ScopeType.COOKIE)
		private Cookie mycookie;
		
		@Out(scope=ScopeType.COOKIE)
		private String mycookieValue;
	}

}
