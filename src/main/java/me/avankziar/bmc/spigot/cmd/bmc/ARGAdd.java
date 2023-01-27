package main.java.me.avankziar.bmc.spigot.cmd.bmc;

import java.io.IOException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import main.java.me.avankziar.bmc.general.ChatApi;
import main.java.me.avankziar.bmc.spigot.BMC;
import main.java.me.avankziar.bmc.spigot.assistance.MatchApi;
import main.java.me.avankziar.bmc.spigot.assistance.TimeHandler;
import main.java.me.avankziar.bmc.spigot.cmdtree.ArgumentConstructor;
import main.java.me.avankziar.bmc.spigot.cmdtree.ArgumentModule;
import main.java.me.avankziar.ifh.general.bonusmalus.BonusMalusValueType;
import net.md_5.bungee.api.chat.ClickEvent;

public class ARGAdd extends ArgumentModule
{
	private BMC plugin;
	
	public ARGAdd(ArgumentConstructor argumentConstructor)
	{
		super(argumentConstructor);
		this.plugin = BMC.getPlugin();
	}

	@Override
	public void run(CommandSender sender, String[] args) throws IOException
	{
		String bonusmalus = args[1];
		String othername = args[2];
		String type = args[3];
		String value = args[4];
		double d = 0.0;
		String bmvtValue = args[5];
		String dur = args[6];
		String internReason = args[7];
		long duration = -1;
		String reason = "";
		BonusMalusValueType bmvt = BonusMalusValueType.ADDITION;
		if(!plugin.getBonusMalusProvider().isRegistered(bonusmalus))
		{
			sender.sendMessage(ChatApi.tl(plugin.getYamlHandler().getLang().getString("CmdAdd.IsNotRegistered")));
			return;
		}
		OfflinePlayer other = Bukkit.getPlayer(othername);
		if(other == null || !other.hasPlayedBefore())
		{
			sender.sendMessage(ChatApi.tl(plugin.getYamlHandler().getLang().getString("PlayerNotExist")));
			return;
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
				return;
			}
			server = sp[1];
		} else if(type.startsWith("world"))
		{
			String[] sp = type.split(":");
			if(sp.length != 3)
			{
				sender.spigot().sendMessage(ChatApi.clickEvent(plugin.getYamlHandler().getLang().getString("InputIsWrong"),
						ClickEvent.Action.RUN_COMMAND, BMC.infoCommand));
				return;
			}
			server = sp[1];
			world = sp[2];
		} else
		{
			type = "global";
		}
		if(!MatchApi.isDouble(value))
		{
			sender.sendMessage(ChatApi.tl(plugin.getYamlHandler().getLang().getString("NoDouble")
					.replace("%value%", value)));
			return;
		}
		d = Double.parseDouble(value);
		try
		{
			bmvt = BonusMalusValueType.valueOf(bmvtValue);
		} catch(Exception e)
		{
			sender.spigot().sendMessage(ChatApi.clickEvent(plugin.getYamlHandler().getLang().getString("InputIsWrong"),
					ClickEvent.Action.RUN_COMMAND, BMC.infoCommand));
			return;
		}
		if(MatchApi.isLong(dur))
		{
			duration = Long.parseLong(dur);
		} else
		{
			duration = TimeHandler.getRepeatingTimeShort(dur);
		}
		if(duration == 0)
		{
			duration = -1;
		}
		for (int i = 8; i < args.length; i++) 
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
			plugin.getBonusMalusProvider().addAdditionFactor(uuid, bonusmalus, d, internReason, reason, server, world, duration);
		} else
		{
			plugin.getBonusMalusProvider().addMultiplicationFactor(uuid, bonusmalus, d, reason, internReason, server, world, duration);
		}
		if(duration < 0)
		{
			sender.sendMessage(ChatApi.tl(plugin.getYamlHandler().getLang().getString("CmdAdd.AddedPermanent")
					.replace("%bm%", bonusmalus)
					.replace("%player%", othername)
					.replace("%type%", type)
					.replace("%formula%", bmvt.toString())
					.replace("%value%", value)
					.replace("%internreason%", internReason)
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
					.replace("%internreason%", internReason)
					.replace("%reason%", reason)
					));
		}
		return;
	}
}
