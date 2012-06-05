package com.hexaid.struts2.persistence.impl;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.hexaid.struts2.conversations.Conversation;
import com.hexaid.struts2.persistence.PersistenceTransactionManager;
import com.opensymphony.xwork2.ObjectFactory;
import com.opensymphony.xwork2.inject.Inject;

/**
 * Implements JPA specific code for Conversation Managed Persistence Contexts,
 * where both JPA and Transactions are configured using Spring (requires using
 * the Struts Spring plugin).
 * 
 * @author Gabriel Belingueres
 * 
 */
public class JPASpringPersistenceTransactionManager implements
		PersistenceTransactionManager {

	private final static Logger LOG = LoggerFactory.getLogger(JPASpringPersistenceTransactionManager.class);
	
	public static final String PERSISTENCE_CONTEXT_KEY = "com.hexaid.struts2.conversations.persistence.context";

	private EntityManagerFactory entityManagerFactory;
	private TransactionTemplate transactionTemplate;
	
	private final ObjectFactory objectFactory;

	@Inject
	public JPASpringPersistenceTransactionManager(@Inject ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	@Override
	public void init() {
		try {
			// inject the JPA Transaction Manager
			final JpaTransactionManager transactionManager = (JpaTransactionManager) objectFactory.buildBean(JpaTransactionManager.class, null);

			this.entityManagerFactory = transactionManager.getEntityManagerFactory();
			this.transactionTemplate = new TransactionTemplate(transactionManager);
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not find a Spring's JpaTransactionManager!", e);
		}
	}

	@Override
	public void conversationStarting(final Conversation conversation) {
		final EntityManager em = entityManagerFactory.createEntityManager();
		em.setFlushMode(FlushModeType.COMMIT);
		putEntityManager(conversation, em);
		bind(em);
	}

	@Override
	public void conversationPaused(final Conversation conversation) {
		final EntityManager em = getEntityManager(conversation);
		LOG.debug("Conversation paused with entityManager: {}", em);
		unbind(em);
	}

	@Override
	public void conversationResumed(Conversation conversation) {
		if (!conversation.isEnded()) {
			final EntityManager em = getEntityManager(conversation);
			LOG.debug("Conversation resumed with entityManager: {}", em);
			bind(em);
		}
	}

	@Override
	public void conversationEnding(final Conversation conversation, final boolean commit) {
		final EntityManager em = getEntityManager(conversation);
		if (commit) {
			LOG.debug("Committing transaction with entityManager: {}", em);
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					em.joinTransaction();
				}
			});
		}
		else {
			LOG.debug("NOT Committing transaction with entityManager: {}", em);
		}
		unbind(em);
		removeEntityManager(conversation, em);
		em.close();
	}

	@Override
	public void conversationEnded(Conversation conversation) {
		// NOTHING
	}

	@Override
	public void exceptionThrown(Conversation conversation, Exception exception) {
		final EntityManager em = getEntityManager(conversation);
		unbind(em);
	}

	protected void putEntityManager(final Conversation conversation, final EntityManager em) {
		conversation.getMap().put(PERSISTENCE_CONTEXT_KEY, em);
	}
	
	protected EntityManager getEntityManager(final Conversation conversation) {
		return (EntityManager) conversation.getMap().get(PERSISTENCE_CONTEXT_KEY);
	}

	protected void removeEntityManager(final Conversation conversation, final EntityManager em) {
		conversation.getMap().remove(PERSISTENCE_CONTEXT_KEY);
	}

	protected void bind(final EntityManager em) {
		LOG.debug("Binding entityManager: {}", em);
		TransactionSynchronizationManager.bindResource(entityManagerFactory, new EntityManagerHolder(em));
	}

	protected void unbind(final EntityManager em) {
		LOG.debug("Unbinding entityManager: {}", em);
		if (TransactionSynchronizationManager.hasResource(entityManagerFactory)) {
			TransactionSynchronizationManager.unbindResource(entityManagerFactory);
		}
	}
	
	protected boolean isEntityManagerFactoryBound() {
		return TransactionSynchronizationManager.hasResource(entityManagerFactory);
	}

}
