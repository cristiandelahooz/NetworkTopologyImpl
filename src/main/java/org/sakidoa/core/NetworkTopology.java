package org.sakidoa.networktopology;

public interface NetworkTopology {
    void configureNetwork(int numberOfNodes);

    void sendMessage(int fromNode, int toNode, String message);

    void runNetwork();

    void shutdownNetwork();

}
