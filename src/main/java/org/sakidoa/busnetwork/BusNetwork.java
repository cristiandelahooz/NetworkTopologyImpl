package org.sakidoa.busnetwork;

import org.sakidoa.core.MessageRouter;
import org.sakidoa.core.Node;
import org.sakidoa.core.NetworkTopology;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

public class BusNetwork implements NetworkTopology {
    private final AtomicBoolean running = new AtomicBoolean(false);
    private List<Node> nodes;
    private ExecutorService executor;
    private MessageRouter messageRouter;

    @Override
    public void configureNetwork(int numberOfNodes) {
        validateNodeCount(numberOfNodes);
        createNodes(numberOfNodes);
        configureBusConnections();
        initializeExecutor(numberOfNodes);
        initializeMessageRouter();
    }

    @Override
    public void sendMessage(int fromNode, int toNode, String message) {
        validateNetworkRunning();
        executor.submit(() -> messageRouter.routeMessage(fromNode, toNode, message));
    }

    @Override
    public void runNetwork() {
        validateNetworkConfigured();
        running.set(true);
        startNodes();
    }

    @Override
    public void shutdownNetwork() {
        running.set(false);
        shutdownNodes();
        gracefulShutdown();
    }

    private void validateNodeCount(int numberOfNodes) {
        if (numberOfNodes <= 0) {
            throw new IllegalArgumentException("Number of nodes must be positive");
        }
    }

    private void createNodes(int numberOfNodes) {
        this.nodes = IntStream.range(0, numberOfNodes)
                .mapToObj(i -> new Node("bus-node-" + i))
                .toList();
    }

    private void configureBusConnections() {
        for (int i = 0; i < nodes.size(); i++) {
            Node currentNode = nodes.get(i);
            for (int j = 0; j < nodes.size(); j++) {
                if (i != j) {
                    currentNode.addNeighbor(nodes.get(j));
                }
            }
        }
    }

    private void initializeExecutor(int numberOfNodes) {
        this.executor = Executors.newFixedThreadPool(numberOfNodes + 1);
    }

    private void initializeMessageRouter() {
        this.messageRouter = new MessageRouter(nodes);
    }

    private void validateNetworkRunning() {
        if (!running.get()) {
            throw new IllegalStateException("Network is not running");
        }
    }

    private void validateNetworkConfigured() {
        if (nodes == null) {
            throw new IllegalStateException("Network not configured");
        }
    }

    private void startNodes() {
        nodes.forEach(executor::submit);
    }

    private void shutdownNodes() {
        if (nodes != null) {
            nodes.forEach(Node::shutdown);
        }
    }

    private void gracefulShutdown() {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                        System.err.println("Executor did not terminate gracefully");
                    }
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

}