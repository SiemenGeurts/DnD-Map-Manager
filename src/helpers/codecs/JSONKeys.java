package helpers.codecs;

public class JSONKeys {
	public static final String KEY_PREFAB_ENTITY = "prefab_entity";
	public static final String KEY_INITIATIVELIST = "initiativelist";
	public static final String KEY_FOW_MASK = "fow_mask";
	public static final String KEY_SET_TILE = "set_tile";
	public static final String KEY_MOVEMENT = "move";
	public static final String KEY_SET_BLOODIED = "bloodied";
	public static final String KEY_CLEAR_TILE = "clear";
	public static final String KEY_ADD_ENTITY = "add_entity";
	public static final String KEY_ENTITY = "entity";
	public static final String KEY_CHANGE_LEVEL = "change_level";
	public static final String KEY_UPDATE_LIST = "list";
	public static final String KEY_DISCONNECT = "disconnect";
	public static final String KEY_REQUEST_RESYNC = "request_resync";
	public static final String KEY_EMPTY = "empty";
	
	public static final String KEY_ADD_INITIATIVE ="add_init";
	public static final String KEY_CLEAR_INITIATIVE ="clear_init";
	
	public enum StringKey {
		KEY_TEMP("temp");
		
		private String key;
		StringKey(String key) {
			this.key = key;
		}
		
		public String get() {
			return key;
		}
	}
	
	public enum IntKey {
		KEY_NUM_TEXTURES("num_textures"),
		KEY_JSON_VERSION("version"),
		KEY_REQUEST_TEXTURE("texture"),
		KEY_REMOVE_ENTITY("remove_entity"),
		KEY_REMOVE_INITIATIVE("remove_initiative"),
		KEY_SELECT_INITIATIVE("select_initiative");
		private String key;
		IntKey(String key) {
			this.key = key;
		}
		
		public String get() {
			return key;
		}
	}
	
	public enum BoolKey {
		KEY_PREVIEW_CONFIRMATION("preview_confirmation");
		
		private String key;
		BoolKey(String key) {
			this.key = key;
		}
		
		public String get() {
			return key;
		}
	}
}
