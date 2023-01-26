package main.java.me.avankziar.bmc.spigot.cmd.bmc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import main.java.me.avankziar.bmc.general.ChatApi;
import main.java.me.avankziar.bmc.spigot.BMC;
import main.java.me.avankziar.bmc.spigot.assistance.MatchApi;
import main.java.me.avankziar.bmc.spigot.assistance.TimeHandler;
import main.java.me.avankziar.bmc.spigot.cmd.BMCCmdExecutor;
import main.java.me.avankziar.bmc.spigot.cmdtree.ArgumentConstructor;
import main.java.me.avankziar.bmc.spigot.cmdtree.ArgumentModule;
import main.java.me.avankziar.bmc.spigot.database.MysqlHandler;
import main.java.me.avankziar.bmc.spigot.objects.BonusMalus;
import main.java.me.avankziar.bmc.spigot.objects.BonusMalusValue;
import main.java.me.avankziar.bmc.spigot.permission.Bypass;
import main.java.me.avankziar.bmc.spigot.permission.Bypass.Permission;
import main.java.me.avankziar.ifh.general.bonusmalus.BonusMalusValueType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ARGBoni extends ArgumentModule
{
	private BMC plugin;
	private HashMap<String, Long> cooldown = new HashMap<>();
	private ArgumentConstructor ac = null;
	
	public ARGBoni(ArgumentConstructor argumentConstructor)
	{
		super(argumentConstructor);
		this.plugin = BMC.getPlugin();
		this.ac = argumentConstructor;
	}

	@Override
	public void run(CommandSender sender, String[] args) throws IOException
	{
		Player player = (Player) sender;
		if(cooldown.containsKey(player.getName()))
		{
			if(cooldown.get(player.getName()) > System.currentTimeMillis())
			{
				player.sendMessage(ChatApi.tl(plugin.getYamlHandler().getLang().getString("PlayerCmdCooldown")));
				return;
			}
		}
		cooldown.put(player.getName(), System.currentTimeMillis()+1000L*30);
		int page = 0;
		if(args.length >= 2 && MatchApi.isInteger(args[1]))
		{
			page = Integer.parseInt(args[1]);
		}
		String othername = player.getName();
		if(args.length >= 3)
		{
			if(!player.hasPermission(Bypass.get(Permission.OTHERPLAYER)))
			{
				player.sendMessage(ChatApi.tl(plugin.getYamlHandler().getLang().getString("NoPermission")));
				return;
			}
			othername = args[2];
		}
		Player other = Bukkit.getPlayer(othername);
		if(other == null)
		{
			player.sendMessage(ChatApi.tl(plugin.getYamlHandler().getLang().getString("PlayerNotExist")));
			return;
		}
		final UUID uuid = other.getUniqueId();
		String type = "global";
		String server = null;
		String world = null;
		if(args.length >= 3)
		{
			type = args[2];
			switch(type)
			{
			default:
			case "global":
				break;
			case "server":
				server = plugin.getAdministration() != null ? plugin.getAdministration().getSpigotServerName() 
						: plugin.getYamlHandler().getConfig().getString("ServerName");
				break;
			case "world":
				server = plugin.getAdministration() != null ? plugin.getAdministration().getSpigotServerName() 
						: plugin.getYamlHandler().getConfig().getString("ServerName");
				world = other.getWorld().getName();
				break;
			}
		}
		ArrayList<BonusMalus> rg = plugin.getBonusMalusProvider().getRegisteredBM();
		LinkedHashMap<BonusMalus, Double> map = new LinkedHashMap<>();
		int i = page * 15;
		int j = 0;
		int end = i + 15;
		for(BonusMalus bm : rg)
		{
			if(i != j)
			{
				j++;
				continue;
			}
			if(!plugin.getBonusMalusProvider().hasBonusMalus(uuid, bm.getBonusMalusName(), server, world))
			{
				continue;
			}
			map.put(bm, plugin.getBonusMalusProvider().getResult(uuid, 1.0, bm.getBonusMalusName(),
					server, world));
			i++;
			j++;
			if(i >= end)
			{
				break;
			}
		}
		boolean lastpage = rg.size()-10 < i;
		if(map.isEmpty())
		{
			player.sendMessage(ChatApi.tl(plugin.getYamlHandler().getLang().getString("PlayerHasNoBonus")
					.replace("%player%", othername)));
			return;
		}
		ArrayList<ArrayList<BaseComponent>> bc = new ArrayList<>();
		ArrayList<BaseComponent> bc1 = new ArrayList<>();
		bc1.add(ChatApi.tctl(plugin.getYamlHandler().getLang().getString("CmdBoni.Headline")
				.replace("%player%", othername)
				.replace("%page%", String.valueOf(page))));
		bc.add(bc1);
		ArrayList<BaseComponent> bc2 = new ArrayList<>();
		bc2.add(ChatApi.tctl(plugin.getYamlHandler().getLang().getString("CmdBoni.LineTwo")));
		bc.add(bc2);
		for(Entry<BonusMalus, Double> bme : map.entrySet())
		{
			BonusMalus bm = bme.getKey();
			Double d = bme.getValue();
			ArrayList<BaseComponent> bc3 = new ArrayList<>();
			bc3.add(ChatApi.tctl(plugin.getYamlHandler().getLang().getString("CmdBoni.BonusMalusDescriptionOne")
					.replace("%displayname%", bm.getDisplayBonusMalusName())));
			ArrayList<BonusMalusValue> bmv = BonusMalusValue.convert(
					plugin.getMysqlHandler().getFullList(MysqlHandler.Type.BONUSMALUSVALUE, "`id` ASC",
					"`bonus_malus_name` = ?", bm.getBonusMalusName()));
			ArrayList<String> vlist = new ArrayList<>();
			vlist.add(plugin.getYamlHandler().getLang().getString("CmdBoni.BaseValue")
					.replace("%value%", String.valueOf(1.0)));
			for(BonusMalusValue bmvv : bmv)
			{
				StringBuilder sb = new StringBuilder();
				if(bmvv.getValueType() == BonusMalusValueType.ADDITION)
				{
					sb.append("&#AAAAAA(+) &r");
				} else
				{
					sb.append("&#6D6D6D(x) &r");
				}
				sb.append("'"+bmvv.getValue()+"'");
				sb.append(" >> '"+bmvv.getReason()+"'");
				if(bmvv.getDuration() > 0)
				{
					long dur = bmvv.getDuration()-System.currentTimeMillis();
					sb.append("&r >> " + TimeHandler.getRepeatingTime(dur, "dd-HH:mm"));
				}
				vlist.add(sb.toString());
			}
			String value = "";
			if(bm.isBooleanBonus())
			{
				value = d >= 1.0 
						? plugin.getYamlHandler().getLang().getString("CmdBoni.True") 
						: plugin.getYamlHandler().getLang().getString("CmdBoni.False");
			} else
			{
				value = String.valueOf(d);
			}
			bc3.add(ChatApi.hoverEvent(plugin.getYamlHandler().getLang().getString("CmdBoni.BonusMalusDescriptionTwo")
					.replace("%value%", value),
					HoverEvent.Action.SHOW_TEXT, String.join("~!~", vlist)));
			bc3.add(ChatApi.hoverEvent(plugin.getYamlHandler().getLang().getString("CmdBoni.BonusMalusDescriptionThree"),
					HoverEvent.Action.SHOW_TEXT, String.join("~!~", bm.getExplanation())));
			bc.add(bc3);
		}
		for(ArrayList<BaseComponent> b : bc)
		{
			TextComponent tc = ChatApi.tc("");
			tc.setExtra(b);
			player.spigot().sendMessage(tc);
		}
		BMCCmdExecutor.pastNextPage(player, page, lastpage, ac.getCommandString(), othername, type);
	}
}