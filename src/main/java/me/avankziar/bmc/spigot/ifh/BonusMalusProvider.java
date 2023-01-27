package main.java.me.avankziar.bmc.spigot.ifh;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import main.java.me.avankziar.bmc.spigot.BMC;
import main.java.me.avankziar.bmc.spigot.database.MysqlHandler;
import main.java.me.avankziar.bmc.spigot.objects.BonusMalus;
import main.java.me.avankziar.bmc.spigot.objects.BonusMalusBaseValue;
import main.java.me.avankziar.bmc.spigot.objects.BonusMalusValue;
import main.java.me.avankziar.ifh.general.bonusmalus.BonusMalusType;
import main.java.me.avankziar.ifh.general.bonusmalus.BonusMalusValueType;

public class BonusMalusProvider implements main.java.me.avankziar.ifh.general.bonusmalus.BonusMalus
{
	private BMC plugin;
	private static ArrayList<BonusMalus> registeredBM = new ArrayList<>();
	private static LinkedHashMap<UUID, LinkedHashMap<String, Double>> bmPerUUIDSUM = new LinkedHashMap<>();
	private static LinkedHashMap<UUID, LinkedHashMap<String, Double>> bmPerUUIDMUL = new LinkedHashMap<>();
	private static LinkedHashMap<UUID, LinkedHashMap<String, Double>> bmPerUUIDPerServerSUM = new LinkedHashMap<>();
	private static LinkedHashMap<UUID, LinkedHashMap<String, Double>> bmPerUUIDPerServerMUL = new LinkedHashMap<>();
	private static LinkedHashMap<UUID, LinkedHashMap<String, LinkedHashMap<String, Double>>> bmPerUUIDPerServerPerWorldSUM = new LinkedHashMap<>();
	private static LinkedHashMap<UUID, LinkedHashMap<String, LinkedHashMap<String, Double>>> bmPerUUIDPerServerPerWorldMUL = new LinkedHashMap<>();
	
	public BonusMalusProvider(BMC plugin)
	{
		this.plugin = plugin;
		if(registeredBM.isEmpty())
		{
			init();
		}
	}
	
	public void init()
	{
		ArrayList<BonusMalus> bmlist = BonusMalus.convert(plugin.getMysqlHandler()
				.getFullList(MysqlHandler.Type.BONUSMALUS, "`id`", "1"));
		registeredBM.addAll(bmlist);
		BMC.log.info(bmlist.size()+" Bonus/Malus are registered!");
	}
	
