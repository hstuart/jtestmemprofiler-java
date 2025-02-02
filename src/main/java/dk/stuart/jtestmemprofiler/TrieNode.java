package dk.stuart.jtestmemprofiler;

import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * A Trie structure for efficient prefix-based storage of stacktraces based on Java internal naming standards.
 * The stack traces are bottom up (i.e., inverse order of what you would have in a stacktrace in an Exception, or
 * what you would normally be traversing in other profilers) to help reduce memory overhead.
 */
@SuppressWarnings("unused")
public class TrieNode {
    private final HashMap<String, TrieNode> children;
    private final long allocationSize;
    private final long childAccumulatedAllocationSize;

    /**
     * Used by {@link NativeCallTreeCollector} to construct the trie from the agent.
     */
    public TrieNode(HashMap<String, TrieNode> children, long allocationSize, long childAccumulatedAllocationSize) {
        this.children = children;
        this.allocationSize = allocationSize;
        this.childAccumulatedAllocationSize = childAccumulatedAllocationSize;
    }

    private static <T> Deque<T> cloneArrayDequeWithoutModifyingOriginal(Deque<T> original) {
        Deque<T> clone = new ArrayDeque<>(original.size());

        for (T element : original) {
            clone.push(element);
        }

        return clone;
    }

    /**
     * Get the stacktrace elements that is called from this one (only present if they allocated memory as part of the
     * profiling).
     *
     * @return A map from stacktrace info to allocation details and further stacktrace elements.
     */
    public Map<String, TrieNode> getChildren() {
        return children;
    }

    /**
     * Returns the direct allocation at this stacktrace element.
     */
    public long getAllocationSize() {
        return allocationSize;
    }

    /**
     * Returns the accumulated allocation at this stacktrace element and all its children recursively.
     */
    public long getChildAccumulatedAllocationSize() {
        return childAccumulatedAllocationSize;
    }

    /**
     * Writes all stack traces and their allocation info to the specified print stream.
     */
    public void write(PrintStream sw) {
        visitLeaf(stack -> {
            stack.forEach(e -> sw.format("%s - %d %d%n", e.key(), e.value().allocationSize, e.value().childAccumulatedAllocationSize));
            sw.println();
        });
    }

    /**
     * Visits all leafs in the trie and passes the stack information to the specified visitor for further processing.
     */
    @SuppressWarnings("ReassignedVariable")
    public void visitLeaf(TrieVisitor visitor) {
        for (var entry : children.entrySet()) {
            var stack = new ArrayDeque<TrieStackInfo>();
            stack.push(new TrieStackInfo(entry.getKey(), entry.getValue(), entry.getValue().children.entrySet().stream().toList(), -1));

            while (!stack.isEmpty()) {
                var top = stack.peek();
                if (top.children().isEmpty()) {
                    visitor.visitStack(cloneArrayDequeWithoutModifyingOriginal(stack));
                    stack.pop();
                    continue;
                }

                top = stack.pop();
                if (top.lastVisitedChildOffset() + 1 < top.children().size()) {
                    stack.push(new TrieStackInfo(top.key(), top.value(), top.children(), top.lastVisitedChildOffset() + 1));
                    var child = top.children().get(top.lastVisitedChildOffset() + 1);
                    stack.push(new TrieStackInfo(child.getKey(), child.getValue(), child.getValue().children.entrySet().stream().toList(), -1));
                }
            }
        }
    }

    /**
     * Visit all nodes that allocate something (whether they are leaves or not).
     */
    public void visitSelfAllocators(TrieVisitor visitor) {
        for (var entry : children.entrySet()) {
            var stack = new ArrayDeque<TrieStackInfo>();
            stack.push(new TrieStackInfo(entry.getKey(), entry.getValue(), entry.getValue().children.entrySet().stream().toList(), -1));

            while (!stack.isEmpty()) {
                var top = stack.peek();
                if (top.children().isEmpty()) {
                    visitor.visitStack(cloneArrayDequeWithoutModifyingOriginal(stack));
                    stack.pop();
                    continue;
                } else if (top.lastVisitedChildOffset() + 1 >= top.children().size() && top.value().allocationSize > 0) {
                    visitor.visitStack(cloneArrayDequeWithoutModifyingOriginal(stack));
                }

                top = stack.pop();
                if (top.lastVisitedChildOffset() + 1 < top.children().size()) {
                    stack.push(new TrieStackInfo(top.key(), top.value(), top.children(), top.lastVisitedChildOffset() + 1));
                    var child = top.children().get(top.lastVisitedChildOffset() + 1);
                    stack.push(new TrieStackInfo(child.getKey(), child.getValue(), child.getValue().children.entrySet().stream().toList(), -1));
                }
            }
        }
    }
}
