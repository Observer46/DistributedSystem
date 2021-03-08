package Chat;


import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;

public class ChatTerminal {

    private Terminal terminal;
    private StringBuilder inputMsgBuffer;

    public ChatTerminal() throws IOException {
        this.terminal = new DefaultTerminalFactory().createTerminal();
        this.inputMsgBuffer = new StringBuilder();
    }

    public void putChar() throws IOException {
        KeyStroke key =  this.terminal.pollInput();
        if(key == null)
            return;

        if (key.getKeyType().equals(KeyType.Enter))
            //this.printToTerminal("YEET");
            this.printBufferToTerminal();
        else
            this.inputMsgBuffer.append(key.getCharacter());
        System.out.println("Key type: " + key.getKeyType().toString());
        System.out.println("Key pressed: " + key.getCharacter());
    }

    public void printToTerminal(String text) throws IOException {
        this.terminal.putString(text);
        this.terminal.putCharacter('\n');
        this.terminal.flush();
    }

    public void printBufferToTerminal() throws IOException {
        this.printToTerminal("Yeet: " + this.inputMsgBuffer.toString());
        this.inputMsgBuffer = new StringBuilder();
    }

    public void close() throws IOException {
        this.terminal.close();
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        ChatTerminal terminal = new ChatTerminal();
        terminal.printToTerminal("Hello boi!");
        terminal.printToTerminal("This tis terminal test");
        while(true){
            terminal.putChar();
            Thread.sleep(1);
        }
    }
}
