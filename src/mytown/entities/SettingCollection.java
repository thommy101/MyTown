package mytown.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mytown.MyTown;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import com.google.common.base.Joiner;
import com.sperion.forgeperms.ForgePerms;

/**
 * Holds a list of all Settings and allows inheritance
 * @author Joe Goett
 */
public class SettingCollection {
	public Object tag;
    public ISettingsSaveHandler saveHandler;
	private SettingCollection parent;
	private List<SettingCollection> children = new ArrayList<SettingCollection>();
	private Map<String, Setting> settings = new HashMap<String, Setting>();
	
	/**
	 * Sets the parent
	 * @param parent
	 */
	public void setParent(SettingCollection parent){
		if (this.parent != null) this.parent.removeChild(this);
		this.parent = parent;
		if (this.parent != null) this.parent.addChild(this);
	}
	
	/**
	 * Adds a child
	 * @param child
	 */
	protected void addChild(SettingCollection child){
		children.add(child);
	}
	
	/**
	 * Removes a child
	 * @param child
	 */
	protected void removeChild(SettingCollection child){
		children.remove(child);
	}
	
	/**
	 * Returns the children list
	 * @return
	 */
	public List<SettingCollection> getChildren(){
		return children;
	}
	
	/**
	 * Returns the settings map
	 * @return
	 */
	public Map<String, Setting> getSettings(){
		return settings;
	}
	
	/**
	 * Shows all the permissions to the given ICommandSender
	 * @param cs
	 * @param title
	 * @param node
	 * @param all
	 */
    public void show(ICommandSender cs, String title, String node, boolean all) {
        EntityPlayer p = (EntityPlayer) cs;
    	Iterator<Setting> it = settings.values().iterator();
    	Setting set;
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("§6-- §ePermissions for %s§6 --\n", title));
        
        while (it.hasNext()){
        	set = it.next();
            if (all || ForgePerms.getPermissionManager().canAccess(p.getCommandSenderName(), p.worldObj.provider.getDimensionName(), "mytown.cmd.perm.set." + node + "." + set.getName())) {
            	builder.append(set.getDisplay());
            }
        }
        builder.append("§6----------------------------");
        
