package org.sakidoa.core;

import org.sakidoa.core.enums.MessageType;
import org.sakidoa.core.enums.NodeEvent;
import org.sakidoa.core.enums.NodeState;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class Node implements Runnable {
    private final String nodeId;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Set<Node> neighbors = Collections.synchronizedSet(new HashSet<>());
    private final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();
    private final ExecutorService messageProcessor = Executors.newSingleThreadExecutor();
    private final AtomicLong processedMessages = new AtomicLong(0);
    private final List<NodeEventListener> eventListeners = Collections.synchronizedList(new ArrayList<>());
    private volatile boolean active;
    private volatile long lastUpdateTime;
    private volatile NodeState state = NodeState.IDLE;

    public Node(String nodeId) {
        this.nodeId = Objects.requireNonNull(nodeId, "Node ID cannot be null");
        this.active = true;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        if (!running.compareAndSet(false, true)) {
            return;
        }

        try {
            notifyListeners(NodeEvent.STARTED);
            setState(NodeState.RUNNING);

            while (active && !Thread.currentThread().isInterrupted()) {
                try {
                    processMessages();
                    performNodeOperations();
                    updateNodeState();
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    handleException(e);
                }
            }
        } finally {
            cleanup();
            running.set(false);
            setState(NodeState.STOPPED);
            notifyListeners(NodeEvent.STOPPED);
        }
    }

    private void processMessages() throws InterruptedException {
        Message message = messageQueue.poll(50, TimeUnit.MILLISECONDS);
        if (message != null) {
            handleMessage(message);
            processedMessages.incrementAndGet();
        }
    }

    private void handleMessage(Message message) {
        switch (message.getType()) {
            case DATA -> processDataMessage(message);
            case CONTROL -> processControlMessage(message);
            case HEARTBEAT -> processHeartbeatMessage(message);
            case TOPOLOGY_UPDATE -> processTopologyUpdate(message);
            default -> handleUnknownMessage(message);
        }
    }

    protected void performNodeOperations() {
        if (System.currentTimeMillis() - lastUpdateTime > 5000) {
            sendHeartbeatToNeighbors();
            lastUpdateTime = System.currentTimeMillis();
        }
    }

    private void updateNodeState() {
        lastUpdateTime = System.currentTimeMillis();
        setState(messageQueue.size() > 100 ? NodeState.BUSY : 
                messageQueue.isEmpty() ? NodeState.IDLE : state);
    }

    public boolean sendMessage(Message message) {
        if (!active) {
            return false;
        }

        try {
            System.out.println(this.nodeId + " envió a " + message.getReceiverId() + ": " + message.getPayload());
            return messageQueue.offer(message, 1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public boolean sendMessageToNeighbor(String neighborId, Message message) {
        return neighbors.stream()
                .filter(neighbor -> neighbor.getNodeId().equals(neighborId))
                .findFirst()
                .map(neighbor -> neighbor.sendMessage(message))
                .orElse(false);
    }

    public void broadcastMessage(Message message) {
        neighbors.forEach(neighbor -> neighbor.sendMessage(message));
    }

    public boolean addNeighbor(Node neighbor) {
        if (neighbor == null || neighbor == this) {
            return false;
        }

        boolean added = neighbors.add(neighbor);
        if (added) {
            notifyListeners(NodeEvent.NEIGHBOR_ADDED);
        }
        return added;
    }

    public boolean removeNeighbor(Node neighbor) {
        boolean removed = neighbors.remove(neighbor);
        if (removed) {
            notifyListeners(NodeEvent.NEIGHBOR_REMOVED);
        }
        return removed;
    }

    public Set<Node> getNeighbors() {
        return new HashSet<>(neighbors);
    }

    public boolean isNeighbor(Node node) {
        return neighbors.contains(node);
    }

    private void sendHeartbeatToNeighbors() {
        Message heartbeat = new Message(MessageType.HEARTBEAT, nodeId, "heartbeat", System.currentTimeMillis());
        broadcastMessage(heartbeat);
    }

    private void processDataMessage(Message message) {
        System.out.println(this.nodeId + " recibió de " + message.getSenderId() + ": " + message.getPayload());
        onDataMessageReceived(message);
    }

    private void processControlMessage(Message message) {
        String command = message.getPayload().toString();
        switch (command.toLowerCase()) {
            case "stop" -> shutdown();
            case "pause" -> setState(NodeState.PAUSED);
            case "resume" -> setState(NodeState.RUNNING);
            default -> onControlMessageReceived(message);
        }
    }

    private void processHeartbeatMessage(Message message) {
        onHeartbeatReceived(message);
    }

    private void processTopologyUpdate(Message message) {
        onTopologyUpdateReceived(message);
    }

    private void handleUnknownMessage(Message message) {
        onUnknownMessageReceived(message);
    }

    protected void onDataMessageReceived(Message message) {}
    protected void onControlMessageReceived(Message message) {}
    protected void onHeartbeatReceived(Message message) {}
    protected void onTopologyUpdateReceived(Message message) {}
    protected void onUnknownMessageReceived(Message message) {}

    private void handleException(Exception e) {
        notifyListeners(NodeEvent.ERROR);
        e.printStackTrace();
    }

    private void cleanup() {
        messageProcessor.shutdown();
        try {
            if (!messageProcessor.awaitTermination(5, TimeUnit.SECONDS)) {
                messageProcessor.shutdownNow();
            }
        } catch (InterruptedException e) {
            messageProcessor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        messageQueue.clear();
        neighbors.clear();
        eventListeners.clear();
    }

    public void shutdown() {
        active = false;
        notifyListeners(NodeEvent.SHUTDOWN_REQUESTED);
    }

    public void forceStop() {
        active = false;
        if (running.get()) {
            Thread.currentThread().interrupt();
        }
    }

    public void addEventListener(NodeEventListener listener) {
        eventListeners.add(listener);
    }

    public void removeEventListener(NodeEventListener listener) {
        eventListeners.remove(listener);
    }

    private void notifyListeners(NodeEvent event) {
        eventListeners.forEach(listener -> {
            try {
                listener.onNodeEvent(this, event);
            } catch (Exception e) {
                // Handle listener exceptions gracefully
            }
        });
    }

    public String getNodeId() {
        return nodeId;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isRunning() {
        return running.get();
    }

    public NodeState getState() {
        return state;
    }

    private void setState(NodeState newState) {
        NodeState oldState = this.state;
        this.state = newState;
        if (oldState != newState) {
            notifyListeners(NodeEvent.STATE_CHANGED);
        }
    }

    public long getProcessedMessageCount() {
        return processedMessages.get();
    }

    public int getQueueSize() {
        return messageQueue.size();
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public int getNeighborCount() {
        return neighbors.size();
    }

    public void receiveMessage(String message) {
        if (!active) {
            return;
        }

        Message msg = new Message(MessageType.DATA, "external", message);

        try {
            if (!messageQueue.offer(msg, 1, TimeUnit.SECONDS)) {
                System.err.println("Failed to receive message: queue is full for node " + nodeId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while trying to receive message for node " + nodeId);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(nodeId, node.nodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId);
    }

    @Override
    public String toString() {
        return String.format("Node{id='%s', state=%s, neighbors=%d, queue=%d}", 
                nodeId, state, neighbors.size(), messageQueue.size());
    }

    @FunctionalInterface
    public interface NodeEventListener {
        void onNodeEvent(Node node, NodeEvent event);
    }
}