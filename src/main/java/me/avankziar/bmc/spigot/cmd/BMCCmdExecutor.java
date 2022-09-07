package main.java.me.avankziar.bmc.spigot.cmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import main.java.me.avankziar.bmc.general.ChatApi;
import main.java.me.avankziar.bmc.spigot.BMC;
import main.java.me.avankziar.bmc.spigot.assistance.MatchApi;
import main.java.me.avankziar.bmc.spigot.assistance.TimeHandler;
import main.java.me.avankziar.bmc.spigot.cmdtree.CommandConstructor;
import main.java.me.avankziar.bmc.spigot.database.MysqlHandler;
import main.java.me.avankziar.bmc.spigot.objects.BonusMalus;
import main.java.me.avankziar.bmc.spigot.objects.BonusMalusValue;
import main.java.me.avankziar.bmc.spigot.permission.Bypass;
import main.java.me.avankziar.bmc.spigot.permission.Bypass.Permission;
import main.java.me.avankziar.ifh.general.bonusmalus.BonusMalusValueType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class BMCCmdExecutor implements CommandExecutor
{
	private BMC plugin;
	private static CommandConstructor cc;
	private HashMap<UUID, Long> cooldown = new HashMap<>();
	
	public BMCCmdExecutor(BMC plugin, CommandConstructor cc)
	{
		this.plugin = plugin;
		BMCCmdExecutor.cc = cc;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lable, String[] args) 
	{
		if (!(sender instanceof Player)) 
		{
			BMC.log.info("/%cmd% is only for Player!".replace("%cmd%", cc.getName()));
			return false;
		}
		Player player = (Player) sender;
		if(cc == null)
		{
			return false;
		}
		if(!player.hasPermission(cc.getPermission()))
		{
			player.sendMessage(ChatApi.tl(plugin.getYamlHandler().getLang().getString("NoPermission")));
			return false;
		}
		baseCommands(player, args); //Base and Info Command
		return true;
	}
	
	public void baseCommands(final Player player, String[] args)
	{
		if(cooldown.containsKey(player.getUniqueId()))
		{
			if(cooldown.get(player.getUniqueId()) < System.currentTimeMillis())
			{
				player.sendMessage(ChatApi.tl(plugin.getYamlHandler().getLang().getString("PlayerCmdCooldown")));
				return;
			}
			
		}
		cooldown.put(player.getUniqueId(), System.currentTimeMillis()+1000L*30);
		int page = 0;
		if(args.length >= 1 && MatchApi.isInteger(args[0]))
		{
			page = Integer.parseInt(args[0]);
		}
		String othername = player.getName();
		if(args.length >= 2)
		{
			if(!player.hasPermission(Bypass.get(Permission.OTHERPLAYER)))
			{
				player.sendMessage(ChatApi.tl(plugin.getYamlHandler().getLang().getString("NoPermission")));
				return;
			}
			othername = args[1];
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
		boolean lastpage = rg.size()-1 > i;
		if(map.isEmpty())
		{
			player.sendMessage(ChatApi.tl(plugin.getYamlHandler().getLang().getString("PlayerHasNoBonus")
					.replace("%player%", othername)));
			return;
		}
		ArrayList<ArrayList<BaseComponent>> bc = new ArrayList<>();
		ArrayList<BaseComponent> bc1 = new ArrayList<>();
		bc1.add(ChatApi.tctl(plugin.getYamlHandler().getLang().getString("Cmd.Headline")
				.replace("%player%", othername)
				.replace("%page%", String.valueOf(page))
				.replace("%amount%", String.valueOf(map.size()))));
		bc.add(bc1);
		ArrayList<BaseComponent> bc2 = new ArrayList<>();
		bc2.add(ChatApi.tctl(plugin.getYamlHandler().getLang().getString("Cmd.LineTwo")));
		for(Entry<BonusMalus, Double> bme : map.entrySet())
		{
			BonusMalus bm = bme.getKey();
			Double d = bme.getValue();
			ArrayList<BaseComponent> bc3 = new ArrayList<>();
			bc3.add(ChatApi.tctl(plugin.getYamlHandler().getLang().getString("Cmd.BonusMalusDescriptionOne")
					.replace("%displayname%", bm.getDisplayBonusMalusName())));
			ArrayList<BonusMalusValue> bmv = BonusMalusValue.convert(
					plugin.getMysqlHandler().getFullList(MysqlHandler.Type.BONUSMALUSVALUE, "`id` ASC",
					"`bonusmalusname`", bm.getBonusMalusName()));
			ArrayList<String> vlist = new ArrayList<>();
			for(BonusMalusValue bmvv : bmv)
			{
				StringBuilder sb = new StringBuilder();
				sb.append(bmvv.getReason()+"&r: ");
				if(bmvv.getValueType() == BonusMalusValueType.ADDITION)
				{
					sb.append("&e(+) &r");
				} else
				{
					sb.append("&b(*) &r");
				}
				sb.append(bmvv.getValue());
				if(bmvv.getDuration() > 0)
				{
					long dur = bmvv.getDuration()-System.currentTimeMillis();
					sb.append(" >> " + TimeHandler.getRepeatingTime(dur, "dd-HH:mm"));
				}
				vlist.add(sb.toString());
			}
			String value = "";
			if(bm.isBooleanBonus())
			{
				value = d >= 1.0 
						? plugin.getYamlHandler().getLang().getString("Cmd.True") 
						: plugin.getYamlHandler().getLang().getString("Cmd.False");
			} else
			{
				value = String.valueOf(d);
			}
			bc3.add(ChatApi.hoverEvent(plugin.getYamlHandler().getLang().getString("Cmd.BonusMalusDescriptionTwo")
					.replace("%value%", value),
					HoverEvent.Action.SHOW_TEXT, String.join("~!~", vlist)));
			bc3.add(ChatApi.hoverEvent(plugin.getYamlHandler().getLang().getString("Cmd.BonusMalusDescriptionThree"),
					HoverEvent.Action.SHOW_TEXT, String.join("~!~", bm.getExplanation())));
			bc.add(bc3);
		}
		for(ArrayList<BaseComponent> b : bc)
		{
			TextComponent tc = ChatApi.tc("");
			tc.setExtra(b);
			player.spigot().sendMessage(tc);
		}
		pastNextPage(player, page, lastpage, String.valueOf(page), othername, type);
	}
	
	public void pastNextPage(Player player,
			int page, boolean lastpage, String cmdstring, String...objects)
	{
		if(page==0 && lastpage)
		{
			return;
		}
		int i = page+1;
		int j = page-1;
		TextComponent MSG = ChatApi.tctl("");
		List<BaseComponent> pages = new ArrayList<BaseComponent>();
		if(page!=0)
		{
			TextComponent msg2 = ChatApi.tctl(
					plugin.getYamlHandler().getLang().getString("Past"));
			String cmd = cmdstring+" "+String.valueOf(j);
			for(String o : objects)
			{
				cmd += " "+o;
			}
			msg2.setClickEvent( new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
			pages.add(msg2);
		}
		if(!lastpage)
		{
			TextComponent msg1 = ChatApi.tctl(
					plugin.getYamlHandler().getLang().getString("Next"));
			String cmd = cmdstring+" "+String.valueOf(i);
			for(String o : objects)
			{
				cmd += " "+o;
			}
			msg1.setClickEvent( new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
			if(pages.size()==1)
			{
				pages.add(ChatApi.tc(" | "));
			}
			pages.add(msg1);
		}
		MSG.setExtra(pages);	
		player.spigot().sendMessage(MSG);
	}

}