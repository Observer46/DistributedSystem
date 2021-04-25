import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public class ZNodeWatcher implements Watcher{

    private final ZooKeeper zooKeeper;
    private final String watchedPath;
    private final AppMonitor appMonitor;
    private final ZNodeChildrenWatcher childrenWatcher;
    private boolean alive;

    public ZNodeWatcher(final String watchedPath, final AppMonitor appMonitor, final ZooKeeper zooKeeper) throws KeeperException, InterruptedException {
        this.watchedPath = watchedPath;
        this.appMonitor = appMonitor;
        this.zooKeeper = zooKeeper;
        this.childrenWatcher = new ZNodeChildrenWatcher(this, zooKeeper, watchedPath);
        this.alive = true;
        initialCheck();
    }

    public void initialCheck(){
        if(!this.alive) return;
        try {
            if(this.zooKeeper.exists(this.watchedPath, false) != null)
                this.nodeCreated();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException ignored) { }
    }
    
    public void nodeCreated() throws KeeperException, InterruptedException {
        if(!this.alive) return;
        System.out.println("ZNode: " + watchedPath + " has been created!");
        this.childrenWatcher.addWatcherToChildren(this.watchedPath);
        this.appMonitor.launchApp();
    }

    public void nodeDeleted(){
        if(!this.alive) return;
        System.out.println("ZNode: " + watchedPath + " has been deleted!");
        this.appMonitor.closeApp();
    }


    public void printTreeRecur(String path) throws KeeperException, InterruptedException {
        for(String child : this.zooKeeper.getChildren(path, false)){
            String childNodePath = path + "/" + child;
            System.out.println(childNodePath);
            printTreeRecur(childNodePath);
        }
    }

    public void printTree() throws KeeperException, InterruptedException {
        if(this.zooKeeper.exists(this.watchedPath, false) != null) {
            System.out.println(this.watchedPath);
            printTreeRecur(this.watchedPath);
        }
    }

    public int countChildren(String path) throws KeeperException, InterruptedException {
        int sum = 0;
        for(String child : this.zooKeeper.getChildren(path, false)){
            sum++;
            String childNodePath = path + "/" + child;
            sum += countChildren(childNodePath);
        }
        return sum;
    }

    public void printChildrenCount() throws KeeperException, InterruptedException {
        System.out.println("ZNode children count: " + countChildren(this.watchedPath));
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()){
            case NodeCreated:
                try {
                    nodeCreated();
                } catch (InterruptedException e){
                    e.printStackTrace();
                }catch (KeeperException ignored) { }

                break;
            case NodeDeleted:
                nodeDeleted();
                break;
            case NodeChildrenChanged:
                try {
                    printChildrenCount();
                    break;
                } catch ( InterruptedException e){
                    e.printStackTrace();
                }catch (KeeperException ignored) { }
        }

        try {
            zooKeeper.exists(this.watchedPath, this);
        } catch ( InterruptedException e){
            e.printStackTrace();
        }catch (KeeperException ignored) { }
    }

    public synchronized void setAlive(final boolean alive){
        this.alive = alive;
    }

    public synchronized boolean alive(){
        return this.alive;
    }
}