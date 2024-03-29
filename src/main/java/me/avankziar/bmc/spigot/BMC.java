package main.java.me.avankziar.bmc.spigot;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import main.java.me.avankziar.bmc.spigot.assistance.BackgroundTask;
import main.java.me.avankziar.bmc.spigot.cmd.BMCCmdExecutor;
import main.java.me.avankziar.bmc.spigot.cmd.TabCompletion;
import main.java.me.avankziar.bmc.spigot.cmd.bmc.ARGAdd;
import main.java.me.avankziar.bmc.spigot.cmd.bmc.ARGBoni;
import main.java.me.avankziar.bmc.spigot.cmd.bmc.ARGRegistered;
import main.java.me.avankziar.bmc.spigot.cmd.bmc.ARGRemove;
import main.java.me.avankziar.bmc.spigot.cmdtree.ArgumentConstructor;
import main.java.me.avankziar.bmc.spigot.cmdtree.ArgumentModule;
import main.java.me.avankziar.bmc.spigot.cmdtree.BaseConstructor;
import main.java.me.avankziar.bmc.spigot.cmdtree.CommandConstructor;
import main.java.me.avankziar.bmc.spigot.cmdtree.CommandExecuteType;
import main.java.me.avankziar.bmc.spigot.conditionbonusmalus.Bypass;
import main.java.me.avankziar.bmc.spigot.database.MysqlHandler;
import main.java.me.avankziar.bmc.spigot.database.MysqlSetup;
import main.java.me.avankziar.bmc.spigot.database.YamlHandler;
import main.java.me.avankziar.bmc.spigot.database.YamlManager;
import main.java.me.avankziar.bmc.spigot.handler.ConfigHandler;
import main.java.me.avankziar.bmc.spigot.ifh.BonusMalusProvider;
import main.java.me.avankziar.bmc.spigot.listener.JoinQuitListener;
import main.java.me.avankziar.ifh.general.condition.Condition;
import main.java.me.avankziar.ifh.spigot.administration.Administration;
import main.java.me.avankziar.ifh.spigot.metrics.Metrics;

public class BMC extends JavaPlugin
{
	public static Logger log;
	private static BMC plugin;
	public String pluginName = "BonusMalusController";
	private YamlHandler yamlHandler;
	private YamlManager yamlManager;
	private MysqlSetup mysqlSetup;
	private MysqlHandler mysqlHandler;
	private BackgroundTask backgroundTask;
	
	private ArrayList<BaseConstructor> helpList = new ArrayList<>();
	private ArrayList<CommandConstructor> commandTree = new ArrayList<>();
	private LinkedHashMap<String, ArgumentModule> argumentMap = new LinkedHashMap<>();
	
	public static String infoCommand = "/";
	
	private Administration rootAConsumer;
	private BonusMalusProvider bmProvider;
	private Condition conditionConsumer;
	
	public void onEnable()
	{
		plugin = this;
		log = getLogger();
		
		setupIFHAdministration();
		
		//https://patorjk.com/software/taag/#p=display&f=ANSI%20Shadow&t=BMC
		log.info(" ██████╗ ███╗   ███╗ ██████╗ | API-Version: "+plugin.getDescription().getAPIVersion());
		log.info(" ██╔══██╗████╗ ████║██╔════╝ | Author: "+plugin.getDescription().getAuthors().toString());
		log.info(" ██████╔╝██╔████╔██║██║      | Plugin Website: "+plugin.getDescription().getWebsite());
		log.info(" ██╔══██╗██║╚██╔╝██║██║      | Depend Plugins: "+plugin.getDescription().getDepend().toString());
		log.info(" ██████╔╝██║ ╚═╝ ██║╚██████╗ | SoftDepend Plugins: "+plugin.getDescription().getSoftDepend().toString());
		log.info(" ╚═════╝ ╚═╝     ╚═╝ ╚═════╝ | LoadBefore: "+plugin.getDescription().getLoadBefore().toString());
		
		yamlHandler = new YamlHandler(plugin);
		
		String path = plugin.getYamlHandler().getConfig().getString("IFHAdministrationPath");
		boolean adm = plugin.getAdministration() != null 
				&& plugin.getYamlHandler().getConfig().getBoolean("useIFHAdministration")
				&& plugin.getAdministration().isMysqlPathActive(path);
		if(adm || yamlHandler.getConfig().getBoolean("Mysql.Status", false) == true)
		{
			mysqlHandler = new MysqlHandler(plugin);
			mysqlSetup = new MysqlSetup(plugin);
		} else
		{
			log.severe("MySQL is not set in the Plugin " + pluginName + "!");
			Bukkit.getPluginManager().getPlugin(pluginName).getPluginLoader().disablePlugin(plugin);
			return;
		}
		
		backgroundTask = new BackgroundTask(plugin);
		
		setupBypassPerm();
		setupCommandTree();
		setupListeners();
		setupIFHProvider();
		setupIFHConsumer();
		setupBstats();
	}
	
