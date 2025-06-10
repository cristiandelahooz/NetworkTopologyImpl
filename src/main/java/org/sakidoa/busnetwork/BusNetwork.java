package org.sakidoa.busnetwork;

import org.sakidoa.core.Node;
import org.sakidoa.networktopology.NetworkTopology;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BusNetwork implements NetworkTopology {
    private List<Node> nodes;
    private ExecutorService executor;

    /**
     * @param numberOfNodes
     */
    @Override
    public void configureNetwork(int numberOfNodes) {
        nodes = new ArrayList<>();
        //...
        executor = Executors.newFixedThreadPool(numberOfNodes);
    }

    /**
     * @param fromNode
     * @param toNode
     * @param message
     */
    @Override
    public void sendMessage(int fromNode, int toNode, String message) {
        //...
        executor.submit(() -> nodes.get(toNode).receiveMessage(message));
    }

    /**
     *
     */
    @Override
    public void runNetwork() {
        //...
        for (Node node : nodes) {
            executor.submit(node);
        }

    }

    /**
     *
     */
    @Override
    public void shutdownNetwork() {
        executor.shutdown();
    }
}