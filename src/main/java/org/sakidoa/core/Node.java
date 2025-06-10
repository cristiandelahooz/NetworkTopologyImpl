package org.sakidoa.core;

import org.sakidoa.core.enums.MessageType;
import org.sakidoa.core.enums.NodeEvent;
import org.sakidoa.core.enums.NodeState;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class Node implements Runnable {

    // Node identification and basic properties
    private final String nodeId;
    private final String nodeName;
    private final AtomicBoolean running = new AtomicBoolean(false);
    // Topology connections
    private final Set<Node> neighbors = Collections.synchronizedSet(new HashSet<>());
    private final Map<String, Object> nodeProperties = new ConcurrentHashMap<>();
    // Communication and messaging
    private final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();
    private final ExecutorService messageProcessor = Executors.newSingleThreadExecutor();
    private final AtomicLong processedMessages = new AtomicLong(0);
    // Event listeners
    private final List<NodeEventListener> eventListeners = Collections.synchronizedList(new ArrayList<>());
    private volatile boolean active;
    // Simulation/System state
    private volatile long lastUpdateTime;
    private volatile NodeState state = NodeState.IDLE;

    public Node(String nodeId, String nodeName) {
        this.nodeId = Objects.requireNonNull(nodeId, "Node ID cannot be null");
        this.nodeName = nodeName != null ? nodeName : nodeId;
        this.active = true;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public Node(String nodeId) {
        this(nodeId, null);
    }

    @Override
    public void run() {
        if (!running.compareAndSet(false, true)) {
            return; // Already running
        }

        try {
            notifyListeners(NodeEvent.STARTED);
            setState(NodeState.RUNNING);

            while (active && !Thread.currentThread().isInterrupted()) {
                try {
                    // Process incoming messages
                    processMessages();

                    // Perform node-specific operations
                    performNodeOperations();

                    // Update node state
                    updateNodeState();

                    // Brief pause to prevent CPU spinning
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

    /**
     * Process incoming messages from the message queue
     */
    private void processMessages() throws InterruptedException {
        Message message = messageQueue.poll(50, TimeUnit.MILLISECONDS);
        if (message != null) {
            handleMessage(message);
            processedMessages.incrementAndGet();
        }
    }

    /**
     * Handle individual messages based on their type
     */
    private void handleMessage(Message message) {
        switch (message.getType()) {
        case DATA:
            processDataMessage(message);
            break;
        case CONTROL:
            processControlMessage(message);
            break;
        case HEARTBEAT:
            processHeartbeatMessage(message);
            break;
        case TOPOLOGY_UPDATE:
            processTopologyUpdate(message);
            break;
        default:
            // Handle unknown message types
            handleUnknownMessage(message);
        }
    }

    /**
     * Perform node-specific operations during each cycle
     */
    protected void performNodeOperations() {
        // Template method - can be overridden by subclasses
        // Default implementation: send heartbeat to neighbors
        if (System.currentTimeMillis() - lastUpdateTime > 5000) {
            sendHeartbeatToNeighbors();
            lastUpdateTime = System.currentTimeMillis();
        }
    }

    /**
     * Update the node's internal state
     */
    private void updateNodeState() {
        // Update last update time
        lastUpdateTime = System.currentTimeMillis();

        // Check if node should transition states
        if (messageQueue.size() > 100) {
            setState(NodeState.BUSY);
        } else if (messageQueue.isEmpty()) {
            setState(NodeState.IDLE);
        }
    }

    /**
     * Send a message to this node
     */
    public boolean sendMessage(Message message) {
        if (!active) {
            return false;
        }

        try {
            return messageQueue.offer(message, 1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Send a message to a specific neighbor
     */
    public boolean sendMessageToNeighbor(String neighborId, Message message) {
        return neighbors.stream().filter(neighbor -> neighbor.getNodeId().equals(neighborId)).findFirst()
                .map(neighbor -> neighbor.sendMessage(message)).orElse(false);
    }

    /**
     * Broadcast a message to all neighbors
     */
    public void broadcastMessage(Message message) {
        neighbors.forEach(neighbor -> neighbor.sendMessage(message));
    }

    /**
     * Add a neighbor to this node's topology
     */
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

    /**
     * Remove a neighbor from this node's topology
     */
    public boolean removeNeighbor(Node neighbor) {
        boolean removed = neighbors.remove(neighbor);
        if (removed) {
            notifyListeners(NodeEvent.NEIGHBOR_REMOVED);
        }
        return removed;
    }

    /**
     * Get all neighbors of this node
     */
    public Set<Node> getNeighbors() {
        return new HashSet<>(neighbors);
    }

    /**
     * Check if a node is a neighbor
     */
    public boolean isNeighbor(Node node) {
        return neighbors.contains(node);
    }

    /**
     * Send heartbeat to all neighbors
     */
    private void sendHeartbeatToNeighbors() {
        Message heartbeat = new Message(MessageType.HEARTBEAT, nodeId, "heartbeat", System.currentTimeMillis());
        broadcastMessage(heartbeat);
    }

    /**
     * Process different types of messages
     */
    private void processDataMessage(Message message) {
        // Handle data messages - template method
        onDataMessageReceived(message);
    }

    private void processControlMessage(Message message) {
        // Handle control messages
        String command = message.getPayload().toString();
        switch (command.toLowerCase()) {
        case "stop":
            shutdown();
            break;
        case "pause":
            setState(NodeState.PAUSED);
            break;
        case "resume":
            setState(NodeState.RUNNING);
            break;
        default:
            onControlMessageReceived(message);
        }
    }

    private void processHeartbeatMessage(Message message) {
        // Update neighbor's last seen time
        onHeartbeatReceived(message);
    }

    private void processTopologyUpdate(Message message) {
        // Handle topology changes
        onTopologyUpdateReceived(message);
    }

    private void handleUnknownMessage(Message message) {
        // Log or handle unknown message types
        onUnknownMessageReceived(message);
    }

    /**
     * Template methods for subclass customization
     */
    protected void onDataMessageReceived(Message message) {
        // Override in subclasses
    }

    protected void onControlMessageReceived(Message message) {
        // Override in subclasses
    }

    protected void onHeartbeatReceived(Message message) {
        // Override in subclasses
    }

    protected void onTopologyUpdateReceived(Message message) {
        // Override in subclasses
    }

    protected void onUnknownMessageReceived(Message message) {
        // Override in subclasses
    }

    /**
     * Exception handling
     */
    private void handleException(Exception e) {
        notifyListeners(NodeEvent.ERROR);
        // Log the exception or handle it appropriately
        e.printStackTrace();
    }

    /**
     * Cleanup resources
     */
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

    /**
     * Shutdown the node gracefully
     */
    public void shutdown() {
        active = false;
        notifyListeners(NodeEvent.SHUTDOWN_REQUESTED);
    }

    /**
     * Force stop the node
     */
    public void forceStop() {
        active = false;
        if (running.get()) {
            // Interrupt the running thread if we have a reference to it
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Event listener management
     */
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

    /**
     * Property management
     */
    public void setProperty(String key, Object value) {
        nodeProperties.put(key, value);
    }

    public Object getProperty(String key) {
        return nodeProperties.get(key);
    }

    public <T> T getProperty(String key, Class<T> type) {
        Object value = nodeProperties.get(key);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    /**
     * Getters and status methods
     */
    public String getNodeId() {
        return nodeId;
    }

    public String getNodeName() {
        return nodeName;
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

    /**
     * State management
     */
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

    /**
     * Status and diagnostic information
     */
    public NodeStatus getStatus() {
        return new NodeStatus(nodeId, nodeName, state, active, running.get(), neighbors.size(), messageQueue.size(),
                processedMessages.get(), lastUpdateTime);
    }

    public void receiveMessage(String message) {
        if (!active) {
            return; // Node is not active, ignore the message
        }

        // Create a Message object from the string message
        Message msg = new Message(MessageType.DATA, "external", message);

        // Add the message to the queue for processing
        try {
            if (!messageQueue.offer(msg, 1, TimeUnit.SECONDS)) {
                // Queue is full, message couldn't be added
                System.err.println("Failed to receive message: queue is full for node " + nodeId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while trying to receive message for node " + nodeId);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Node node = (Node) o;
        return Objects.equals(nodeId, node.nodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId);
    }

    @Override
    public String toString() {
        return String.format("Node{id='%s', name='%s', state=%s, neighbors=%d, queue=%d}", nodeId, nodeName, state,
                neighbors.size(), messageQueue.size());
    }

    @FunctionalInterface
    public interface NodeEventListener {

        void onNodeEvent(Node node, NodeEvent event);
    }
}