	public void join(UUID uuid)
	{
		ArrayList<BonusMalusValue> bmvlist = BonusMalusValue.convert(
				plugin.getMysqlHandler().getFullList(MysqlHandler.Type.BONUSMALUSVALUE, "`id` ASC",
						"`player_uuid` = ?", uuid.toString()));
		ArrayList<String> bmvl = new ArrayList<>();
		LinkedHashMap<String, Double> summap = new LinkedHashMap<>();
		LinkedHashMap<String, Double> mulmap = new LinkedHashMap<>();
		LinkedHashMap<String, Double> summapserver = new LinkedHashMap<>();
		LinkedHashMap<String, Double> mulmapserver = new LinkedHashMap<>();
		LinkedHashMap<String, LinkedHashMap<String, Double>> summapworld = new LinkedHashMap<>();
		LinkedHashMap<String, LinkedHashMap<String, Double>> mulmapworld = new LinkedHashMap<>();
		long now = System.currentTimeMillis();
		String server = plugin.getAdministration() != null ? plugin.getAdministration().getSpigotServerName() 
				: plugin.getYamlHandler().getConfig().getString("ServerName");
		for(BonusMalusValue bmv : bmvlist)
		{
			String bms = bmv.getBonusMalusName();
			bmvl.add(bms);
			if(bmv.getDuration() > 0 && bmv.getDuration() < now)
			{
				plugin.getMysqlHandler().deleteData(MysqlHandler.Type.BONUSMALUSVALUE, "`id` = ?", bmv.getID());
			}
			switch(bmv.getValueType())
			{
			case ADDITION:
				if(bmv.getServer() != null && bmv.getServer().equals(server))
				{
					if(bmv.getWorld() != null)
					{
						//world
						LinkedHashMap<String, Double> summ = new LinkedHashMap<>();
						if(summapworld.containsKey(bmv.getWorld()))
						{
							summ = summapworld.get(bmv.getWorld());
						} 
						summ.put(bms, summ.containsKey(bms) ? summ.get(bms) + bmv.getValue() : bmv.getValue());
						summapworld.put(bmv.getWorld(), summ);
						break;
					}
					//server
					summapserver.put(bms, summapserver.containsKey(bms) ? summapserver.get(bms) + bmv.getValue() : bmv.getValue());
					break;
				}
				//global
				summap.put(bms, summap.containsKey(bms) ? summap.get(bms) + bmv.getValue() : bmv.getValue());
				break;
			case MULTIPLICATION:
				if(bmv.getServer() != null && bmv.getServer().equals(server))
				{
					if(bmv.getWorld() != null)
					{
						//world
						LinkedHashMap<String, Double> mulm = new LinkedHashMap<>();
						if(mulmapworld.containsKey(bmv.getWorld()))
						{
							mulm = summapworld.get(bmv.getWorld());
						}
						
						mulm.put(bms, mulm.containsKey(bms) ? mulm.get(bms) + bmv.getValue() : bmv.getValue());
						mulmapworld.put(bmv.getWorld(), mulm);
						break;
					}
					//server
					mulmapserver.put(bms, mulmapserver.containsKey(bms) ? mulmapserver.get(bms) + bmv.getValue() : bmv.getValue());
					break;
				}
				//global
				mulmap.put(bms, mulmap.containsKey(bms) ? mulmap.get(bms) + bmv.getValue() : bmv.getValue());
				break;
			}
		}
		bmPerUUIDSUM.put(uuid, summap);
		bmPerUUIDMUL.put(uuid, mulmap);
		bmPerUUIDPerServerSUM.put(uuid, summapserver);
		bmPerUUIDPerServerMUL.put(uuid, mulmapserver);
		bmPerUUIDPerServerPerWorldSUM.put(uuid, summapworld);
		bmPerUUIDPerServerPerWorldMUL.put(uuid, mulmapworld);
	}
	
	public void quit(UUID uuid)
	{
		bmPerUUIDSUM.remove(uuid);
		bmPerUUIDMUL.remove(uuid);
		bmPerUUIDPerServerSUM.remove(uuid);
		bmPerUUIDPerServerMUL.remove(uuid);
		bmPerUUIDPerServerPerWorldSUM.remove(uuid);
		bmPerUUIDPerServerPerWorldMUL.remove(uuid);
	}
	
	public double getSumValue(UUID uuid, String bonusMalusName, String server, String world)
	{
		double d = 0.0;
		if(server != null)
		{
			if(world != null)
			{
				if(bmPerUUIDPerServerPerWorldSUM.containsKey(uuid) 
						&& bmPerUUIDPerServerPerWorldSUM.get(uuid).containsKey(world))
				{
					d += bmPerUUIDPerServerPerWorldSUM.get(uuid).get(world).containsKey(bonusMalusName)
							? bmPerUUIDPerServerPerWorldSUM.get(uuid).get(world).get(bonusMalusName) : 0;
				}			
			}
			d += bmPerUUIDPerServerSUM.containsKey(uuid) ?
					(bmPerUUIDPerServerSUM.get(uuid).containsKey(bonusMalusName) ? bmPerUUIDPerServerSUM.get(uuid).get(bonusMalusName) : 0)
					: 0;
		}
		d += bmPerUUIDSUM.containsKey(uuid) ?
				(bmPerUUIDSUM.get(uuid).containsKey(bonusMalusName) ? bmPerUUIDSUM.get(uuid).get(bonusMalusName) : 0)
				: 0;
		return d;
	}
	
