package com.hexaid.struts2.interceptor;

import com.hexaid.struts2.conversations.Conversation;
import com.hexaid.struts2.persistence.impl.PersistenceTransactionManagerAdapter;

/**
 * @author Gabriel Belingueres
 */
public class TestPersistenceTransactionManager extends PersistenceTransactionManagerAdapter {

  @Override
  public void conversationEnding(Conversation conversation, boolean commit) {
    if (commit) {
      String id = conversation.getId();
      if (id.equals("1")) {
        // procesamiento normal
      }
      else if (id.equals("2")) {
        throw new IllegalStateException("This is a RuntimeException subclass for testing only");
      }
    }
  }

}
