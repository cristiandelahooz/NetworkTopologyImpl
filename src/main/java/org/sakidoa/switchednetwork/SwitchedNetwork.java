package org.sakidoa.switchednetwork;

import org.sakidoa.core.*;
import org.sakidoa.core.enums.MessageType;

import java.util.*;
import java.util.concurrent.*;

public class SwitchedNetwork implements NetworkTopology {

    private final Map<String, Node> nodes = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final ExecutorService switchExecutor = Executors.newSingleThreadExecutor();
    private final BlockingQueue<Message> switchQueue = new LinkedBlockingQueue<>();

    @Override
    public void configureNetwork(int numberOfNodes) {
        for (int i = 0; i < numberOfNodes; i++) {
            String nodeId = "Node-" + i;
            Node node = new Node(nodeId);
            nodes.put(nodeId, node);
        }

        switchExecutor.execute(() -> {
            try {
                while (true) {
                    Message msg = switchQueue.take();
                    Node recipient = nodes.get(msg.getReceiverId());
                    if (recipient != null) {
                        recipient.sendMessage(msg);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    @Override
    public void sendMessage(int fromNode, int toNode, String message) {
        String fromId = "Node-" + fromNode;
        String toId = "Node-" + toNode;

        if (!nodes.containsKey(fromId) || !nodes.containsKey(toId)) return;

        Message msg = new Message(MessageType.DATA, fromId, message);
        msg.setReceiverId(toId);
        switchQueue.offer(msg);
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
        executorService.shutdownNow();
        switchExecutor.shutdownNow();
    }
}
