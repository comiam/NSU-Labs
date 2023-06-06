import org.junit.jupiter.api.Test;
import persistence.structure.list.PersistentLinkedList;

import static org.junit.jupiter.api.Assertions.*;

public class PersistentLinkedListTest {
    @Test
    public void addFirstTest() {
        var l0 = new PersistentLinkedList<Integer>();
        var l1 = l0.addFirst(2);
        var l2 = l1.addFirst(4);
        var l3 = l1.addFirst(5);
        var l4 = l2.addFirst(6);

        assertEquals(2, l1.get(0));
        assertNull(l1.get(1));

        assertEquals(4, l2.get(0));
        assertEquals(2, l2.get(1));
        assertNull(l2.get(2));

        assertEquals(5, l3.get(0));
        assertEquals(2, l3.get(1));
        assertNull(l3.get(2));

        assertEquals(6, l4.get(0));
        assertEquals(4, l4.get(1));
        assertEquals(2, l4.get(2));
        assertNull(l4.get(3));

    }

    @Test
    public void addLastTest() {
        var l0 = new PersistentLinkedList<Integer>();
        var l1 = l0.addLast(2);
        var l2 = l1.addLast(4);
        var l3 = l1.addLast(5);
        var l4 = l2.addLast(6);

        assertEquals(2, l1.get(0));
        assertNull(l1.get(1));

        assertEquals(2, l2.get(0));
        assertEquals(4, l2.get(1));
        assertNull(l2.get(2));

        assertEquals(2, l3.get(0));
        assertEquals(5, l3.get(1));
        assertNull(l3.get(2));

        assertEquals(2, l4.get(0));
        assertEquals(4, l4.get(1));
        assertEquals(6, l4.get(2));
        assertNull(l4.get(3));
    }

    @Test
    public void containsTest() {
        var l0 = new PersistentLinkedList<Integer>();
        var l1 = l0.addLast(2);

        assertTrue(l1.contains(2));
        assertFalse(l0.contains(2));
    }

    @Test
    public void removeFirstTest() {
        var l0 = new PersistentLinkedList<Integer>();
        var l1 = l0.addFirst(2);
        var l2 = l1.addFirst(4);
        var l3 = l2.addFirst(6);

        var l4 = l2.removeFirst();
        var l5 = l3.removeFirst();

        var l6 = l5.addFirst(8);
        var l7 = l6.removeFirst();

        var l8 = l0.removeFirst();

        assertEquals(2, l4.get(0));
        assertNull(l4.get(1));

        assertEquals(4, l5.get(0));
        assertEquals(2, l5.get(1));
        assertNull(l5.get(2));

        assertEquals(4, l7.get(0));
        assertEquals(2, l7.get(1));
        assertNull(l7.get(2));

        assertEquals(0, l8.size());
    }

    @Test
    public void removeLastTest() {
        var l0 = new PersistentLinkedList<Integer>();
        var l1 = l0.addLast(2);
        var l2 = l1.addLast(4);
        var l3 = l2.addLast(6);

        var l4 = l2.removeLast();
        var l5 = l3.removeLast();

        var l6 = l5.addLast(8);
        var l7 = l6.removeLast();

        var l8 = l0.removeLast();

        assertEquals(2, l4.get(0));
        assertNull(l4.get(1));

        assertEquals(2, l5.get(0));
        assertEquals(4, l5.get(1));
        assertNull(l5.get(2));

        assertEquals(2, l7.get(0));
        assertEquals(4, l7.get(1));
        assertNull(l7.get(2));

        assertEquals(0, l8.size());
    }

    @Test
    public void clearTest() {
        var l0 = new PersistentLinkedList<Integer>();
        var l1 = l0.addLast(2);
        var l2 = l1.addLast(4);

        var l3 = l1.clear();
        var l4 = l2.clear();

        assertEquals(0, l3.size());
        assertEquals(0, l4.size());

        assertNull(l3.get(0));
        assertNull(l4.get(0));
    }

    @Test
    public void replaceTest() {
        var l0 = new PersistentLinkedList<Integer>();
        var l1 = l0.addLast(2);
        var l2 = l1.replace(0, 4);
        var l3 = l2.replace(0, 6);

        var l4 = l2.addLast(8);
        var l5 = l4.replace(1, 10);

        assertEquals(2, l1.get(0));
        assertNull(l1.get(1));

        assertEquals(4, l2.get(0));
        assertNull(l2.get(1));

        assertEquals(6, l3.get(0));
        assertNull(l3.get(1));

        assertEquals(4, l4.get(0));
        assertEquals(8, l4.get(1));
        assertNull(l4.get(2));

        assertEquals(4, l5.get(0));
        assertEquals(10, l5.get(1));
        assertNull(l5.get(2));
    }

    @Test
    public void undoTest() {
        var l0 = new PersistentLinkedList<Integer>();
        var l1 = l0.addLast(2);
        var l2 = l1.replace(0, 4);
        var l3 = l2.replace(0, 6);
        var l4 = l3.addLast(8);
        var l5 = l4.removeLast();

        assertEquals(2, l1.get(0));
        assertNull(l1.get(1));

        assertEquals(4, l2.get(0));
        assertNull(l2.get(1));

        assertEquals(6, l3.get(0));
        assertNull(l3.get(1));

        var l6 = l2.undo();
        var l7 = l3.undo();
        var l8 = l5.undo();

        assertEquals(2, l6.get(0));
        assertNull(l6.get(1));

        assertEquals(4, l7.get(0));
        assertNull(l7.get(1));

        assertEquals(6, l8.get(0));
        assertEquals(8, l8.get(1));
        assertNull(l8.get(2));
    }

    @Test
    public void redoTest() {
        var l0 = new PersistentLinkedList<Integer>();
        var l1 = l0.addLast(2);
        var l2 = l1.replace(0, 4);
        var l3 = l2.replace(0, 6);
        var l4 = l3.addLast(8);
        var l5 = l4.removeLast();

        assertEquals(2, l1.get(0));
        assertNull(l1.get(1));

        assertEquals(4, l2.get(0));
        assertNull(l2.get(1));

        assertEquals(6, l3.get(0));
        assertNull(l3.get(1));

        var l6 = l2.undo();
        var l7 = l3.undo();
        var l8 = l5.undo();

        var l9 = l6.redo();
        var l10 = l7.redo();
        var l11 = l8.redo();

        assertEquals(4, l9.get(0));
        assertNull(l9.get(1));

        assertEquals(6, l10.get(0));
        assertNull(l10.get(1));

        assertEquals(6, l11.get(0));
        assertNull(l11.get(1));
    }

    @Test
    public void toPersistentArray() {
        var l0 = new PersistentLinkedList<Integer>();
        var l1 = l0.addFirst(2);
        var l2 = l1.addFirst(4);
        var l3 = l2.addFirst(5);

        var arr0 = l3.toPersistentArray();

        assertEquals(5, arr0.get(0));
        assertEquals(4, arr0.get(1));
        assertEquals(2, arr0.get(2));
        assertThrows(IndexOutOfBoundsException.class, () -> arr0.get(3));
    }
}
