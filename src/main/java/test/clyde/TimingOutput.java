package test.clyde;
import java.io.FileWriter;
import java.io.PrintWriter;

public class TimingOutput {
	private static final String fileName = "c:\\temp\\profiler.txt";
	private static PrintWriter writer;
	
	static {
		try {
			writer = new PrintWriter(new FileWriter(fileName), true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private TimingOutput() {
		
	}
	
	public static void addLine(long startTime, long endTime, String method, Thread thread) {
		synchronized (writer) {
			writer.println(String.format("%s\t%s\t%s(%s)\t[%s]", startTime, endTime, thread.getName(), thread.getId(), method));
		}
	}
	
	public static void close() {
		writer.close();
	}
}
