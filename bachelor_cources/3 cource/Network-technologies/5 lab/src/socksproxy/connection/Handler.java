package socksproxy.connection;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public interface Handler  {
    void close() throws IOException;
    void accept(SelectionKey key);
}
