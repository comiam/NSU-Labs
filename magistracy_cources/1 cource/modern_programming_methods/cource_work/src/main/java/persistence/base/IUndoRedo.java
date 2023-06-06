package persistence.base;

public interface IUndoRedo<T> {
    T undo();

    T redo();
}
