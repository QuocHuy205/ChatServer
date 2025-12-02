// FILE: vku/chatapp/server/rmi/MessageServiceImpl.java
// ✅ NEW: Server implementation để lưu messages vào database

package vku.chatapp.server.rmi;

import vku.chatapp.common.model.Message;
import vku.chatapp.common.rmi.IMessageService;
import vku.chatapp.server.database.dao.MessageDAO;
import vku.chatapp.common.enums.MessageStatus;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class MessageServiceImpl extends UnicastRemoteObject implements IMessageService {
    private final MessageDAO messageDAO;

    public MessageServiceImpl() throws RemoteException {
        super();
        this.messageDAO = new MessageDAO();
    }

    @Override
    public Message saveMessage(Message message) throws RemoteException {
        try {
            return messageDAO.saveMessage(message);
        } catch (Exception e) {
            System.err.println("❌ Error saving message: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Failed to save message", e);
        }
    }

    @Override
    public List<Message> getConversationHistory(Long user1Id, Long user2Id, int limit) throws RemoteException {
        try {
            return messageDAO.getConversationHistory(user1Id, user2Id, limit);
        } catch (Exception e) {
            System.err.println("❌ Error getting conversation history: " + e.getMessage());
            throw new RemoteException("Failed to get conversation history", e);
        }
    }

    @Override
    public boolean updateMessageStatus(Long messageId, String status) throws RemoteException {
        try {
            MessageStatus messageStatus = MessageStatus.valueOf(status);
            return messageDAO.updateMessageStatus(messageId, messageStatus);
        } catch (Exception e) {
            System.err.println("❌ Error updating message status: " + e.getMessage());
            throw new RemoteException("Failed to update message status", e);
        }
    }

    @Override
    public int getUnreadCount(Long userId) throws RemoteException {
        try {
            // TODO: Implement in MessageDAO
            return 0;
        } catch (Exception e) {
            throw new RemoteException("Failed to get unread count", e);
        }
    }
}