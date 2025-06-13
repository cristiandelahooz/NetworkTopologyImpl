package org.sakidoa.core;

import java.util.List;

public class MessageRouter {
    private final List<Node> nodes;

    public MessageRouter(List<Node> nodes) {
        this.nodes = nodes;
    }

    public void routeMessage(int fromNode, int toNode, String message) {
        validateNodeIndices(fromNode, toNode);

        Node targetNode = nodes.get(toNode);
        targetNode.receiveMessage(message);
    }

    private void validateNodeIndices(int fromNode, int toNode) {
        if (fromNode < 0 || fromNode >= nodes.size()) {
            throw new IndexOutOfBoundsException("From node index out of bounds: " + fromNode);
        }
        if (toNode < 0 || toNode >= nodes.size()) {
            throw new IndexOutOfBoundsException("To node index out of bounds: " + toNode);
        }
    }
}
