import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AppMonitor {
    private final String appPath;
    private Process process = null;

    public AppMonitor(final String appPath){
        this.appPath = appPath;
    }

    public void launchApp() {
        if(this.process != null)
            return;
        try {
            this.process = Runtime.getRuntime().exec(this.appPath);
        } catch (IOException e){
            System.out.println("Failed to launch app!");
        }
    }

    public void closeApp() {
        if(this.process == null)
            return;
        this.process.destroy();

        try {
            this.process.waitFor(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("Forcibly closing app");
        }

        if(this.process.isAlive())
            this.process.destroyForcibly();
        this.process = null;
    }
}
