package main.java.me.avankziar.bmc.spigot.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

import main.java.me.avankziar.bmc.spigot.database.Language.ISO639_2B;
import main.java.me.avankziar.bmc.spigot.permission.Bypass;

public class YamlManager
{
	private ISO639_2B languageType = ISO639_2B.GER;
	//The default language of your plugin. Mine is german.
	private ISO639_2B defaultLanguageType = ISO639_2B.GER;
	
	//Per Flatfile a linkedhashmap.
	private static LinkedHashMap<String, Language> configSpigotKeys = new LinkedHashMap<>();
	private static LinkedHashMap<String, Language> commandsKeys = new LinkedHashMap<>();
	private static LinkedHashMap<String, Language> languageKeys = new LinkedHashMap<>();
	/*
	 * Here are mutiplefiles in one "double" map. The first String key is the filename
	 * So all filename muss be predefine. For example in the config.
	 */
	private static LinkedHashMap<String, LinkedHashMap<String, Language>> guisKeys = new LinkedHashMap<>();
	
	public YamlManager()
	{
		initConfig();
		initCommands();
		initLanguage();
	}
	
	public ISO639_2B getLanguageType()
	{
		return languageType;
	}

	public void setLanguageType(ISO639_2B languageType)
	{
		this.languageType = languageType;
	}
	
	public ISO639_2B getDefaultLanguageType()
	{
		return defaultLanguageType;
	}
	
	public LinkedHashMap<String, Language> getConfigSpigotKey()
	{
		return configSpigotKeys;
	}
	
	public LinkedHashMap<String, Language> getCommandsKey()
	{
		return commandsKeys;
	}
	
	public LinkedHashMap<String, Language> getLanguageKey()
	{
		return languageKeys;
	}
	
	public LinkedHashMap<String, LinkedHashMap<String, Language>> getGUIKey()
	{
		return guisKeys;
	}
	
	/*
	 * The main methode to set all paths in the yamls.
	 */
	public void setFileInput(YamlConfiguration yml, LinkedHashMap<String, Language> keyMap, String key, ISO639_2B languageType)
	{
		if(!keyMap.containsKey(key))
		{
			return;
		}
		if(yml.get(key) != null)
		{
			return;
		}
		if(keyMap.get(key).languageValues.get(languageType).length == 1)
		{
			if(keyMap.get(key).languageValues.get(languageType)[0] instanceof String)
			{
				yml.set(key, ((String) keyMap.get(key).languageValues.get(languageType)[0]).replace("\r\n", ""));
			} else
			{
				yml.set(key, keyMap.get(key).languageValues.get(languageType)[0]);
			}
		} else
		{
			List<Object> list = Arrays.asList(keyMap.get(key).languageValues.get(languageType));
			ArrayList<String> stringList = new ArrayList<>();
			if(list instanceof List<?>)
			{
				for(Object o : list)
				{
					if(o instanceof String)
					{
						stringList.add(((String) o).replace("\r\n", ""));
					} else
					{
						stringList.add(o.toString().replace("\r\n", ""));
					}
				}
			}
			yml.set(key, (List<String>) stringList);
		}
	}
	
