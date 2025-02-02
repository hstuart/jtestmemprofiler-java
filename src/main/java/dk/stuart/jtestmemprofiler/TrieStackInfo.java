package dk.stuart.jtestmemprofiler;

import java.util.List;
import java.util.Map;

/**
 * Information about a stack frame's allocations
 * @param key The name of the stack frame (declaring class and method)
 * @param value The node corresponding to that stack frame with allocation information
 * @param children All children of the current node (primarily used for traversal)
 * @param lastVisitedChildOffset Last offset into the children list currently visited (primarily used for traversal)
 */
public record TrieStackInfo(String key, TrieNode value, List<Map.Entry<String, TrieNode>> children, int lastVisitedChildOffset) {
}
