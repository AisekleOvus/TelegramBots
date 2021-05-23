import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class test {
	public static void main(String[] args) throws Exception { 
		ScheduledExecutorService sched = Executors.newScheduledThreadPool(1);
		sched.schedule(() -> {
			for(int i=0; i < 60; i++) {
				try {
				Thread.sleep(1000);
				System.out.println(i);
				}catch(Exception e) {}
			}
		}, 10, TimeUnit.SECONDS);
		sched.shutdown();
		while(!sched.isTerminated()) {
			Thread.sleep(1000);
		}
		System.out.println("Done ! sched is shutdowned");
	}
}