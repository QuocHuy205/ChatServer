// FILE: vku/chatapp/server/service/PeerDiscoveryServiceImpl.java
// ✅ ADD: getPeerInfo implementation

package vku.chatapp.server.rmi;

import vku.chatapp.common.dto.PeerInfo;
import vku.chatapp.common.rmi.IPeerDiscoveryService;
import vku.chatapp.server.database.dao.FriendDAO;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PeerDiscoveryServiceImpl extends UnicastRemoteObject implements IPeerDiscoveryService {

    private final Map<Long, PeerInfo> onlinePeers;
    private final Map<Long, Long> lastHeartbeat;
    private final FriendDAO friendDAO;

    private static final long HEARTBEAT_TIMEOUT = 60000; // 60 seconds

    public PeerDiscoveryServiceImpl() throws RemoteException {
        super();
        this.onlinePeers = new ConcurrentHashMap<>();
        this.lastHeartbeat = new ConcurrentHashMap<>();
        this.friendDAO = new FriendDAO();

        // Start cleanup thread
        startCleanupThread();
    }

    @Override
    public boolean registerPeer(PeerInfo peerInfo) throws RemoteException {
        try {
            onlinePeers.put(peerInfo.getUserId(), peerInfo);
            lastHeartbeat.put(peerInfo.getUserId(), System.currentTimeMillis());

            System.out.println("📡 Peer registered: " + peerInfo.getUserId() +
                    " at " + peerInfo.getAddress() + ":" + peerInfo.getPort());

            return true;
        } catch (Exception e) {
            System.err.println("❌ Error registering peer: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean unregisterPeer(Long userId) throws RemoteException {
        try {
            onlinePeers.remove(userId);
            lastHeartbeat.remove(userId);

            System.out.println("📡 Peer unregistered: " + userId);

            return true;
        } catch (Exception e) {
            System.err.println("❌ Error unregistering peer: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<PeerInfo> getOnlineFriends(Long userId) throws RemoteException {
        try {
            List<Long> friendIds = friendDAO.getFriendIds(userId);
            List<PeerInfo> onlineFriends = new ArrayList<>();

            for (Long friendId : friendIds) {
                PeerInfo peerInfo = onlinePeers.get(friendId);
                if (peerInfo != null && !isTimedOut(friendId)) {
                    onlineFriends.add(peerInfo);
                }
            }

            System.out.println("📡 Found " + onlineFriends.size() + " online friends for user " + userId);

            return onlineFriends;

        } catch (Exception e) {
            System.err.println("❌ Error getting online friends: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public boolean updateHeartbeat(Long userId) throws RemoteException {
        try {
            if (onlinePeers.containsKey(userId)) {
                lastHeartbeat.put(userId, System.currentTimeMillis());
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("❌ Error updating heartbeat: " + e.getMessage());
            return false;
        }
    }

    /**
     * ✅ NEW: Get specific peer info
     */
    @Override
    public PeerInfo getPeerInfo(Long userId) throws RemoteException {
        try {
            PeerInfo peerInfo = onlinePeers.get(userId);

            if (peerInfo != null && !isTimedOut(userId)) {
                System.out.println("✅ Returning peer info for " + userId +
                        ": " + peerInfo.getAddress() + ":" + peerInfo.getPort());
                return peerInfo;
            }

            System.out.println("⚠️ Peer not found or timed out: " + userId);
            return null;

        } catch (Exception e) {
            System.err.println("❌ Error getting peer info: " + e.getMessage());
            return null;
        }
    }

    private boolean isTimedOut(Long userId) {
        Long lastBeat = lastHeartbeat.get(userId);
        if (lastBeat == null) return true;

        return System.currentTimeMillis() - lastBeat > HEARTBEAT_TIMEOUT;
    }

    private void startCleanupThread() {
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30000); // Run every 30 seconds

                    // Remove timed out peers
                    List<Long> timedOutPeers = new ArrayList<>();

                    for (Long userId : onlinePeers.keySet()) {
                        if (isTimedOut(userId)) {
                            timedOutPeers.add(userId);
                        }
                    }

                    for (Long userId : timedOutPeers) {
                        onlinePeers.remove(userId);
                        lastHeartbeat.remove(userId);
                        System.out.println("🧹 Removed timed out peer: " + userId);
                    }

                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    System.err.println("❌ Error in cleanup thread: " + e.getMessage());
                }
            }
        });

        cleanupThread.setDaemon(true);
        cleanupThread.start();
        System.out.println("✅ Peer cleanup thread started");
    }
}