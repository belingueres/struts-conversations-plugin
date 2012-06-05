package com.hexaid.struts2.persistence.impl;

import static org.junit.Assert.*;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hexaid.struts2.conversations.Conversation;
import com.opensymphony.xwork2.ObjectFactory;

/**
 * @author Gabriel Belingueres
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/context-jpa.xml")
public class TestJPASpringPersistenceTransactionManager {
	
	private JPASpringPersistenceTransactionManager persistenceTxManager;
	
	@Autowired
	private JpaTransactionManager transactionManager;
	
	@Autowired
	private DataSource dataSource;

	@Autowired
	private UserDAO userDAO;
	
	private JdbcTemplate jdbcTemplate;

	private Conversation conversation;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		MockObjectFactory objectFactory = new MockObjectFactory(transactionManager);
		persistenceTxManager = new JPASpringPersistenceTransactionManager(objectFactory);
		persistenceTxManager.init();
		
		jdbcTemplate = new JdbcTemplate(dataSource);

		// table is empty
		deleteAllUsers();

		conversation = new Conversation("1");
		
		// TODO: find a better way of doing this!!!
		// Force to unbound the EntityManagerFactory from the current thread
		// (because the tests run ALL in the SAME thread)
		persistenceTxManager.unbind(null);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		persistenceTxManager = null;
	}

	@Test
	public void testConversationStartingSavesEntityManager() {
		assertFalse(persistenceTxManager.isEntityManagerFactoryBound());

		persistenceTxManager.conversationStarting(conversation);
		
		assertNotNull(persistenceTxManager.getEntityManager(conversation));
		assertTrue(persistenceTxManager.isEntityManagerFactoryBound());
	}
	
	@Test
	public void testPersistenceManagerCommitsOnSingleRequest() {
		assertFalse(persistenceTxManager.isEntityManagerFactoryBound());

		persistenceTxManager.conversationStarting(conversation);

		assertTrue(persistenceTxManager.isEntityManagerFactoryBound());

		User user = new User("username");
		userDAO.persist(user);

		// true for COMMIT
		persistenceTxManager.conversationEnding(conversation, true);

		assertEquals(1, countUsers());
		assertNull(persistenceTxManager.getEntityManager(conversation));
		assertFalse(persistenceTxManager.isEntityManagerFactoryBound());
	}

	@Test
	public void testPersistenceManagerRollbackOnSingleRequest() {
		assertFalse(persistenceTxManager.isEntityManagerFactoryBound());

		persistenceTxManager.conversationStarting(conversation);
		
		assertTrue(persistenceTxManager.isEntityManagerFactoryBound());

		User user = new User("username");
		userDAO.persist(user);
		
		// false for ROLLBACK
		persistenceTxManager.conversationEnding(conversation, false);

		assertEquals(0, countUsers());
		assertNull(persistenceTxManager.getEntityManager(conversation));
		assertFalse(persistenceTxManager.isEntityManagerFactoryBound());
	}
	
	@Test
	public void testPersistenceManagerCommitsAfterMultipleRequests() {
		assertFalse(persistenceTxManager.isEntityManagerFactoryBound());

		// first request
		persistenceTxManager.conversationStarting(conversation);
		
		assertTrue(persistenceTxManager.isEntityManagerFactoryBound());

		User user = new User("username");
		userDAO.persist(user);
		
		persistenceTxManager.conversationPaused(conversation);

		assertFalse(persistenceTxManager.isEntityManagerFactoryBound());

		// entity manager remains in the conversation and is open
		EntityManager entityManager = persistenceTxManager.getEntityManager(conversation);
		assertNotNull(entityManager);
		assertTrue(entityManager.isOpen());

		// second request
		assertEquals(0, countUsers());
		
		persistenceTxManager.conversationResumed(conversation);
		
		assertTrue(persistenceTxManager.isEntityManagerFactoryBound());

		User user2 = new User("username2");
		userDAO.persist(user2);
		
		// true for COMMIT
		persistenceTxManager.conversationEnding(conversation, true);

		assertEquals(2, countUsers());
		assertNull(persistenceTxManager.getEntityManager(conversation));
		assertFalse(persistenceTxManager.isEntityManagerFactoryBound());
	}

	@Test
	public void testPersistenceManagerRollbacksAfterMultipleRequests() {
		assertFalse(persistenceTxManager.isEntityManagerFactoryBound());

		// first request
		persistenceTxManager.conversationStarting(conversation);
		
		assertTrue(persistenceTxManager.isEntityManagerFactoryBound());

		User user = new User("username");
		userDAO.persist(user);
		
		persistenceTxManager.conversationPaused(conversation);

		assertFalse(persistenceTxManager.isEntityManagerFactoryBound());

		// entity manager remains in the conversation and is open
		EntityManager entityManager = persistenceTxManager.getEntityManager(conversation);
		assertNotNull(entityManager);
		assertTrue(entityManager.isOpen());

		// second request
		assertEquals(0, countUsers());
		
		persistenceTxManager.conversationResumed(conversation);
		
		assertTrue(persistenceTxManager.isEntityManagerFactoryBound());

		User user2 = new User("username2");
		userDAO.persist(user2);
		
		// false for ROLLBACK
		persistenceTxManager.conversationEnding(conversation, false);

		assertEquals(0, countUsers());
		assertNull(persistenceTxManager.getEntityManager(conversation));
		assertFalse(persistenceTxManager.isEntityManagerFactoryBound());
	}

	@Test
	public void testExceptionThrown() {
		assertFalse(persistenceTxManager.isEntityManagerFactoryBound());

		persistenceTxManager.conversationStarting(conversation);
		
		assertTrue(persistenceTxManager.isEntityManagerFactoryBound());

		User user = new User("username");
		userDAO.persist(user);

		// table remains empty
		assertEquals(0, countUsers());

		persistenceTxManager.exceptionThrown(conversation, new IllegalStateException("test"));

		// table remains empty after exception
		assertEquals(0, countUsers());
		assertNotNull(persistenceTxManager.getEntityManager(conversation));
		assertFalse(persistenceTxManager.isEntityManagerFactoryBound());
	}

	private int countUsers() {
		return jdbcTemplate.queryForInt("select count(*) from user");
	}
	
	private int deleteAllUsers() {
		return jdbcTemplate.update("delete from user");
	}
	
	private static class MockObjectFactory extends ObjectFactory {

		private static final long serialVersionUID = 1L;
		
		private JpaTransactionManager transactionManager;

		public MockObjectFactory(JpaTransactionManager transactionManager) {
			this.transactionManager = transactionManager;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public Object buildBean(Class clazz, Map<String, Object> extraContext)
				throws Exception {
			return transactionManager;
		}
	}
	
	public static class UserDAO {
		@PersistenceContext
		EntityManager em;
		
		public void persist(User user) {
			em.persist(user);
		}
	}

}
