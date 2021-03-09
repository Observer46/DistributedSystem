package Chat;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;

public class ChatTerminal {

    private Terminal terminal;
    private StringBuilder inputMsgBuffer;
    private String stringToPrint = null;

    public ChatTerminal() throws IOException {
        this.terminal = new DefaultTerminalFactory().createTerminal();
        this.inputMsgBuffer = new StringBuilder();
    }

    public void putChar() throws IOException {
        KeyStroke key =  this.terminal.pollInput();
        if(key == null)
            return;

        if (key.getKeyType().equals(KeyType.Enter))
            this.updateStringToPrint();
        else if (key.getKeyType().equals(KeyType.Backspace)){
            this.eraseChar();
        }
        else {
            this.inputMsgBuffer.append(key.getCharacter());
            this.terminal.putCharacter(key.getCharacter());
            this.terminal.flush();
        }
    }

    public void eraseChar() throws IOException {
        if(this.inputMsgBuffer.length() > 0) {
            TerminalPosition curPos = this.terminal.getCursorPosition();
            this.terminal.setCursorPosition(curPos.getColumn() - 1, curPos.getRow());
            this.terminal.putCharacter(' ');
            this.terminal.setCursorPosition(curPos.getColumn() - 1, curPos.getRow());
            this.inputMsgBuffer.deleteCharAt(this.inputMsgBuffer.length() - 1);
            this.terminal.flush();
        }
    }

    public void printToTerminal(String text) throws IOException {
        String[] lines = text.split("\n");
        boolean firstLine = true;
        for(String line : lines) {
            this.terminal.putString(line);
            if(firstLine){
                int fillIn = this.inputMsgBuffer.length() + 2 - line.length();
                StringBuilder blanks = new StringBuilder();
                for(int i=0; i < fillIn; i++)
                    blanks.append(" ");
                this.terminal.putString(blanks.toString());
            }
            this.terminal.putCharacter('\n');
            firstLine = false;
        }
        this.terminal.flush();
    }

    public String getStringToPrint(){
        String text = this.stringToPrint;
        this.stringToPrint = null;
        return text;
    }

    public void rewindCursor() throws IOException {
        this.terminal.setCursorPosition( 0, this.terminal.getCursorPosition().getRow());
    }

    public void updateStringToPrint(){
        this.stringToPrint = this.inputMsgBuffer.toString();
        this.inputMsgBuffer = new StringBuilder();
    }

    public void printBufferForClient(String text) throws IOException {
        this.rewindCursor();
        this.printToTerminal(text);
        this.printCursor();
        this.terminal.putString(this.inputMsgBuffer.toString());
        this.terminal.flush();
    }

    public String getInputBlocking() throws IOException {
        KeyStroke key =  this.terminal.readInput();
        while(!key.getKeyType().equals(KeyType.Enter)){
            if (key.getKeyType().equals(KeyType.Backspace)){
                this.eraseChar();
            }
            else {
                this.inputMsgBuffer.append(key.getCharacter());
                this.terminal.putCharacter(key.getCharacter());
                this.terminal.flush();
            }
            key = this.terminal.readInput();
        }
        String text = this.inputMsgBuffer.toString();
        this.printBufferForClient(text + "  ");
        this.inputMsgBuffer = new StringBuilder();
        return text;
    }

    public void printCursor() throws IOException {
        this.terminal.putString("> ");
        this.terminal.flush();
    }

    public void close() throws IOException {
        this.terminal.close();
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        ChatTerminal terminal = new ChatTerminal();
        terminal.printToTerminal("Hello boi!");
        terminal.printToTerminal("This tis terminal test");
        terminal.printCursor();
        while(true){
            terminal.putChar();
            Thread.sleep(1);
        }
    }
}
