package socksproxy.connection;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public interface SocketHandler extends Handler {
    void read(SelectionKey key) throws IOException;
    void write(SelectionKey key) throws IOException;
}
