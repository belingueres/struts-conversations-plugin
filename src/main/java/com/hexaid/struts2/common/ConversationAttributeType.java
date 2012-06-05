package com.hexaid.struts2.common;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.hexaid.struts2.conversations.Conversation;
import com.hexaid.struts2.conversations.ConversationFactory;

/**
 * @author Gabriel Belingueres
 *
 */
public enum ConversationAttributeType {
	
	/**
	 * Use REQUIRED when a new conversation needs to be created if none is present.
	 */
	REQUIRED {
		@Override
		public Conversation process(final ConversationFactory conversationFactory,
									final Map<String, Conversation> allConversationsMap,
									final String conversationId,
									final boolean hasNaturalIdExpression) {
			// check the conversationId parameter
			if (StringUtils.isBlank(conversationId)) {
				return REQUIRES_NEW.process(conversationFactory, allConversationsMap, conversationId, hasNaturalIdExpression);
			} else {
				// conversationID was present => get the conversation
				return allConversationsMap.get(conversationId);
			}
		}
	},
	
	/**
	 * Use REQUIRES_NEW when a new conversation always needs to be created.
	 */
	REQUIRES_NEW {
		@Override
		public Conversation process(final ConversationFactory conversationFactory,
									final Map<String, Conversation> allConversationsMap,
									final String conversationId,
									final boolean hasNaturalIdExpression) {
			// creates a new conversation
			if (hasNaturalIdExpression) {
				// the id will be resolved LATER!
				return conversationFactory.createNaturalConversation();
			}
			else {
				return conversationFactory.createConversation();
			}
		}
	},
	
	/**
	 * Use MANDATORY when a conversation must be already created.
	 */
	MANDATORY {
		@Override
		public Conversation process(final ConversationFactory conversationFactory,
									final Map<String, Conversation> allConversationsMap,
									final String conversationId,
									final boolean hasNaturalIdExpression) {
			// just get the conversation from the Map
			if (StringUtils.isBlank(conversationId)) {
				return null;
			}
			return allConversationsMap.get(conversationId);
		}
	},

	SUPPORTS {
        @Override
        public Conversation process(final ConversationFactory conversationFactory,
                                    final Map<String, Conversation> allConversationsMap,
									final String conversationId,
									final boolean hasNaturalIdExpression) {
            return MANDATORY.process(conversationFactory, allConversationsMap, conversationId, hasNaturalIdExpression);
        }
	},
	
	/**
	 * Use NONE when a conversation must NOT be created.
	 */
	NONE {
		@Override
		public Conversation process(final ConversationFactory conversationFactory,
				Map<String, Conversation> allConversationsMap,
				final String conversationId,
				final boolean hasNaturalIdExpression) {
			throw new IllegalStateException("Should not reach here");
		}
	};
	
	public abstract Conversation process(final ConversationFactory conversationFactory,
										 final Map<String, Conversation> allConversationsMap, 
										 final String conversationId,
										 final boolean hasNaturalIdExpression);
}
