import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class TimerTaskUtility extends TimerTask{
	int timeoutValue;
	
	

	@Override
	public void run(){
		
		System.out.println("timer started"+new Date());
		/*try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		System.out.println("timer stopped"+new Date());
	}
	public static void main(String[] args){
		TimerTask util = new TimerTaskUtility();
		Timer timer = new Timer(true);
		timer.schedule(util, 5000);
		//timer.scheduleAtFixedRate(util,  1000);
		System.out.println("Timer task begins !!"+new Date());
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		timer.cancel();
		System.out.println("timer cancelled"+new Date());
	}
}
