package expedition;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

public class ChatTerminal {

    public static final int MAX_BUFFERED_LINES = 100;

    private final Terminal terminal;
    private StringBuilder inputMsgBuffer;
    private String stringToPrint = null;
    private final LinkedList<String> msgHistory = new LinkedList<>();

    public ChatTerminal(TextColor textColor) throws IOException {
        this.terminal = new DefaultTerminalFactory().createTerminal();
        this.terminal.setForegroundColor(textColor);
        this.inputMsgBuffer = new StringBuilder();
    }

    public void updateMsgHistory(String msg){
        this.msgHistory.addFirst(msg);
        if(this.msgHistory.size() > ChatTerminal.MAX_BUFFERED_LINES)
            this.msgHistory.removeLast();
    }

    public void scrollIfNeeded() throws IOException {
        int screenRowLimit = this.terminal.getTerminalSize().getRows();
        if(this.msgHistory.size() >= screenRowLimit){
            this.terminal.clearScreen();
            int j = 0;

            for(int i = screenRowLimit - 3; i >= 0; i--){
                String lineToPrint = this.msgHistory.get(j);
                this.terminal.setCursorPosition(0, i);
                this.printToTerminal(lineToPrint, false);
                j++;
            }

            this.terminal.setCursorPosition(0, screenRowLimit - 2);
        }
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
        else if(key.getCharacter() != null){
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
        this.printToTerminal(text, true);
    }

    public void printToTerminal(String text, boolean logMessage) throws IOException {
        String[] lines = text.split("\n");
        boolean firstLine = true;
        for(String line : lines) {
            this.terminal.putString(line);

            if(logMessage)
                this.updateMsgHistory(line);

            int fillIn = this.inputMsgBuffer.length() + 2 - line.length();
            if(firstLine && fillIn > 0){
                char[] fillArray = new char[fillIn];
                Arrays.fill(fillArray, ' ');
                String blanks = new String(fillArray);
                this.terminal.putString(blanks);
            }
            this.terminal.putCharacter('\n');
            firstLine = false;
        }

        this.terminal.flush();
        if(logMessage)
            this.scrollIfNeeded();
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
        this.inputMsgBuffer = new StringBuilder();
        this.printBufferForClient(text + "  ");
        return text;
    }

    public void printCursor() throws IOException {
        this.terminal.putString("> ");
        this.terminal.flush();
    }

    public void eraseCursor() throws IOException {
        this.terminal.setCursorPosition(0, this.terminal.getCursorPosition().getRow());
        this.terminal.putString("  ");
        this.terminal.setCursorPosition(0, this.terminal.getCursorPosition().getRow());
        this.terminal.flush();
    }

    public void close() throws IOException {
        this.terminal.close();
    }
}