        MyTown.sendChatToPlayer(cs, builder.toString());
    }
	
    /**
     * Returns a Setting object of the given key
     * @param key
     * @return
     */
	public Setting getSetting(String key){
		return settings.get(key);
	}

	/**
	 * Unlinks all children of this SettingCollection
	 */
    public void unlinkAllDown() {
        setParent(null);

        for (SettingCollection child : children) {
            child.unlinkAllDown();
        }
    }
	
    /**
     * Returns the serialized version of all the settings in the collection
     * @return
     */
	public String serialize(){
		List<String> ret = new ArrayList<String>();
		Iterator<Setting> it = settings.values().iterator();
		Setting set;
		while (it.hasNext()){
			set = it.next();
            if (set.getValue() != null && set.getValue() != set.getDefaultValue()) {
                ret.add(set.serialize());
            }
		}
		return Joiner.on("/").join(ret);
	}
	
	/**
	 * Saves the settings
	 */
    public void save() {
        if (saveHandler != null) {
            saveHandler.save(this, tag);
        }
    }
	
    /**
     * Splits the given string and loads the appropriate setting
     * @param val
     */
	public void deserialize(String val){
        if (val == null || val.isEmpty() || val.equals("")) {
            return;
        }

        String[] splits = val.split("/");
        for (String line : splits) {
            String[] v = line.split(":");
            if (v.length != 2) {
                continue;
            }

            Setting set = getSetting(v[0]);
            if (set != null) {
                set.deserialize(v[1]);
            }
        }
	}
	
	/**
	 * Refreshes itself and all the children
	 */
	public void refresh(){
    	Iterator<Setting> it = settings.values().iterator();
    	Setting set;
    	while (it.hasNext()){
    		set = it.next();
            if (set.getValue() == null) {
                if (parent != null) {
                	set.setValue(parent.getSetting(set.getName()));
                }
            } else {
                set.resetValue();
            }
    	}
    	
    	refreshChildren();
	}
	
	/**
	 * Refreshes the children
	 */
	public void refreshChildren(){
		for(SettingCollection child : children){
			child.refresh();
		}
	}

    public interface ISettingsSaveHandler {
        void save(SettingCollection sender, Object tag);
    }
    
    /**
     * Helper function to generate the core settings
     * @return
     */
    public static SettingCollection generateCoreSettings(){
    	SettingCollection s = new SettingCollection();
        s.settings.put("carts", new Setting("carts", "Allow cart interaction", false, boolean.class));
        s.settings.put("steverailer", new Setting("steverailer", "Allow Stevescarts railers", false, boolean.class));
        s.settings.put("steveminer", new Setting("steveminer", "Allow Stevescarts miners", false, boolean.class));
        s.settings.put("rcbore", new Setting("rcbore", "Allow Railcraft bores", false, boolean.class));
        s.settings.put("bc", new Setting("bc", "Allow quarrys,filler,builders", false, boolean.class));
        s.settings.put("ccturtles", new Setting("ccturtles", "Allow Computercraft turtles", false, boolean.class));
        s.settings.put("tcbores", new Setting("tcbores", "Allow Thaumcraft Arcane Bores", false, boolean.class));
        s.settings.put("gravigunoff", new Setting("gravigunoff", "Disable Throwing of Blocks", true, boolean.class));
        s.settings.put("closeclaim", new Setting("closeclaim", "Allow claiming next to", false, boolean.class));
        s.settings.put("fireballoff", new Setting("fireballoff", "Disable FireBall/Wither explosion", true, boolean.class));
        s.settings.put("creepoff", new Setting("creepoff", "Disable creeper explosion", false, boolean.class));
        s.settings.put("tntoff", new Setting("tntoff", "Disable TNT explosion", true, boolean.class));
        s.settings.put("mobsoff", new Setting("mobsoff", "Disable mobs", false, boolean.class));
        s.settings.put("yon", new Setting("yon", "Height enabled", false, boolean.class));
        s.settings.put("yfrom", new Setting("yfrom", "Height check from", 0, int.class));
        s.settings.put("yto", new Setting("yto", "Height check to", 255, int.class));
    	return s;
    }
    
    /**
     * Helper function to generate the outsider settings
     * @return
     */
    public static SettingCollection generateOutsiderSettings(){
    	SettingCollection s = new SettingCollection();
    	//Boolean settings
    	s.settings.put("roam", new Setting("roam", "Allows them to enter", true, boolean.class));
    	s.settings.put("build", new Setting("build", "Allows them to build", false, boolean.class));
    	s.settings.put("loot", new Setting("loot", "Allows them to pickup items", false, boolean.class));
    	s.settings.put("open", new Setting("open", "Allows them to open doors, trapdoors, etc", false, boolean.class));
    	s.settings.put("activate", new Setting("activate", "Allows them to activate buttons, switches, etc", false, boolean.class));
    	s.settings.put("container", new Setting("container", "Allows them to open containers (chests, machines, etc)", false, boolean.class));
    	s.settings.put("attackmobs", new Setting("attackmobs", "allows them to attack mobs", false, boolean.class));
    	s.settings.put("attackcreatures", new Setting("attackcreatures", "allows them to attack creatures", false, boolean.class));
    	
    	//Array settings
    	s.settings.put("openList", new Setting("openList", "List of allowed blocks to be opened", null, List.class));
    	s.settings.put("activateList", new Setting("activateList", "List of allowed blocks to be activated", null, List.class));
    	s.settings.put("containerList", new Setting("containerList", "List of allowed containers to be accessed", null, List.class));
    	s.settings.put("attackmobsList", new Setting("attackmobsList", "List of allowed mobs to be attacked", null, List.class));
    	s.settings.put("attackcreaturesList", new Setting("attackcreaturesList", "List of allowed creatures to be attacked", null, List.class));
    	return s;
    }
    
    /**
     * Helper function to generate the town member settings
     * @return
     */
    public static SettingCollection generateTownMemberSettings(){
    	SettingCollection s = new SettingCollection();
    	//Boolean settings
    	s.settings.put("roam", new Setting("roam", "Allows them to enter", true, boolean.class));
    	s.settings.put("build", new Setting("build", "Allows them to build", true, boolean.class));
    	s.settings.put("loot", new Setting("loot", "Allows them to pickup items", true, boolean.class));
    	s.settings.put("open", new Setting("open", "Allows them to open doors, trapdoors, etc", true, boolean.class));
    	s.settings.put("activate", new Setting("activate", "Allows them to activate buttons, switches, etc", true, boolean.class));
    	s.settings.put("container", new Setting("container", "Allows them to open containers (chests, machines, etc)", true, boolean.class));
    	s.settings.put("attackmobs", new Setting("attackmobs", "allows them to attack mobs", true, boolean.class));
    	s.settings.put("attackcreatures", new Setting("attackcreatures", "allows them to attack creatures", true, boolean.class));
    	
    	//Array settings
    	s.settings.put("openList", new Setting("openList", "List of allowed blocks to be opened", null, List.class));
    	s.settings.put("activateList", new Setting("activateList", "List of allowed blocks to be activated", null, List.class));
    	s.settings.put("containerList", new Setting("containerList", "List of allowed containers to be accessed", null, List.class));
    	s.settings.put("attackmobsList", new Setting("attackmobsList", "List of allowed mobs to be attacked", null, List.class));
    	s.settings.put("attackcreaturesList", new Setting("attackcreaturesList", "List of allowed creatures to be attacked", null, List.class));
    	return s;
    }
}