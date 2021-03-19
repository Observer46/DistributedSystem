package expedition;

import java.io.IOException;

public interface User {
    void initialize() throws IOException;
    void runListenerThreads() throws IOException;
}
