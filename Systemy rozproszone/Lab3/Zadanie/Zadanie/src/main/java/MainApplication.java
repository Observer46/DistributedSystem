import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.Scanner;

public class MainApplication {
    public static final String DEFAULT_APP = "C:\\Program Files (x86)\\Audacity\\audacity.exe";
    public static final String ZNODE_PATH = "/z";
    public static final String QUIT_COMMAND = "-quit";
    public static final String LIST_COMMAND = "-ls";
    public static final String[] COMMANDS = { QUIT_COMMAND, LIST_COMMAND };

    private final ZNodeWatcher watcher;
    private final AppMonitor monitor;
    private final ZooKeeper zooKeeper;
    private final Scanner scanner;

    public MainApplication(String nodePath, String execPath, String hostPath) throws IOException, KeeperException, InterruptedException {
        this.monitor = new AppMonitor(execPath);
        this.zooKeeper = new ZooKeeper(hostPath, 10000, null);
        this.watcher = new ZNodeWatcher(ZNODE_PATH, this.monitor, this.zooKeeper);
        this.zooKeeper.exists(nodePath, this.watcher);
        this.scanner = new Scanner(System.in);
        System.out.println("App watcher launched!");
    }

    public static void main(String[] args){
        if(args.length < 1){
            System.out.println("Wrong arg count!");
            System.out.println("Args: hostPath [exec_path]");
        }

        String hostPath = args[0];
        String execPath = args.length > 1 ? args[1] : DEFAULT_APP;

        try {
            MainApplication mainApp = new MainApplication(ZNODE_PATH, execPath, hostPath);
            mainApp.parseCommands();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            System.out.println("Keeper exception occurred - probably a problem with the connection!");
            System.exit(1);
        }
    }

    public void closeAll() throws InterruptedException {
        if(this.zooKeeper != null)
            this.zooKeeper.close();
        if(this.watcher != null)
            this.watcher.setAlive(false);
        if(this.monitor != null)
            this.monitor.closeApp();
    }

    public void parseCommands(){
        while(this.watcher.alive()){
            String command = this.scanner.nextLine();
            try {
                processCommand(command);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (KeeperException ignored) {}
        }
    }

    public void processCommand(String command) throws KeeperException, InterruptedException {
        if(command.equals(QUIT_COMMAND))
            this.closeAll();
        else if(command.equals(LIST_COMMAND))
            this.watcher.printTree();
        else{
            System.out.println("Unknown command: " + command);
            System.out.print("Command list: ");
            for(String cmd : COMMANDS)
                System.out.print(cmd + " ");
            System.out.println();
        }
    }
}
