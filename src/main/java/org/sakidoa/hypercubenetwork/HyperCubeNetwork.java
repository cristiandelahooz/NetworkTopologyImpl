package org.sakidoa.hypercubenetwork;

import org.sakidoa.core.MessageRouter;
import org.sakidoa.core.NetworkTopology;
import org.sakidoa.core.Node;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

public class HyperCubeNetwork implements NetworkTopology {
    private final AtomicBoolean running = new AtomicBoolean(false);
    private List<Node> nodes;
    private ExecutorService executor;
    private MessageRouter messageRouter;
    private int dimensions;

    @Override
    public void configureNetwork(int numberOfNodes) {
        validateNodeCount(numberOfNodes);
        calculateDimensions(numberOfNodes);
        createNodes(numberOfNodes);
        configureHyperCubeConnections();
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
        if (numberOfNodes < 2) {
            throw new IllegalArgumentException("HyperCube network requires at least 2 nodes");
        }
        if (!isPowerOfTwo(numberOfNodes)) {
            throw new IllegalArgumentException("HyperCube network requires number of nodes to be a power of 2");
        }
    }

    private boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }

    private void calculateDimensions(int numberOfNodes) {
        this.dimensions = Integer.numberOfTrailingZeros(numberOfNodes);
    }

    private void createNodes(int numberOfNodes) {
        this.nodes = IntStream.range(0, numberOfNodes)
                .mapToObj(i -> new Node("hypercube-node-" + i))
                .toList();
    }

    private void configureHyperCubeConnections() {
        int nodeCount = nodes.size();
        
        for (int i = 0; i < nodeCount; i++) {
            Node currentNode = nodes.get(i);
            connectToHyperCubeNeighbors(currentNode, i);
        }
    }

    private void connectToHyperCubeNeighbors(Node currentNode, int nodeIndex) {
        for (int dimension = 0; dimension < dimensions; dimension++) {
            int neighborIndex = calculateNeighborIndex(nodeIndex, dimension);
            Node neighbor = nodes.get(neighborIndex);
            currentNode.addNeighbor(neighbor);
        }
    }

    private int calculateNeighborIndex(int nodeIndex, int dimension) {
        return nodeIndex ^ (1 << dimension);
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