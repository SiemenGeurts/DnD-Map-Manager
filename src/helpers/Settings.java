package helpers;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import gui.ErrorHandler;

public class Settings {

	public static final Setting<Boolean> FIRSTTIME = new Setting<>("firsttime", true);
	public static final Setting<Integer> SERVERPORT = new Setting<>("port", 5000);
	public static final Setting<Integer> CLIENTPORT = new Setting<>("port", 5000);
	public static final Setting<String> IP = new Setting<>("ip", "localhost");
	public static final Setting<Boolean> SERVERSELECTED = new Setting<>("serverselected", true);
	public static class Setting<T> {
		
		T def;
		String key;
		
		Setting(String name, T def) {
			this.def = def;
			key = name;
		}

		public String getKey() {
			return key;
		}
		
		public Class<?> getType() {
			return def.getClass();
		}
		
		public T getDefault() {
			return def;
		}
	}
	
	static PreferenceManager manager = new PreferenceManager();
		
	/**
	 * Sets the value of the given setting to given value.
	 * 
	 * @param setting the <tt>Setting</tt> whose value will be changed.
	 * @param value   the new value of <tt>setting</tt>.
	 */
	public static <T> void set(Setting<T> setting, T value) {
		if (value == null)
			return;
		if (value.getClass().equals(setting.getType())) {
			manager.set(setting, value);
		} else
			throw new IllegalArgumentException("Setting " + setting.toString().toLowerCase() + " expects value of type "
					+ setting.getType() + ", got " + Utils.type(value));
	}
	
	public static int getInt(Setting<Integer> setting) {
		return manager.getInt(setting.getKey(), setting.getDefault());
	}

	public static double getDouble(Setting<Double> setting) {
		return manager.getDouble(setting.getKey(), setting.getDefault());
	}

	public static boolean getBool(Setting<Boolean> setting) {
		return manager.getBoolean(setting.getKey(), setting.getDefault());
	}

	public static String getString(Setting<String> setting) {
		return manager.getString(setting.getKey(), setting.getDefault());
	}

	public static void loadPrefs() {
		if (getBool(FIRSTTIME)) {
			manager.set(FIRSTTIME, false);
			Logger.println("Couldn't find settings, using defaults.");
			resetVariables();
			return;
		}
		Logger.println("Settings were loaded.");
	}

	public static void resetVariables() {
		manager.set(FIRSTTIME, FIRSTTIME.getDefault());
		manager.set(SERVERPORT, SERVERPORT.getDefault());
		manager.set(CLIENTPORT, CLIENTPORT.getDefault());
		manager.set(SERVERSELECTED, SERVERSELECTED.getDefault());
		manager.set(IP, IP.getDefault());
		try {
			manager.flush();
		} catch (BackingStoreException e) {
			ErrorHandler.handle("Failed to save preferences!", e);
		}
	}
		
	private static class PreferenceManager {
		private Preferences pref;
		
		public PreferenceManager() {
			pref = Preferences.userRoot().node("/preferences");
		}
		
		public <T> void set(Setting<T> setting, T value) {
			if (setting.getType().equals(Integer.class))
				pref.putInt(setting.getKey(), (Integer) value);
			else if (setting.getType().equals(Double.class))
				pref.putDouble(setting.getKey(), (Double) value);
			else if (setting.getType().equals(String.class))
				pref.put(setting.getKey(), (String) value);
			else if (setting.getType().equals(Boolean.class))
				pref.putBoolean(setting.getKey(), (Boolean) value);
		}
		
		public void flush() throws BackingStoreException {
			pref.flush();
		}
		
		public int getInt(String key, int def) {
			return pref.getInt(key, def);
		}
		
		public boolean getBoolean(String key, boolean def) {
			return pref.getBoolean(key, def);
		}
		
		public String getString(String key, String def) {
			return pref.get(key, def);
		}
		
		public double getDouble(String key, double def) {
			return pref.getDouble(key, def);
		}
	}
}