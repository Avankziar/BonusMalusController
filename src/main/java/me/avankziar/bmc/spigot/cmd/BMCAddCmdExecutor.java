package main.java.me.avankziar.bmc.spigot.cmd;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import main.java.me.avankziar.bmc.general.ChatApi;
import main.java.me.avankziar.bmc.spigot.BMC;
import main.java.me.avankziar.bmc.spigot.assistance.MatchApi;
import main.java.me.avankziar.bmc.spigot.assistance.TimeHandler;
import main.java.me.avankziar.bmc.spigot.cmdtree.CommandConstructor;
import main.java.me.avankziar.bmc.spigot.permission.BonusMalusPermission;
import main.java.me.avankziar.ifh.general.bonusmalus.BonusMalusValueType;
import net.md_5.bungee.api.chat.ClickEvent;

public class BMCAddCmdExecutor implements CommandExecutor
{
	private BMC plugin;
	private static CommandConstructor cc;
	
	public BMCAddCmdExecutor(BMC plugin, CommandConstructor cc)
	{
		this.plugin = plugin;
		BMCAddCmdExecutor.cc = cc;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lable, String[] args) 
	{
		if(cc == null)
		{
			return false;
		}
		if (sender instanceof Player) 
		{
			Player player = (Player) sender;
			if(!BonusMalusPermission.hasPermission(player, cc))
			{
				player.sendMessage(ChatApi.tl(plugin.getYamlHandler().getLang().getString("NoPermission")));
				return false;
			}
		}
		if(args.length < 6)
		{
			sender.spigot().sendMessage(ChatApi.clickEvent(plugin.getYamlHandler().getLang().getString("InputIsWrong"),
					ClickEvent.Action.RUN_COMMAND, BMC.infoCommand));
			return false;
		}
		String bonusmalus = args[0];
		String othername = args[1];
		String type = args[2];
		String value = args[3];
		double d = 0.0;
		String bmvtValue = args[4];
		String dur = args[5];
		long duration = -1;
		String reason = "";
		BonusMalusValueType bmvt = BonusMalusValueType.ADDITION;
		if(!plugin.getBonusMalusProvider().isRegistered(bonusmalus))
		{
			sender.sendMessage(ChatApi.tl(plugin.getYamlHandler().getLang().getString("CmdAdd.IsNotRegistered")));
			return false;
		}
		OfflinePlayer other = Bukkit.getPlayer(othername);
		if(other == null || !other.hasPlayedBefore())
		{
			sender.sendMessage(ChatApi.tl(plugin.getYamlHandler().getLang().getString("PlayerNotExist")));
			return false;
		}
		final UUID uuid = other.getUniqueId();
		String server = null;
		String world = null;
		if(type.startsWith("server"))
		{
			String[] sp = type.split(":");
			if(sp.length != 2)
			{
				sender.spigot().sendMessage(ChatApi.clickEvent(plugin.getYamlHandler().getLang().getString("InputIsWrong"),
						ClickEvent.Action.RUN_COMMAND, BMC.infoCommand));
				return false;
			}
			server = sp[1];
		} else if(type.startsWith("server"))
		{
			String[] sp = type.split(":");
			if(sp.length != 3)
			{
				sender.spigot().sendMessage(ChatApi.clickEvent(plugin.getYamlHandler().getLang().getString("InputIsWrong"),
						ClickEvent.Action.RUN_COMMAND, BMC.infoCommand));
				return false;
			}
			server = sp[1];
			world = sp[2];
		} else if(!type.startsWith("global"))
		{
			type = "global";
		}
		if(!MatchApi.isDouble(value))
		{
			sender.sendMessage(ChatApi.tl(plugin.getYamlHandler().getLang().getString("NoDouble")
					.replace("%value%", value)));
			return false;
		}
		d = Double.parseDouble(value);
		try
		{
			bmvt = BonusMalusValueType.valueOf(bmvtValue);
		} catch(Exception e)
		{
			sender.spigot().sendMessage(ChatApi.clickEvent(plugin.getYamlHandler().getLang().getString("InputIsWrong"),
					ClickEvent.Action.RUN_COMMAND, BMC.infoCommand));
			return false;
		}
		if(!MatchApi.isLong(dur))
		{
			sender.sendMessage(ChatApi.tl(plugin.getYamlHandler().getLang().getString("NoNumber")
					.replace("%value%", dur)));
			return false;
		}
		duration = Long.parseLong(dur);
		if(duration == 0)
		{
			duration = -1;
		}
		for (int i = 6; i < args.length; i++) 
        {
			reason += args[i];
			if(i < (args.length-1))
			{
				reason += " ";
			}
        }
		if(reason.isBlank())
		{
			reason = "/";
		}
		if(bmvt == BonusMalusValueType.ADDITION)
		{
			plugin.getBonusMalusProvider().addAdditionFactor(uuid, bonusmalus, d, reason, server, world, duration);
		} else
		{
			plugin.getBonusMalusProvider().addMultiplicationFactor(uuid, bonusmalus, d, reason, server, world, duration);
		}
		if(duration < 0)
		{
			sender.sendMessage(ChatApi.tl(plugin.getYamlHandler().getLang().getString("CmdAdd.AddedPermanent")
					.replace("%bm%", bonusmalus)
					.replace("%player%", othername)
					.replace("%type%", type)
					.replace("%formula%", bmvt.toString())
					.replace("%value%", value)
					.replace("%reason%", reason)
					));
		} else
		{
			sender.sendMessage(ChatApi.tl(plugin.getYamlHandler().getLang().getString("CmdAdd.AddedTemporary")
					.replace("%bm%", bonusmalus)
					.replace("%player%", othername)
					.replace("%type%", type)
					.replace("%formula%", bmvt.toString())
					.replace("%value%", value)
					.replace("%duration%", TimeHandler.getRepeatingTime(duration, "dd-HH:mm"))
					.replace("%reason%", reason)
					));
		}
		return true;
	}
}