package dk.stuart.jtestmemprofiler;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class TrieTest {
    @Test
    void write_splitTree_writesTwice() {
        var leaf1 = new TrieNode(new HashMap<>(), 30, 30);
        var leaf2 = new TrieNode(new HashMap<>(), 40, 40);
        var common = new TrieNode(new HashMap<>() {{
            put("leaf1", leaf1);
            put("leaf2", leaf2);
        }}, 0, 70);
        var trie = new TrieNode(new HashMap<>() {{
            put("common", common);
        }}, 0, 70);

        var data = new ByteArrayOutputStream();
        var output = new PrintStream(data, true, StandardCharsets.UTF_8);
        trie.write(output);

        assertThat(data.toString(StandardCharsets.UTF_8).split("\\r?\\n")).isEqualTo("common - 0 70\nleaf1 - 30 30\n\ncommon - 0 70\nleaf2 - 40 40\n\n".split("\\r?\\n"));
    }

    @Test
    void getChildren_noChildren_returnsEmptyList() {
        var trie = new TrieNode(new HashMap<>(), 0, 0);

        assertThat(trie.getChildren()).isEmpty();
    }

    @Test
    void getChildren_singleChild_returnsSingleChild() {
        var leaf1 = new TrieNode(new HashMap<>(), 30, 30);
        var common = new TrieNode(new HashMap<>() {{
            put("leaf1", leaf1);
        }}, 0, 0);

        assertThat(common.getChildren()).hasSize(1);
    }

    @Test
    void getAllocationSize_singleNode_returnsAllocationSize() {
        var leaf1 = new TrieNode(new HashMap<>(), 30, 30);

        assertThat(leaf1.getAllocationSize()).isEqualTo(30);
    }

    @Test
    void getChildAccumulatedAllocationSize_singleNode_returnsAccumulatedAllocationSize() {
        var leaf1 = new TrieNode(new HashMap<>(), 0, 30);

        assertThat(leaf1.getChildAccumulatedAllocationSize()).isEqualTo(30);
    }

    @Test
    void visitSelfAllocators_treeWithAllLevelAllocations_visitsSelfAllocators() {
        var leaf1 = new TrieNode(new HashMap<>(), 30, 30);
        var leaf2 = new TrieNode(new HashMap<>(), 40, 40);
        var common = new TrieNode(new HashMap<>() {{
            put("leaf1", leaf1);
            put("leaf2", leaf2);
        }}, 10, 80);
        var trie = new TrieNode(new HashMap<>() {{
            put("common", common);
        }}, 0, 80);

        var nodes = new ArrayList<TrieNode>();
        trie.visitSelfAllocators(stack -> nodes.add(stack.peekLast().value()));

        assertThat(nodes).containsExactlyInAnyOrder(leaf1, leaf2, common);
    }

    @Test
    void visitSelfAllocators_treeWithOnlyLeafAllocations_visitsSelfAllocators() {
        var leaf1 = new TrieNode(new HashMap<>(), 30, 30);
        var leaf2 = new TrieNode(new HashMap<>(), 40, 40);
        var common = new TrieNode(new HashMap<>() {{
            put("leaf1", leaf1);
            put("leaf2", leaf2);
        }}, 0, 70);
        var trie = new TrieNode(new HashMap<>() {{
            put("common", common);
        }}, 0, 70);

        var nodes = new ArrayList<TrieNode>();
        trie.visitSelfAllocators(stack -> nodes.add(stack.peekLast().value()));

        assertThat(nodes).containsExactlyInAnyOrder(leaf1, leaf2);
    }
}
