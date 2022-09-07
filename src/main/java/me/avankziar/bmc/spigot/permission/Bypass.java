package main.java.me.avankziar.bmc.spigot.permission;

import java.util.LinkedHashMap;

public class Bypass
{
	public enum Permission
	{
		OTHERPLAYER
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
}