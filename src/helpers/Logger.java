package helpers;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import gui.Dialogs;

public class Logger {

	private static PrintStream printer;
	private static PrintStream oldSysErr;
	private static long startTime;
	
	public Logger(String logPath) {
		try {
			printer = new PrintStream(new FileOutputStream(logPath), true);
			oldSysErr = System.err;
			startTime = System.currentTimeMillis();
			//System.setErr(printer);
		} catch (FileNotFoundException e) {
			Dialogs.warning("Could not create log file.", false);
		}
	}
	
	public static void println(String s) {
		System.out.println(s);
		if(printer != null)
			printer.println(getTimeStamp() + s);
	}
	
	public static void println(Object obj) {
		System.out.println(obj);
		if(printer != null)
			printer.println(getTimeStamp() + obj.toString());
	}
	
	public static void error(Exception e) {
		e.printStackTrace(oldSysErr);
		e.printStackTrace(printer);
	}
	
	public static void error(String msg) {
		System.err.println(msg);
		printer.println(getTimeStamp() + "ERROR: " +msg);
	}
	
	private static String getTimeStamp() {
		return String.format("[%.2f] ", (System.currentTimeMillis() - startTime)/1000d);
	}
}