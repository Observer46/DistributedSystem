package expedition;

import com.googlecode.lanterna.TextColor;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public abstract class AbstractInputUser extends AbstractUser {

    public static final String EXIT_COMMAND = "-exit";

    protected AbstractInputUser(String role, TextColor chatColor) throws IOException, TimeoutException {
        super(role, chatColor);
    }

    protected void close() throws IOException {
        this.terminal.close();
        System.exit(0);
    }
}
