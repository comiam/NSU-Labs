import org.junit.jupiter.api.Test;
import persistence.structure.map.PersistentMap;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class PersistentMapTest {
    @Test
    public void addTest() {
        var d0 = new PersistentMap<Integer, Integer>();
        var d1 = d0.add(3, 3);
        var d2 = d1.add(4, 4);
        var d4 = d1.add(5, 5);
        var d3 = d1.add(6, 6);

        assertNull(d0.get(3));
        assertNull(d0.get(4));
        assertNull(d0.get(5));
        assertNull(d0.get(6));

        assertEquals(3, d1.get(3));
        assertNull(d1.get(4));
        assertNull(d1.get(5));
        assertNull(d1.get(6));

        assertEquals(3, d2.get(3));
        assertEquals(4, d2.get(4));
        assertNull(d2.get(5));
        assertNull(d2.get(6));

        assertEquals(3, d3.get(3));
        assertNull(d3.get(4));
        assertNull(d3.get(5));
        assertEquals(6, d3.get(6));

        assertEquals(3, d4.get(3));
        assertNull(d4.get(4));
        assertEquals(5, d4.get(5));
        assertNull(d4.get(6));

        assertThrows(IllegalArgumentException.class, () -> d1.add(3, 5));
    }

    @Test
    public void removeTest() {
        var d0 = new PersistentMap<Integer, Integer>();
        var d1 = d0.add(3, 3);
        var d2 = d1.remove(3);
        var d3 = d1.add(5, 5);
        var d4 = d3.remove(3);

        assertEquals(3, d1.get(3));
        assertNull(d1.get(5));

        assertNull(d2.get(3));
        assertNull(d2.get(5));

        assertEquals(3, d3.get(3));
        assertEquals(5, d3.get(5));

        assertNull(d4.get(3));
        assertEquals(5, d4.get(5));
    }

    @Test
    public void clearTest() {
        var d0 = new PersistentMap<Integer, Integer>();
        var d1 = d0.add(3, 3);
        var d2 = d1.add(4, 4);
        var d3 = d2.add(5, 5);
        var d4 = d2.clear();
        var d5 = d3.clear();

        assertEquals(3, d1.get(3));
        assertNull(d1.get(4));
        assertNull(d1.get(5));

        assertEquals(3, d2.get(3));
        assertEquals(4, d2.get(4));
        assertNull(d2.get(5));

        assertEquals(3, d3.get(3));
        assertEquals(4, d3.get(4));
        assertEquals(5, d3.get(5));

        assertNull(d4.get(3));
        assertNull(d4.get(4));
        assertNull(d4.get(5));

        assertNull(d5.get(3));
        assertNull(d5.get(4));
        assertNull(d5.get(5));
    }

    @Test
    public void keyValueSetTest() {
        var d0 = new PersistentMap<String, Integer>();
        var d1 = d0.add("Lol", 3);
        var d2 = d1.add("Kek", 4);
        var d3 = d2.add("heh", 5);

        assertEquals(d1.keySet(), Set.of("Lol"));
        assertEquals(d2.keySet(), Set.of("Lol", "Kek"));
        assertEquals(d3.keySet(), Set.of("Lol", "Kek", "heh"));

        assertEquals(d1.valueSet(), Set.of(3));
        assertEquals(d2.valueSet(), Set.of(3, 4));
        assertEquals(d3.valueSet(), Set.of(3, 4, 5));
    }

    @Test
    public void replaceTest() {
        var d0 = new PersistentMap<Integer, Integer>();
        var d1 = d0.add(3, 3);
        var d2 = d1.add(4, 4);
        var d3 = d2.replace(3, 5);
        var d4 = d2.replace(4, 6);

        assertEquals(3, d1.get(3));
        assertNull(d1.get(4));

        assertEquals(3, d2.get(3));
        assertEquals(4, d2.get(4));

        assertEquals(5, d3.get(3));
        assertEquals(4, d3.get(4));

        assertEquals(3, d4.get(3));
        assertEquals(6, d4.get(4));

        assertThrows(IllegalArgumentException.class, () -> d1.replace(2, 8));
    }

    @Test
    public void undoTest() {
        var d0 = new PersistentMap<Integer, Integer>();
        var d1 = d0.add(3, 3);
        var d2 = d1.add(4, 4);
        var d3 = d2.replace(3, 5);
        var d4 = d3.remove(4);
        var d5 = d3.clear();

        var d6 = d0.undo();
        var d7 = d2.undo();
        var d8 = d3.undo();
        var d9 = d4.undo();
        var da = d5.undo();

        assertEquals(d0, d6);

        assertEquals(d1.get(3), d7.get(3));
        assertEquals(d1.get(4), d7.get(4));

        assertEquals(d2.get(3), d8.get(3));
        assertEquals(d2.get(4), d8.get(4));

        assertEquals(d3.get(3), d9.get(3));
        assertEquals(d3.get(4), d9.get(4));

        assertEquals(d3.get(3), da.get(3));
        assertEquals(d3.get(4), da.get(4));
    }

    @Test
    public void redoTest() {
        var d0 = new PersistentMap<Integer, Integer>();
        var d1 = d0.add(3, 3);
        var d2 = d1.add(4, 4);
        var d3 = d2.replace(3, 5);
        var d4 = d3.remove(4);
        var d5 = d4.clear();

        var d6 = d5.redo();
        var d7 = d0.redo();
        var d8 = d1.redo();
        var d9 = d2.redo();
        var da = d3.redo();

        assertEquals(d5, d6);

        assertEquals(d1.get(3), d7.get(3));
        assertEquals(d1.get(4), d7.get(4));

        assertEquals(d2.get(3), d8.get(3));
        assertEquals(d2.get(4), d8.get(4));

        assertEquals(d3.get(3), d9.get(3));
        assertEquals(d3.get(4), d9.get(4));

        assertEquals(d4.get(3), da.get(3));
        assertEquals(d4.get(4), da.get(4));
    }

    @Test
    public void toPersistentArrayTest() {
        var d0 = new PersistentMap<Integer, Integer>();
        var d1 = d0.add(3, 3);
        var d2 = d1.add(4, 4);
        var d3 = d2.add(5, 5);
        var d4 = d3.add(6, 6);

        var arr = d4.toPersistentArray();

        assertEquals(3, arr.get(0));
        assertEquals(4, arr.get(1));
        assertEquals(5, arr.get(2));
        assertEquals(6, arr.get(3));
    }

    @Test
    public void toPersistentLinkedListTest() {
        var d0 = new PersistentMap<Integer, Integer>();
        var d1 = d0.add(3, 3);
        var d2 = d1.add(4, 4);
        var d3 = d2.add(5, 5);
        var d4 = d3.add(6, 6);

        var arr = d4.toPersistentLinkedList();

        assertEquals(3, arr.get(0));
        assertEquals(4, arr.get(1));
        assertEquals(5, arr.get(2));
        assertEquals(6, arr.get(3));
    }
}
