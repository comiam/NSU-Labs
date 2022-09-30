package comiam.sapper.ui;

public interface GameViewController
{
    void markCell(int x, int y);
    void markMaybeCell(int x, int y);
    void offMarkOnCell(int x, int y);
    void freeCell(int x, int y);
    void setPause();
    void setNumCell(int x, int y, int num);
    void repaintTimer();
    void repaintFlag();
    void disableGame(byte[][] map);
    boolean restartGame();
    boolean rebuildField();
    void noticeOverGame();
    void noticeWinGame();
    boolean isGUI();
    //Only for text mode
    void update(boolean makeOnlyOutSymbol);
}
