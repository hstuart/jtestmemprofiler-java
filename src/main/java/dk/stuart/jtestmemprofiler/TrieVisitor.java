package dk.stuart.jtestmemprofiler;

import java.util.Deque;

/**
 * Visitor for acting on all selected trie nodes.
 */
public interface TrieVisitor {
    /**
     * Perform an action on an allocation stack (e.g. self-allocators or leaves).
     * @param stack The current stack leading to this node.
     */
    void visitStack(Deque<TrieStackInfo> stack);
}
