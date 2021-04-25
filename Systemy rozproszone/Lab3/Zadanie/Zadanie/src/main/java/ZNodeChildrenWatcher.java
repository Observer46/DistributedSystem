import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

public class ZNodeChildrenWatcher implements Watcher {
    private final String watchedPath;
    private final ZNodeWatcher watcher;
    private final ZooKeeper zooKeeper;

    public ZNodeChildrenWatcher(ZNodeWatcher watcher, ZooKeeper zooKeeper, String watchedPath) throws KeeperException, InterruptedException {
        this.watcher = watcher;
        this.zooKeeper = zooKeeper;
        this.watchedPath = watchedPath;
        addWatcherToChildren(watchedPath);
    }

    public void addWatcherToChildren(String nodePath) throws KeeperException, InterruptedException {
        if(zooKeeper.exists(nodePath, null) != null){
            List<String> children = zooKeeper.getChildren(nodePath, this);
            for(String child : children)
                addWatcherToChildren(nodePath + "/" + child);
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        try {
            addWatcherToChildren(watchedPath);
            if(watchedEvent.getType().equals(Event.EventType.NodeChildrenChanged))
                this.watcher.printChildrenCount();

        } catch ( InterruptedException e){
            e.printStackTrace();
        }catch (KeeperException ignored) { }
    }
}
