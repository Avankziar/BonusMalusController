package main.java.me.avankziar.bmc.spigot.cbm;

import java.util.LinkedHashMap;

import main.java.me.avankziar.bmc.spigot.cmdtree.BaseConstructor;

public class Bypass
{
	public enum Permission
	{
		//Here Condition and BypassPermission.
		OTHERPLAYER;
		
		public String getCondition()
		{
			return BaseConstructor.getPlugin().pluginName.toLowerCase()+"-"+this.toString().toLowerCase();
		}
	}
	private static LinkedHashMap<Bypass.Permission, String> mapPerm = new LinkedHashMap<>();
	
	public static void set(Bypass.Permission bypass, String perm)
	{
		mapPerm.put(bypass, perm);
	}
	
	public static String get(Bypass.Permission bypass)
	{
		return mapPerm.get(bypass);
	}
	
	public enum Counter
	{
		//Here BonusMalus and CountPermission Things
		;
		
		public String getBonusMalus()
		{
			return BaseConstructor.getPlugin().pluginName.toLowerCase()+"-"+this.toString().toLowerCase();
		}
	}
	
	private static LinkedHashMap<Bypass.Counter, String> mapCount = new LinkedHashMap<>();
	
	public static void set(Bypass.Counter bypass, String perm)
	{
		mapCount.put(bypass, perm);
	}
	
	public static String get(Bypass.Counter bypass)
	{
		return mapCount.get(bypass);
	}
}