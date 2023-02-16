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
import main.java.me.avankziar.bmc.spigot.conditionbonusmalus.Bypass.Permission;
import main.java.me.avankziar.bmc.spigot.conditionbonusmalus.ConditionBonusMalus;
import main.java.me.avankziar.bmc.spigot.database.MysqlHandler;
import main.java.me.avankziar.bmc.spigot.objects.BonusMalus;
import main.java.me.avankziar.bmc.spigot.objects.BonusMalusValue;
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
		cooldown.put(player.getName(), System.currentTimeMillis()+1000L*10);
		int page = 0;
		if(args.length >= 2 && MatchApi.isInteger(args[1]))
		{
			page = Integer.parseInt(args[1]);
		}
		String othername = player.getName();
		if(args.length >= 3)
		{
			if(!ConditionBonusMalus.hasPermission(player, Permission.OTHERPLAYER))
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
		ArrayList<BonusMalus> rg = plugin.getBonusMalus().getRegisteredBM();
		LinkedHashMap<BonusMalus, Double> map = new LinkedHashMap<>();
		int end = page * 10 + 9;
		for(int i = page * 10; i < rg.size(); i++)
		{
			BonusMalus bm = rg.get(i);
			if(!plugin.getBonusMalus().hasBonusMalus(uuid, bm.getBonusMalusName(), server, world))
			{
				continue;
			}
			map.put(bm, plugin.getBonusMalus().getResult(uuid, 1.0, bm.getBonusMalusName(),
					server, world));
			if(i >= end)
			{
				break;
			}
		}
		boolean lastpage = rg.size()-9 < page * 10;
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
		ArrayList<BaseComponent> bc4 = new ArrayList<>();
		bc4.add(ChatApi.tctl(plugin.getYamlHandler().getLang().getString("CmdBoni.LineThree")));
		bc.add(bc4);
		ArrayList<BaseComponent> bc5 = new ArrayList<>();
		bc5.add(ChatApi.tctl(plugin.getYamlHandler().getLang().getString("CmdBoni.LineFour")));
		bc.add(bc5);
		for(Entry<BonusMalus, Double> bme : map.entrySet())
		{
			BonusMalus bm = bme.getKey();
			final double d = plugin.getBonusMalus().getLastBaseValue(uuid, bme.getValue().doubleValue(),
					bm.getBonusMalusName(), server, world);
			final double sum = plugin.getBonusMalus().getSumValue(uuid, bm.getBonusMalusName(), server, world);
			final double mul = plugin.getBonusMalus().getMulltiplyValue(uuid, bm.getBonusMalusName(), server, world);
			final double dd = (d + sum) * mul; 
			ArrayList<BaseComponent> bc3 = new ArrayList<>();
			bc3.add(ChatApi.hoverEvent(plugin.getYamlHandler().getLang().getString("CmdBoni.BonusMalusDescriptionOne")
					.replace("%displayname%", bm.getDisplayBonusMalusName()),
					HoverEvent.Action.SHOW_TEXT, String.join("~!~", bm.getExplanation())));
			ArrayList<BonusMalusValue> bmv = BonusMalusValue.convert(
					plugin.getMysqlHandler().getFullList(MysqlHandler.Type.BONUSMALUSVALUE, "`id` ASC",
					"`player_uuid` = ? AND `bonus_malus_name` = ?", uuid.toString(), bm.getBonusMalusName()));
			ArrayList<String> vlist = new ArrayList<>();
			vlist.add(plugin.getYamlHandler().getLang().getString("CmdBoni.BaseValue")
					.replace("%value%", String.valueOf(d)));
			for(BonusMalusValue bmvv : bmv)
			{
				StringBuilder sb = new StringBuilder();
				if(bmvv.getValueType() == BonusMalusValueType.ADDITION)
				{
					if(bmvv.getValue() >= 0)
					{
						sb.append("&#60ec4b(+) &r");
					} else
					{
						sb.append("&#eb2424(+) &r");
					}
				} else
				{
					if(bmvv.getValue() >= 1.0)
					{
						sb.append("&#60ec4b(x) &r");
					} else
					{
						sb.append("&#eb2424(x) &r");
					}
				}
				sb.append("'"+bmvv.getValue()+"'");
				sb.append(" >> '"+bmvv.getDisplayReason()+"'");
				if(bmvv.getDuration() > 0)
				{
					long dur = bmvv.getDuration()-System.currentTimeMillis();
					sb.append("&r >> " + TimeHandler.getRepeatingTime(dur, "dd-HH:mm"));
				}
				vlist.add(sb.toString());
			}
			vlist.add(plugin.getYamlHandler().getLang().getString("CmdBoni.EndValue")
					.replace("%start%", String.valueOf(d))
					.replace("%value%", String.valueOf(dd))
					.replace("%sum%", String.valueOf(sum))
					.replace("%mul%", String.valueOf(mul)));
			String value = String.valueOf(dd);
			bc3.add(ChatApi.hoverEvent(plugin.getYamlHandler().getLang().getString("CmdBoni.BonusMalusDescriptionTwo")
					.replace("%value%", value),
					HoverEvent.Action.SHOW_TEXT, String.join("~!~", vlist)));
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