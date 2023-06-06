import org.junit.jupiter.api.Test;
import persistence.structure.array.PersistentArray;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PersistentArrayTest {
    @Test
    public void addTest() {
        var arr0 = new PersistentArray<Integer>();

        var arr1 = arr0.add(3);
        var arr2 = arr1.add(5);
        var arr3 = arr2.add(6);
        var arr4 = arr1.add(7);

        assertThrows(IndexOutOfBoundsException.class, () -> arr0.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> arr0.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> arr0.get(2));

        assertEquals(arr1.get(0), 3);
        assertThrows(IndexOutOfBoundsException.class, () -> arr1.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> arr1.get(2));

        assertEquals(arr2.get(0), 3);
        assertEquals(arr2.get(1), 5);
        assertThrows(IndexOutOfBoundsException.class, () -> arr2.get(2));

        assertEquals(arr3.get(0), 3);
        assertEquals(arr3.get(1), 5);
        assertEquals(arr3.get(2), 6);

        assertEquals(arr4.get(0), 3);
        assertEquals(arr4.get(1), 7);
        assertThrows(IndexOutOfBoundsException.class, () -> arr4.get(2));
    }

    @Test
    public void insertTest() {
        var arr0 = new PersistentArray<Integer>();

        var arr1 = arr0.insert(0, 3);
        var arr2 = arr1.insert(0, 5);
        var arr3 = arr2.insert(1, 6);
        var arr4 = arr1.insert(1, 7);

        assertThrows(IndexOutOfBoundsException.class, () -> arr0.insert(1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> arr0.insert(-1, 0));

        assertThrows(IndexOutOfBoundsException.class, () -> arr0.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> arr0.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> arr0.get(2));

        assertEquals(3, arr1.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> arr1.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> arr1.get(2));

        assertEquals(5, arr2.get(0));
        assertEquals(3, arr2.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> arr2.get(2));

        assertEquals(5, arr3.get(0));
        assertEquals(6, arr3.get(1));
        assertEquals(3, arr3.get(2));

        assertEquals(3, arr4.get(0));
        assertEquals(7, arr4.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> arr4.get(2));
    }

    @Test
    public void replaceTest() {
        var arr0 = new PersistentArray<Integer>();

        var arr1 = arr0.add(3);
        var arr2 = arr1.add(5);
        var arr3 = arr2.add(6);
        var arr6 = arr1.add(8);

        var arr4 = arr6.replace(0, 9);
        var arr5 = arr3.replace(1, 1);

        assertThrows(IndexOutOfBoundsException.class, () -> arr0.replace(1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> arr0.replace(-1, 0));

        assertEquals(9, arr4.get(0));
        assertEquals(8, arr4.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> arr4.get(2));

        assertEquals(3, arr5.get(0));
        assertEquals(1, arr5.get(1));
        assertEquals(6, arr5.get(2));
    }

    @Test
    public void removeTest() {
        var arr0 = new PersistentArray<Integer>();

        var arr1 = arr0.add(3);
        var arr2 = arr1.add(5);
        var arr3 = arr2.add(6);
        var arr7 = arr1.add(7);

        var arr4 = arr2.remove(0);
        var arr5 = arr7.remove(1);
        var arr6 = arr3.remove(1);

        assertEquals(5, arr4.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> arr4.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> arr4.get(2));

        assertEquals(3, arr5.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> arr5.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> arr5.get(2));

        assertEquals(3, arr6.get(0));
        assertEquals(6, arr6.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> arr6.get(2));
    }

    @Test
    public void clearTest() {
        var arr0 = new PersistentArray<Integer>();

        var arr1 = arr0.add(3);
        var arr2 = arr1.add(5);
        var arr3 = arr2.add(6);

        var arr4 = arr2.clearAll();
        var arr5 = arr3.clearAll();

        assertThrows(IndexOutOfBoundsException.class, () -> arr4.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> arr4.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> arr4.get(2));

        assertThrows(IndexOutOfBoundsException.class, () -> arr5.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> arr5.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> arr5.get(2));
    }

    @Test
    public void undoTest() {
        var arr0 = new PersistentArray<Integer>();

        var arr1 = arr0.add(3);
        var arr2 = arr1.add(5);
        var arr3 = arr2.add(6);

        var arr4 = arr0.undo();
        var arr5 = arr2.undo();
        var arr6 = arr3.undo();

        assertEquals(arr0, arr4);

        assertEquals(3, arr5.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> arr5.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> arr5.get(2));

        assertEquals(3, arr6.get(0));
        assertEquals(5, arr6.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> arr6.get(2));
    }

    @Test
    public void redoTest() {
        var arr0 = new PersistentArray<Integer>();

        var arr1 = arr0.add(3);
        var arr2 = arr1.add(5);
        var arr3 = arr2.add(6);

        var arr4 = arr3.redo();
        var arr5 = arr0.redo();
        var arr6 = arr2.redo();

        assertEquals(arr3, arr4);

        assertEquals(3, arr5.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> arr5.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> arr5.get(2));

        assertEquals(3, arr6.get(0));
        assertEquals(5, arr6.get(1));
        assertEquals(6, arr6.get(2));
    }

    @Test
    public void toPersistentLinkedListTest() {
        var arr0 = new PersistentArray<Integer>();

        var arr1 = arr0.add(3);
        var arr2 = arr1.add(5);
        var arr3 = arr2.add(6);
        var arr4 = arr3.add(8);

        var l1 = arr4.toPersistentLinkedList();

        assertEquals(3, l1.get(0));
        assertEquals(5, l1.get(1));
        assertEquals(6, l1.get(2));
        assertEquals(8, l1.get(3));
    }
}