	public void onDisable()
	{
		Bukkit.getScheduler().cancelTasks(plugin);
		HandlerList.unregisterAll(plugin);
		log.info(pluginName + " is disabled!");
	}

	public static BMC getPlugin()
	{
		return plugin;
	}
	
	public YamlHandler getYamlHandler() 
	{
		return yamlHandler;
	}
	
	public YamlManager getYamlManager()
	{
		return yamlManager;
	}

	public void setYamlManager(YamlManager yamlManager)
	{
		this.yamlManager = yamlManager;
	}
	
	public MysqlSetup getMysqlSetup() 
	{
		return mysqlSetup;
	}
	
	public MysqlHandler getMysqlHandler()
	{
		return mysqlHandler;
	}
	
	public BackgroundTask getBackgroundTask()
	{
		return backgroundTask;
	}
	
	public String getServername()
	{
		return getPlugin().getAdministration() != null ? getPlugin().getAdministration().getSpigotServerName() 
				: getPlugin().getYamlHandler().getConfig().getString("ServerName");
	}
	
	private void setupCommandTree()
	{		
		infoCommand += plugin.getYamlHandler().getCommands().getString("bmc.Name");
		
		TabCompletion tab = new TabCompletion(plugin);
		
		ArgumentConstructor add = new ArgumentConstructor(CommandExecuteType.BMC_ADD, "bmc_add", 0, 7, 999, true, null);
		new ARGAdd(add);
		ArgumentConstructor boni = new ArgumentConstructor(CommandExecuteType.BMC_BONI, "bmc_boni", 0, 0, 3, true, null);
		new ARGBoni(boni);
		ArgumentConstructor registered = new ArgumentConstructor(CommandExecuteType.BMC_REGISTERED, "bmc_registered", 0, 0, 1, false, null);
		new ARGRegistered(registered);
		ArgumentConstructor remove = new ArgumentConstructor(CommandExecuteType.BMC_REMOVE, "bmc_remove", 0, 2, 3, true, null);
		new ARGRemove(remove);
		
		CommandConstructor bmc = new CommandConstructor(CommandExecuteType.BMC, "bmc", true,
				add, boni, registered, remove);
		registerCommand(bmc.getPath(), bmc.getName());
		getCommand(bmc.getName()).setExecutor(new BMCCmdExecutor(plugin, bmc));
		getCommand(bmc.getName()).setTabCompleter(tab);
	}
	
	public void setupBypassPerm()
	{
		String path = "Bypass.";
		for(Bypass.Permission bypass : new ArrayList<Bypass.Permission>(EnumSet.allOf(Bypass.Permission.class)))
		{
			Bypass.set(bypass, yamlHandler.getCommands().getString(path+bypass.toString()));
		}
	}
	
	public ArrayList<BaseConstructor> getCommandHelpList()
	{
		return helpList;
	}
	
	public void addingCommandHelps(BaseConstructor... objects)
	{
		for(BaseConstructor bc : objects)
		{
			helpList.add(bc);
		}
	}
	
	public ArrayList<CommandConstructor> getCommandTree()
	{
		return commandTree;
	}
	
	public CommandConstructor getCommandFromPath(String commandpath)
	{
		CommandConstructor cc = null;
		for(CommandConstructor coco : getCommandTree())
		{
			if(coco.getPath().equalsIgnoreCase(commandpath))
			{
				cc = coco;
				break;
			}
		}
		return cc;
	}
	
	public CommandConstructor getCommandFromCommandString(String command)
	{
		CommandConstructor cc = null;
		for(CommandConstructor coco : getCommandTree())
		{
			if(coco.getName().equalsIgnoreCase(command))
			{
				cc = coco;
				break;
			}
		}
		return cc;
	}
	
	public void registerCommand(String... aliases) 
	{
		PluginCommand command = getCommand(aliases[0], plugin);
	 
		command.setAliases(Arrays.asList(aliases));
		getCommandMap().register(plugin.getDescription().getName(), command);
	}
	 
	private static PluginCommand getCommand(String name, BMC plugin) 
	{
		PluginCommand command = null;
	 
		try 
		{
			Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
			c.setAccessible(true);
	 
			command = c.newInstance(name, plugin);
		} catch (SecurityException e) 
		{
			e.printStackTrace();
		} catch (IllegalArgumentException e) 
		{
			e.printStackTrace();
		} catch (IllegalAccessException e) 
		{
			e.printStackTrace();
		} catch (InstantiationException e) 
		{
			e.printStackTrace();
		} catch (InvocationTargetException e) 
		
		{
			e.printStackTrace();
		} catch (NoSuchMethodException e) 
		{
			e.printStackTrace();
		}
	 
		return command;
	}
	 
