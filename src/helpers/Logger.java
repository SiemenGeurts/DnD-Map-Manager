package helpers;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import gui.Dialogs;

public class Logger {

	private static PrintStream printer;
	private static PrintStream oldSysErr;
	public Logger(String logPath) {
		try {
			printer = new PrintStream(new FileOutputStream(logPath), true);
			oldSysErr = System.err;
			System.setErr(printer);
		} catch (FileNotFoundException e) {
			Dialogs.warning("Could not create log file.", false);
		}
	}
	
	public static void println(String s) {
		Logger.println(s);
		if(printer != null)
			printer.println(s);
	}
	
	public static void println(Object obj) {
		Logger.println(obj);
		if(printer != null)
			printer.println(obj);
	}
	
	public static void error(Exception e) {
		e.printStackTrace(oldSysErr);
		e.printStackTrace(printer);
	}
}
//