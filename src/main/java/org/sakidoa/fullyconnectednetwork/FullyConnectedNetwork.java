package org.sakidoa.fullyconnectednetwork;

import org.sakidoa.core.*;
import org.sakidoa.core.enums.MessageType;

import java.util.*;
import java.util.concurrent.*;

public class FullyConnectedNetwork implements NetworkTopology {

    private final Map<String, Node> nodes = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void configureNetwork(int numberOfNodes) {
        for (int i = 0; i < numberOfNodes; i++) {
            String nodeId = "Node-" + i;
            Node node = new Node(nodeId);
            nodes.put(nodeId, node);
        }

        for (Node nodeA : nodes.values()) {
            for (Node nodeB : nodes.values()) {
                if (!nodeA.equals(nodeB)) {
                    nodeA.addNeighbor(nodeB);
                }
            }
        }
    }

    @Override
    public void sendMessage(int fromNode, int toNode, String message) {
        String fromId = "Node-" + fromNode;
        String toId = "Node-" + toNode;

        Node sender = nodes.get(fromId);
        Node receiver = nodes.get(toId);

        if (sender != null && receiver != null && sender.isNeighbor(receiver)) {
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
