package org.sakidoa.starnetwork;

import org.sakidoa.core.*;
import org.sakidoa.core.enums.MessageType;

import java.util.*;
import java.util.concurrent.*;

public class StarNetwork implements NetworkTopology {

    private final Map<String, Node> nodes = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private Node centralNode;

    @Override
    public void configureNetwork(int numberOfNodes) {
        if (numberOfNodes < 2) {
            throw new IllegalArgumentException("La topología estrella requiere al menos 2 nodos.");
        }

        for (int i = 0; i < numberOfNodes; i++) {
            String nodeId = "Node-" + i;
            Node node = new Node(nodeId);
            nodes.put(nodeId, node);
        }

        centralNode = nodes.get("Node-0");

        for (Node node : nodes.values()) {
            if (!node.equals(centralNode)) {
                node.addNeighbor(centralNode);
                centralNode.addNeighbor(node);
            }
        }
    }

    @Override
    public void sendMessage(int fromNode, int toNode, String message) {
        String fromId = "Node-" + fromNode;
        String toId = "Node-" + toNode;

        Node sender = nodes.get(fromId);
        Node receiver = nodes.get(toId);

        if (sender == null || receiver == null) return;

        if (!sender.equals(centralNode) && !receiver.equals(centralNode)) {
            Message toHub = new Message(MessageType.DATA, fromId, "→ HUB: " + message);
            toHub.setReceiverId(centralNode.getNodeId());
            centralNode.sendMessage(toHub);

            Message toReceiver = new Message(MessageType.DATA, "HUB", "→ " + toId + ": " + message);
            toReceiver.setReceiverId(toId);
            receiver.sendMessage(toReceiver);
        } else {
            Message msg = new Message(MessageType.DATA, fromId, message);
            msg.setReceiverId(toId);
            receiver.sendMessage(msg);
        }
    }

    @Override
    public void runNetwork() {
        for (Node node : nodes.values()) {
            executorService.execute(node);
        }
    }

    @Override
    public void shutdownNetwork() {
        for (Node node : nodes.values()) {
            node.shutdown();
        }
        executorService.shutdown();
    }
}
