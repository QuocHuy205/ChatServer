// FILE: vku/chatapp/server/core/RMIServer.java
// ✅ FIX: Thêm MessageService vào RMI registry

package vku.chatapp.server.core;

import vku.chatapp.common.constants.AppConstants;
import vku.chatapp.server.rmi.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIServer {
    private Registry registry;

    public void start() throws Exception {
        // Create RMI registry
        registry = LocateRegistry.createRegistry(AppConstants.RMI_PORT);

        // Create and bind services
        AuthServiceImpl authService = new AuthServiceImpl();
        registry.rebind(AppConstants.RMI_AUTH_SERVICE, authService);

        UserServiceImpl userService = new UserServiceImpl();
        registry.rebind(AppConstants.RMI_USER_SERVICE, userService);

        FriendServiceImpl friendService = new FriendServiceImpl();
        registry.rebind(AppConstants.RMI_FRIEND_SERVICE, friendService);

        PeerDiscoveryServiceImpl peerService = new PeerDiscoveryServiceImpl();
        registry.rebind(AppConstants.RMI_PEER_DISCOVERY_SERVICE, peerService);

        // ✅ NEW: Register MessageService
        MessageServiceImpl messageService = new MessageServiceImpl();
        registry.rebind("MessageService", messageService);

        System.out.println("✅ RMI services bound successfully");
    }

    public void stop() {
        try {
            if (registry != null) {
                // Unbind services
                registry.unbind(AppConstants.RMI_AUTH_SERVICE);
                registry.unbind(AppConstants.RMI_USER_SERVICE);
                registry.unbind(AppConstants.RMI_FRIEND_SERVICE);
                registry.unbind(AppConstants.RMI_PEER_DISCOVERY_SERVICE);
                registry.unbind("MessageService"); // ✅ NEW
            }
        } catch (Exception e) {
            System.err.println("Error stopping RMI server: " + e.getMessage());
        }
    }
}