	public void initConfig() //INFO:Config
	{
		configSpigotKeys.put("useIFHAdministration"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				true}));
		configSpigotKeys.put("IFHAdministrationPath"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				"bmc"}));
		configSpigotKeys.put("ServerName"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				"hub"}));
		configSpigotKeys.put("Mysql.Status"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				false}));
		configSpigotKeys.put("Mysql.Host"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				"127.0.0.1"}));
		configSpigotKeys.put("Mysql.Port"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				3306}));
		configSpigotKeys.put("Mysql.DatabaseName"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				"mydatabase"}));
		configSpigotKeys.put("Mysql.SSLEnabled"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				false}));
		configSpigotKeys.put("Mysql.AutoReconnect"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				true}));
		configSpigotKeys.put("Mysql.VerifyServerCertificate"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				false}));
		configSpigotKeys.put("Mysql.User"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				"admin"}));
		configSpigotKeys.put("Mysql.Password"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				"not_0123456789"}));
		configSpigotKeys.put("DeleteOldDataTask.RunInSeconds"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				60}));
	}
	
	@SuppressWarnings("unused") //INFO:Commands
	public void initCommands()
	{
		comBypass();
		String path = "";
		commandsInput("bmc", "bmc", "bmc.cmd.bmc", 
				"/bmc [page] [playername] [global/server/world]", "/bmc ", false,
				"&c/bmc [Seite] [Spielername] [global/server/world] &f| Infoseite für alle Befehle.",
				"&c/bmc [page] [playername] [global/server/world] &f| Info page for all commands.",
				"&bBefehlsrecht für &f/bmc",
				"&bCommandright for &f/bmc",
				"&eInfoseite für alle Befehle.",
				"&eInfo page for all commands.");
		commandsInput("bmcboni", "bmcboni", "bmc.cmd.bmcboni", 
				"/bmcboni [page] [playername] [global/server/world]", "/bmcboni ", false,
				"&c/bmcboni [Seite] [Spielername] [global/server/world] &f| Listet alle aktiven Boni/Mali des Spielers mit Hovererklärung auf.",
				"&c/bmcboni [page] [playername] [global/server/world] &f| Lists all active bonuses/maluses of the player with hoverexplanation.",
				"&bBefehlsrecht für &f/bmcboni",
				"&bCommandright for &f/bmcboni",
				"&eListet alle aktiven Boni/Mali des Spielers mit Hovererklärung auf.",
				"&eLists all active bonuses/maluses of the player with hoverexplanation.");
		commandsInput("bmcadd", "bmcadd", "bmc.cmd.bmcadd", 
				"/bmcadd <bonusmalus> <player> <global/server:servername/world:servername:worldname> <value> <ADDITION/MULTIPLICATION> <0/dd-HH:mm> <reason...>", "/bmcadd ", false,
				"&c/bmcadd <BonusMalus> <Spieler> <global/server:servername/world:servername:weltname> <value> <ADDITION/MULTIPLICATION> <0/dd-HH:mm> <Grund...> &f| Fügt dem angegeben Spieler einen Bonus/Malus hinzu.",
				"&c/bmcadd <bonusmalus> <player> <global/server:servername/world:servername:worldname> <value> <ADDITION/MULTIPLICATION> <0/dd-HH:mm> <reason...> &f| Adds a bonus/penalty to the specified player.",
				"&bBefehlsrecht für &f/bmcadd",
				"&bCommandright for &f/bmcadd",
				"&eFügt dem angegeben Spieler einen Bonus/Malus hinzu.",
				"&eAdds a bonus/penalty to the specified player.");
	}
	
	private void comBypass() //INFO:ComBypass
	{
		List<Bypass.Permission> list = new ArrayList<Bypass.Permission>(EnumSet.allOf(Bypass.Permission.class));
		for(Bypass.Permission ept : list)
		{
			commandsKeys.put("Bypass."+ept.toString().replace("_", ".")
					, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					"bmc."+ept.toString().toLowerCase().replace("_", ".")}));
		}
	}
	
	private void commandsInput(String path, String name, String basePermission, 
			String suggestion, String commandString, boolean putUpCmdPermToBonusMalusSystem,
			String helpInfoGerman, String helpInfoEnglish,
			String dnGerman, String dnEnglish,
			String exGerman, String exEnglish)
	{
		commandsKeys.put(path+".Name"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				name}));
		commandsKeys.put(path+".Permission"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				basePermission}));
		commandsKeys.put(path+".Suggestion"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				suggestion}));
		commandsKeys.put(path+".PutUpCommandPermToBonusMalusSystem"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				putUpCmdPermToBonusMalusSystem}));
		commandsKeys.put(path+".CommandString"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				commandString}));
		commandsKeys.put(path+".HelpInfo"
				, new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
				helpInfoGerman,
				helpInfoEnglish}));
		commandsKeys.put(path+".Displayname"
				, new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
				dnGerman,
				dnEnglish}));
		commandsKeys.put(path+".Explanation"
				, new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
				exGerman,
				exEnglish}));
	}
	
	public void initLanguage() //INFO:Languages
	{
		languageKeys.put("InputIsWrong",
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"&cDeine Eingabe ist fehlerhaft! Klicke hier auf den Text, um weitere Infos zu bekommen!",
						"&cYour input is incorrect! Click here on the text to get more information!"}));
		languageKeys.put("NoPermission",
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"&cDu hast dafür keine Rechte!",
						"&cYou dont not have the rights!"}));
		languageKeys.put("NoPlayerExist",
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"&cDer Spieler existiert nicht!",
						"&cThe player does not exist!"}));
		languageKeys.put("NoNumber",
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"&cDas Argument &f%value% &cmuss eine ganze Zahl sein.",
						"&cThe argument &f%value% &must be an integer."}));
		languageKeys.put("NoDouble",
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"&cDas Argument &f%value% &cmuss eine Gleitpunktzahl sein!",
						"&cThe argument &f%value% &must be a floating point number!"}));
		languageKeys.put("IsNegativ",
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"&cDas Argument &f%value% &cmuss eine positive Zahl sein!",
						"&cThe argument &f%value% &must be a positive number!"}));
		languageKeys.put("GeneralHover",
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"&eKlick mich!",
						"&eClick me!"}));
		languageKeys.put("Headline", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"&e=====&7[&6BungeeTeleportManager&7]&e=====",
						"&e=====&7[&6BungeeTeleportManager&7]&e====="}));
		languageKeys.put("Next", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"&e&nnächste Seite &e==>",
						"&e&nnext page &e==>"}));
		languageKeys.put("Past", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"&e<== &nvorherige Seite",
						"&e<== &nprevious page"}));
		languageKeys.put("PlayerCmdCooldown", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"&cDu bist noch im Befehls-Cooldown! Bitte warte etwas!",
						"&cYou are still in the command cooldown! Please wait a little!"}));
		languageKeys.put("PlayerHasNoBonus", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"&cDer Spieler &f%player% &chat keine Boni oder Mali!",
						"&cThe player &f%player% &chas no bonuses or maluses!"}));
		languageKeys.put("Cmd.Headline", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"&e===&fBoni/Mali &6von &c%player%&f, Seite %page%, GesamtAnzahl: %amount%&e===",
						"&e===&fBoni/Mali &6from &c%player%&f, page %page%, totalamount: %amount%&e==="}));
		languageKeys.put("Cmd.LineTwo", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"&fAlle Werte wurde mit einem BasisWert von 1.0 berechnet!",
						"&fAll values were calculated with a base value of 1.0!"}));
		languageKeys.put("Cmd.BonusMalusDescriptionOne", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"%displayname%&r: ",
						"%displayname%&r: "}));
		languageKeys.put("Cmd.BonusMalusDescriptionTwo", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"%value% ",
						"%value% "}));
		languageKeys.put("Cmd.BonusMalusDescriptionThree", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"&6Erklärungshover",
						"&6Erklärungshover"}));
		languageKeys.put("Cmd.True", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"&a✔",
						"&a✔"}));
		languageKeys.put("Cmd.False", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"&c✖",
						"&c✖"}));
		languageKeys.put("Cmd.BaseValue", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"&#fc9303BaseWert: &r%value%",
						"&#fc9303Basevalue: &r%value%"}));
		languageKeys.put("CmdAdd.IsNotRegistered", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"&cDer Bonus/Malus ist nicht registriert!",
						"&cThe bonus/malus is not registered!"}));
		languageKeys.put("CmdAdd.AddedPermanent", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"&eDer Spieler &f%player% &ehat permanent den Bonus/Malus &f%bm% &emit dem Wert &f%value% &eund den folgenden Werten erhalten: &f%type% | %formula% | %reason%",
						"&eThe player &f%player% &ehas permanently received the bonus/penalty &f%bm% &ewith the value &f%value% &eand the following values: &f%type% | %formula% | %reason%"}));
		languageKeys.put("CmdAdd.AddedTemporary", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"&eDer Spieler &f%player% &ehat den Bonus/Malus &f%bm% &emit dem Wert &f%value% &eund den folgenden Werten erhalten: &f%type% | %formula% | %duration% | %reason%",
						"&eThe player &f%player% &ehas received the bonus/malus &f%bm% &ewith the value &f%value% &eand the following values: &f%type% | %formula% | %duration% | %reason%"}));
	}
}