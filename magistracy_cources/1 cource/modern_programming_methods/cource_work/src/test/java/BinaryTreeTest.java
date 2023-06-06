import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import persistence.base.tree.BinaryTree;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BinaryTreeTest {
    private static BinaryTree<Integer, Integer> tree;

    @BeforeAll
    public static void initTree() {
        tree = new BinaryTree<>();
        tree.insert(10, 10);
        tree.insert(5, 5);
        tree.insert(15, 15);
        tree.insert(3, 3);
        tree.insert(7, 7);
        tree.insert(12, 12);
        tree.insert(18, 18);
        tree.insert(1, 1);
        tree.insert(4, 4);
        tree.insert(6, 6);
        tree.insert(9, 9);
        tree.insert(14, 14);
        tree.insert(17, 17);
        tree.insert(20, 20);
    }

    public static Stream<Arguments> searchTestArgSource() {
        return Stream.of(
                Arguments.of(8, 7),
                Arguments.of(2, 1),
                Arguments.of(13, 12),
                Arguments.of(19, 18),
                Arguments.of(30, 20),
                Arguments.of(1, 1),
                Arguments.of(6, 6),
                Arguments.of(20, 20),
                Arguments.of(0, null),
                Arguments.of(-10, null)
        );
    }

    @ParameterizedTest
    @MethodSource("searchTestArgSource")
    public void searchTest(Integer value, Integer expected) {
        var result = tree.findNearestLess(value);
        assertEquals(expected, result);
    }

    @Test
    public void toListTest() {
        var list = tree.toList();
        int[] keys = new int[]{
                1, 3, 4, 5, 6, 7, 9, 10, 12, 14, 15, 17, 18, 20
        };

        for (int i = 0; i < list.size(); i++) {
            assertEquals(keys[i], list.get(i).getKey());
        }
    }
}
