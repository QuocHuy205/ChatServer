package vku.chatapp.server.rmi;

import vku.chatapp.common.dto.PeerInfo;
import vku.chatapp.common.rmi.IPeerDiscoveryService;
import vku.chatapp.server.core.ClientRegistry;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class PeerDiscoveryServiceImpl extends UnicastRemoteObject implements IPeerDiscoveryService {
    private final ClientRegistry registry;

    public PeerDiscoveryServiceImpl() throws RemoteException {
        super();
        this.registry = ClientRegistry.getInstance();
    }

    @Override
    public boolean registerPeer(PeerInfo peerInfo) throws RemoteException {
        return registry.registerPeer(peerInfo);
    }

    @Override
    public boolean unregisterPeer(Long userId) throws RemoteException {
        return registry.unregisterPeer(userId);
    }

    @Override
    public PeerInfo getPeerInfo(Long userId) throws RemoteException {
        return registry.getPeerInfo(userId);
    }

    @Override
    public List<PeerInfo> getOnlineFriends(Long userId) throws RemoteException {
        return registry.getOnlineFriends(userId);
    }

    @Override
    public boolean updateHeartbeat(Long userId) throws RemoteException {
        return registry.updateHeartbeat(userId);
    }
}