	private static CommandMap getCommandMap() 
	{
		CommandMap commandMap = null;
	 
		try {
			if (Bukkit.getPluginManager() instanceof SimplePluginManager) 
			{
				Field f = SimplePluginManager.class.getDeclaredField("commandMap");
				f.setAccessible(true);
	 
				commandMap = (CommandMap) f.get(Bukkit.getPluginManager());
			}
		} catch (NoSuchFieldException e) 
		{
			e.printStackTrace();
		} catch (SecurityException e) 
		{
			e.printStackTrace();
		} catch (IllegalArgumentException e) 
		{
			e.printStackTrace();
		} catch (IllegalAccessException e) 
		{
			e.printStackTrace();
		}
	 
		return commandMap;
	}
	
	public LinkedHashMap<String, ArgumentModule> getArgumentMap()
	{
		return argumentMap;
	}
	
	public void setupListeners()
	{
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new JoinQuitListener(plugin), plugin);
	}
	
	private boolean setupIFHProvider()
	{
		if(!plugin.getServer().getPluginManager().isPluginEnabled("InterfaceHub")) 
	    {
			log.severe("IFH is not set in the Plugin " + pluginName + "! Disable plugin!");
			Bukkit.getPluginManager().getPlugin(pluginName).getPluginLoader().disablePlugin(this);
	    	return false;
	    }
		bmProvider = new BonusMalusProvider(plugin);
    	plugin.getServer().getServicesManager().register(
        main.java.me.avankziar.ifh.general.bonusmalus.BonusMalus.class,
        bmProvider,
        this,
        ServicePriority.Normal);
    	log.info(pluginName + " detected InterfaceHub >>> BonusMalus.class is provided!");
		return false;
	}
	
	public BonusMalusProvider getBonusMalus()
	{
		return bmProvider;
	}

	private void setupIFHAdministration()
	{ 
		if(!plugin.getServer().getPluginManager().isPluginEnabled("InterfaceHub")) 
	    {
	    	return;
	    }
		RegisteredServiceProvider<main.java.me.avankziar.ifh.spigot.administration.Administration> rsp = 
                getServer().getServicesManager().getRegistration(Administration.class);
		if (rsp == null) 
		{
		   return;
		}
		rootAConsumer = rsp.getProvider();
		log.info(pluginName + " detected InterfaceHub >>> Administration.class is consumed!");
	}
	
	public Administration getAdministration()
	{
		return rootAConsumer;
	}
	
	public void setupIFHConsumer()
	{
		setupIFHCondition();
	}
	
	public void setupIFHCondition()
	{
		if(!new ConfigHandler().isMechanicConditionEnabled())
		{
			return;
		}
		if(!plugin.getServer().getPluginManager().isPluginEnabled("InterfaceHub")) 
	    {
	    	return;
	    }
        new BukkitRunnable()
        {
        	int i = 0;
			@Override
			public void run()
			{
				try
				{
					if(i == 20)
				    {
						cancel();
				    	return;
				    }
				    RegisteredServiceProvider<main.java.me.avankziar.ifh.general.condition.Condition> rsp = 
		                             getServer().getServicesManager().getRegistration(
		                            		 main.java.me.avankziar.ifh.general.condition.Condition.class);
				    if(rsp == null) 
				    {
				    	i++;
				        return;
				    }
				    conditionConsumer = rsp.getProvider();
				    log.info(pluginName + " detected InterfaceHub >>> Condition.class is consumed!");
				    cancel();
				} catch(NoClassDefFoundError e)
				{
					cancel();
				}
				if(getCondition() != null)
				{
					for(BaseConstructor bc : getCommandHelpList())
					{
						if(!bc.isPutUpCmdPermToConditionSystem())
						{
							continue;
						}
						if(getCondition().isRegistered(bc.getConditionPath()))
						{
							continue;
						}
						String[] ex = {plugin.getYamlHandler().getCommands().getString(bc.getPath()+".Explanation")};
						getCondition().register(
								bc.getConditionPath(),
								plugin.getYamlHandler().getCommands().getString(bc.getPath()+".Displayname", "Command "+bc.getName()),
								ex);
					}
					List<Bypass.Permission> list = new ArrayList<Bypass.Permission>(EnumSet.allOf(Bypass.Permission.class));
					for(Bypass.Permission ept : list)
					{
						if(getCondition().isRegistered(ept.getCondition()))
						{
							continue;
						}
						List<String> lar = plugin.getYamlHandler().getCBMLang().getStringList(ept.toString()+".Explanation");
						getCondition().register(
								ept.getCondition(),
								plugin.getYamlHandler().getCBMLang().getString(ept.toString()+".Displayname", ept.toString()),
								lar.toArray(new String[lar.size()]));
					}
				}				
			}
        }.runTaskTimer(plugin, 0L, 20*2);
	}
	
	public Condition getCondition()
	{
		return conditionConsumer;
	}
	
	public void setupBstats()
	{
		int pluginId = 16373; //Bungee 16374
        new Metrics(this, pluginId);
	}
}