	public double getMulltiplyValue(UUID uuid, String bonusMalusName, String server, String world)
	{
		double d = 0.0;
		if(server != null)
		{
			if(world != null)
			{
				if(bmPerUUIDPerServerPerWorldMUL.containsKey(uuid) 
						&& bmPerUUIDPerServerPerWorldMUL.get(uuid).containsKey(world)
						&& bmPerUUIDPerServerPerWorldMUL.get(uuid).get(world).containsKey(bonusMalusName))
				{
					d += bmPerUUIDPerServerPerWorldMUL.get(uuid).get(world).get(bonusMalusName);
				}				
			}
			if(bmPerUUIDPerServerMUL.containsKey(uuid)
					&& bmPerUUIDPerServerMUL.get(uuid).containsKey(bonusMalusName))
			{
				d += bmPerUUIDPerServerMUL.get(uuid).get(bonusMalusName);
			}
		}
		if(bmPerUUIDMUL.containsKey(uuid)
				&& bmPerUUIDMUL.get(uuid).containsKey(bonusMalusName))
		{
			d += bmPerUUIDMUL.get(uuid).get(bonusMalusName);
		}
		return d == 0.0 ? 1.0 : d;
	}
	
	public ArrayList<BonusMalus> getRegisteredBM()
	{
		return registeredBM;
	}
	
	public LinkedHashMap<String, Double> getRegisteredValues(UUID uuid, String world, boolean additionOrMultiplication, String levelType)
	{
		switch(levelType)
		{
		default:
		case "global":
			if(additionOrMultiplication)
			{
				return bmPerUUIDSUM.get(uuid);
			} else
			{
				return bmPerUUIDMUL.get(uuid);
			}
		case "server":
			if(additionOrMultiplication)
			{
				return bmPerUUIDPerServerSUM.get(uuid);
			} else
			{
				return bmPerUUIDPerServerMUL.get(uuid);
			}
		case "world":
			if(additionOrMultiplication)
			{
				LinkedHashMap<String, LinkedHashMap<String, Double>> map = bmPerUUIDPerServerPerWorldSUM.get(uuid);
				return map.get(world);
			} else
			{
				LinkedHashMap<String, LinkedHashMap<String, Double>> map = bmPerUUIDPerServerPerWorldMUL.get(uuid);
				return map.get(world);
			}
		}
	}

