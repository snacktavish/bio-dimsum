
public class Timer {
	
	private final static boolean ENABLED = true;
	private final static boolean VERBOSE = true;
	
	private String name;
	private long tmpTime = 0;
	private long overallTime = 0;
	private boolean running = false;
	
	public Timer(String name) {
		this.name = name;
	}
	
	
	public void start() {
		if(ENABLED) {
			if(!running) {
				tmpTime = System.nanoTime();
				running = true;
			} else
				System.err.println("Timer "+name+ " allready running");
		}
	}
	
	public long stop() {
		if(ENABLED) {
			if(running) {
				long diff = System.nanoTime() - tmpTime;
				overallTime += diff;
				tmpTime = 0;
				if (VERBOSE) System.out.println(name + ": "+ diff/1000/1000 + "ms");
				running = false;
				return diff;
			} else {
				System.err.println("Timer "+name+ " not running");
			}
		} 
		return 0;
	}
	
	public String toString() {
		return name + ": Overall: "+ overallTime/1000/1000 +"ms";
	}

}
