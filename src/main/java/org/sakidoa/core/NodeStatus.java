package org.sakidoa.core;

import org.sakidoa.core.enums.NodeState;

public record NodeStatus(String nodeId, String nodeName, NodeState state, boolean active, boolean running,
                         int neighborCount, int queueSize, long processedMessages, long lastUpdateTime) {
}