	public boolean isRegistered(String bonusMalusName)
	{
		for(BonusMalus bm : registeredBM)
		{
			if(bm.getBonusMalusName().equals(bonusMalusName))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean register(String bonusMalusName, String displayBonusMalusName,
			boolean isBooleanBonusMalus,
			BonusMalusType bonusMalustype,
			String...bonusMalusExplanation)
	{
		if(isRegistered(bonusMalusName))
		{
			return false;
		}
		if(bonusMalusName == null || displayBonusMalusName == null
				|| bonusMalustype == null)
		{
			return false;
		}
		BonusMalus bm = new BonusMalus(bonusMalusName, displayBonusMalusName,
				isBooleanBonusMalus, bonusMalustype, bonusMalusExplanation);
		plugin.getMysqlHandler().create(MysqlHandler.Type.BONUSMALUS, bm);
		registeredBM.add(bm);
		return true;
	}
	
	public ArrayList<String> getRegistered()
	{
		ArrayList<String> list = new ArrayList<>();
		registeredBM.stream().forEach(x -> list.add(x.getBonusMalusName()));
		return list;
	}
	
	public ArrayList<String> getRegistered(BonusMalusType type)
	{
		ArrayList<String> list = new ArrayList<>();
		registeredBM.stream().forEach(x -> 
		{
			if(x.getBonusMalusType() == type)
			{
				list.add(x.getBonusMalusName());
			}
		});
		return list;
	}
	
	public String getRegisteredDisplayName(String bonusMalusName)
	{
		String display = null;
		for(BonusMalus bm : registeredBM)
		{
			if(bm.getBonusMalusName().equals(bonusMalusName))
			{
				display = bm.getDisplayBonusMalusName();
				break;
			}
		}
		return display;
	}
	
	public BonusMalusType getRegisteredBonusMalusType(String bonusMalusName)
	{
		BonusMalusType bmt = BonusMalusType.DOWN;
		for(BonusMalus bm : registeredBM)
		{
			if(bm.getBonusMalusName().equals(bonusMalusName))
			{
				bmt = bm.getBonusMalusType();
				break;
			}
		}
		return bmt;
	}
	
	public String[] getRegisteredExplanation(String bonusMalusName)
	{
		String[] ar = null;
		for(BonusMalus bm : registeredBM)
		{
			if(bm.getBonusMalusName().equals(bonusMalusName))
			{
				ar = bm.getExplanation().toArray(new String[bm.getExplanation().size()]);
				break;
			}
		}
		return ar;
	}
	
	public void remove(UUID uuid)
	{
		plugin.getMysqlHandler().deleteData(MysqlHandler.Type.BONUSMALUSVALUE,
				"`player_uuid` = ?", uuid.toString());
		update();
	}
	
	public void remove(UUID uuid, String reason)
	{
		plugin.getMysqlHandler().deleteData(MysqlHandler.Type.BONUSMALUSVALUE,
				"`player_uuid` = ? AND `reason` = ?", uuid.toString(), reason);
		update();
	}
	
	public void remove(UUID uuid, String bonusMalusName, String reason)
	{
		plugin.getMysqlHandler().deleteData(MysqlHandler.Type.BONUSMALUSVALUE,
				"`player_uuid` = ? AND `bonus_malus_name` = ? AND `reason` = ?", uuid.toString(), bonusMalusName, reason);
		update();
	}
	
	public void remove(String reason)
	{
		plugin.getMysqlHandler().deleteData(MysqlHandler.Type.BONUSMALUSVALUE,
				"`reason` = ?", reason);
		update();
	}
	
	public void remove(String bonusMalusName, String reason)
	{
		plugin.getMysqlHandler().deleteData(MysqlHandler.Type.BONUSMALUSVALUE,
				"`bonus_malus_name` = ? AND `reason` = ?", bonusMalusName, reason);
		update();
	}
	
	public boolean hasBonusMalus(UUID uuid, String bonusMalusName)
	{
		return plugin.getMysqlHandler().exist(MysqlHandler.Type.BONUSMALUSVALUE,
				"`player_uuid` = ? AND `bonus_malus_name` = ?", uuid.toString(), bonusMalusName);
	}
	
	public boolean hasBonusMalus(UUID uuid, String bonusMalusName, String server, String world)
	{
		if(server != null && world != null)
		{
			return plugin.getMysqlHandler().exist(MysqlHandler.Type.BONUSMALUSVALUE,
					"`player_uuid` = ? AND `bonus_malus_name` = ? AND `server` = ?"
					, uuid.toString(), bonusMalusName, server, world);
		} else if(server != null && world == null)
		{
			return plugin.getMysqlHandler().exist(MysqlHandler.Type.BONUSMALUSVALUE,
					"`player_uuid` = ? AND `bonus_malus_name` = ? AND `server` = ? AND `world` = ?"
					, uuid.toString(), bonusMalusName, server);
		}
		return hasBonusMalus(uuid, bonusMalusName);
	}
	
	public boolean hasBonusMalus(UUID uuid, String bonusMalusName, String internReason, String server, String world)
	{
		if(server != null && world != null) //TODO
		{
			return plugin.getMysqlHandler().exist(MysqlHandler.Type.BONUSMALUSVALUE,
					"`player_uuid` = ? AND `bonus_malus_name` = ? AND `server` = ?"
					, uuid.toString(), bonusMalusName, server, world);
		} else if(server != null && world == null)
		{
			return plugin.getMysqlHandler().exist(MysqlHandler.Type.BONUSMALUSVALUE,
					"`player_uuid` = ? AND `bonus_malus_name` = ? AND `server` = ? AND `world` = ?"
					, uuid.toString(), bonusMalusName, server);
		}
		return hasBonusMalus(uuid, bonusMalusName);
	}
	
	public double getLastBaseValue(final UUID uuid, final double baseValue, final String bonusMalusName, String server, String world)
	{
		BonusMalusBaseValue bmbv = (BonusMalusBaseValue) plugin.getMysqlHandler().getData(MysqlHandler.Type.BONUSMALUSBASEVALUE,
				"`player_uuid` = ? AND `bonus_malus_name` = ? AND `last_base_value` = ?",
				uuid.toString(), bonusMalusName, baseValue);
		if(bmbv == null)
		{
			return 1.0;
		} else
		{
			return bmbv.getLastBaseValue();
		}
	}
	
	public double getResult(UUID uuid, double baseValue, String bonusMalusName)
	{
		return getResult(uuid, baseValue, bonusMalusName, null, null);
	}
	
	public double getResult(final UUID uuid, final double baseValue, final String bonusMalusName, String server, String world)
	{
		if(!plugin.getMysqlHandler().exist(MysqlHandler.Type.BONUSMALUSBASEVALUE,
				"`player_uuid` = ? AND `bonus_malus_name` = ? AND `last_base_value` = ?",
				uuid.toString(), bonusMalusName, baseValue))
		{
			new BukkitRunnable()
			{
				
				@Override
				public void run()
				{
					BonusMalusBaseValue bmbv = (BonusMalusBaseValue) plugin.getMysqlHandler().getData(MysqlHandler.Type.BONUSMALUSBASEVALUE,
							"`player_uuid` = ? AND `bonus_malus_name` = ? AND `last_base_value` = ?",
							uuid.toString(), bonusMalusName, baseValue);
					if(bmbv == null)
					{
						bmbv = new BonusMalusBaseValue(uuid, bonusMalusName, baseValue);
						plugin.getMysqlHandler().create(MysqlHandler.Type.BONUSMALUSBASEVALUE, bmbv);
					} else
					{
						bmbv.setLastBaseValue(baseValue);
						plugin.getMysqlHandler().updateData(MysqlHandler.Type.BONUSMALUSBASEVALUE, bmbv,
								"`player_uuid` = ? AND `bonus_malus_name` = ?",
								uuid.toString(), bonusMalusName);
					}
				}
			}.runTaskLaterAsynchronously(plugin, 10L);
		}
		if(!isRegistered(bonusMalusName) || !hasBonusMalus(uuid, bonusMalusName))
		{
			return baseValue;
		}
		return (baseValue + getSumValue(uuid, bonusMalusName, server, world)) * getMulltiplyValue(uuid, bonusMalusName, server, world);
	}
	
	public boolean getResult(UUID uuid, boolean permissionOutput, String bonusMalusName)
	{
		return getResult(uuid, permissionOutput, bonusMalusName, null, null);
	}
	
	public boolean getResult(UUID uuid, boolean permissionOutput, String bonusMalusName, String server, String world)
	{
		if(!isRegistered(bonusMalusName) || !hasBonusMalus(uuid, bonusMalusName))
		{
			return permissionOutput;
		}
		double r = ((permissionOutput ? 1.0 : 0.0) + getSumValue(uuid, bonusMalusName, server, world))
					* getMulltiplyValue(uuid, bonusMalusName, server, world);
		return r >= 1.0 ? true : false;
	}
	
	public void addAdditionFactor(UUID uuid, String bonusMalusName,
			double value, String internReason, String displayReason,
			String server, String world,
			Long duration)
	{
		addFactor(uuid, bonusMalusName, value, internReason, displayReason, server, world, duration, BonusMalusValueType.ADDITION);
	}
	
	public void addMultiplicationFactor(UUID uuid, String bonusMalusName,
			double value, String internReason, String displayReason,
			String server, String world,
			Long duration)
	{
		addFactor(uuid, bonusMalusName, value, internReason, displayReason, server, world, duration, BonusMalusValueType.MULTIPLICATION);
	}
	
	private void addFactor(UUID uuid, String bonusMalusName,
			double value, String internReason, String displayReason,
			String server, String world,
			Long duration, BonusMalusValueType bmvt)
	{
		BonusMalusValue bmv = new BonusMalusValue(uuid, bonusMalusName, bmvt,
				value, internReason, server, world,
				duration != null ? (duration.longValue() > 0 ? duration.longValue()+System.currentTimeMillis() : -1) : -1);
		plugin.getMysqlHandler().create(MysqlHandler.Type.BONUSMALUSVALUE, bmv);
		update(uuid);
	}
	
	public void update()
	{
		for(Player player : Bukkit.getOnlinePlayers())
		{
			final UUID uuid = player.getUniqueId();
			update(uuid);
		}
	}
	
	public void update(UUID uuid)
	{
		if(Bukkit.getPlayer(uuid) == null)
		{
			return;
		}
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				quit(uuid);
				join(uuid);
			}
		}.runTaskLaterAsynchronously(plugin, 2L);
	}
}