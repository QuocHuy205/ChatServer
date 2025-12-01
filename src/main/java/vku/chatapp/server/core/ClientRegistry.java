package vku.chatapp.server.core;

import vku.chatapp.common.dto.PeerInfo;
import vku.chatapp.server.database.dao.FriendDAO;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ClientRegistry {
    private static ClientRegistry instance;
    private final Map<Long, PeerInfo> onlinePeers;
    private final FriendDAO friendDAO;

    private ClientRegistry() {
        this.onlinePeers = new ConcurrentHashMap<>();
        this.friendDAO = new FriendDAO();
        startHeartbeatMonitor();
    }

    public static ClientRegistry getInstance() {
        if (instance == null) {
            synchronized (ClientRegistry.class) {
                if (instance == null) {
                    instance = new ClientRegistry();
                }
            }
        }
        return instance;
    }

    public boolean registerPeer(PeerInfo peerInfo) {
        if (peerInfo == null || peerInfo.getUserId() == null) {
            return false;
        }

        peerInfo.setLastHeartbeat(System.currentTimeMillis());
        onlinePeers.put(peerInfo.getUserId(), peerInfo);
        System.out.println("Peer registered: User " + peerInfo.getUserId());
        return true;
    }

    public boolean unregisterPeer(Long userId) {
        if (userId == null) {
            return false;
        }

        PeerInfo removed = onlinePeers.remove(userId);
        if (removed != null) {
            System.out.println("Peer unregistered: User " + userId);
            return true;
        }
        return false;
    }

    public PeerInfo getPeerInfo(Long userId) {
        return onlinePeers.get(userId);
    }

    public List<PeerInfo> getOnlineFriends(Long userId) {
        try {
            List<Long> friendIds = friendDAO.getFriendIds(userId);
            return friendIds.stream()
                    .map(onlinePeers::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            System.err.println("Error getting online friends: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean updateHeartbeat(Long userId) {
        PeerInfo peer = onlinePeers.get(userId);
        if (peer != null) {
            peer.setLastHeartbeat(System.currentTimeMillis());
            return true;
        }
        return false;
    }

    private void startHeartbeatMonitor() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                long timeout = 60000; // 60 seconds

                List<Long> timedOutUsers = onlinePeers.entrySet().stream()
                        .filter(e -> now - e.getValue().getLastHeartbeat() > timeout)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());

                timedOutUsers.forEach(userId -> {
                    onlinePeers.remove(userId);
                    System.out.println("Peer timed out: User " + userId);
                });
            }
        }, 30000, 30000); // Check every 30 seconds
    }

    public int getOnlineCount() {
        return onlinePeers.size();
    }
}