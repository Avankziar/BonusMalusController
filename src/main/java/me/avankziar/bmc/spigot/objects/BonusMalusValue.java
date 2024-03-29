package main.java.me.avankziar.bmc.spigot.objects;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

import main.java.me.avankziar.bmc.spigot.database.MysqlHandable;
import main.java.me.avankziar.bmc.spigot.database.MysqlHandler;
import main.java.me.avankziar.ifh.general.bonusmalus.BonusMalusValueType;

public class BonusMalusValue implements MysqlHandable
{
	private int id;
	private UUID uuid;
	private String bonusMalusName;
	private BonusMalusValueType valueType;
	private double value;
	private String internReason;
	private String displayReason;
	private String server;
	private String world;
	private long duration;
	
	public BonusMalusValue(){}
	
	public BonusMalusValue(int id, UUID uuid, String bonusMalusName, BonusMalusValueType valueType,
			double value, String internReason, String displayReason, String server, String world, long duration)
	{
		setID(id);
		setUuid(uuid);
		setBonusMalusName(bonusMalusName);
		setValueType(valueType);
		setValue(value);
		setInternReason(internReason);
		setDisplayReason(displayReason);
		setServer(server);
		setWorld(world);
		setDuration(duration);
	}
	
	public BonusMalusValue(UUID uuid, String bonusMalusName, BonusMalusValueType valueType,
			double value, String internReason, String displayReason, String server, String world, long duration)
	{
		setID(0);
		setUuid(uuid);
		setBonusMalusName(bonusMalusName);
		setValueType(valueType);
		setValue(value);
		setInternReason(internReason);
		setDisplayReason(displayReason);
		setServer(server);
		setWorld(world);
		setDuration(duration);
	}
	
	public int getID()
	{
		return id;
	}

	public void setID(int id)
	{
		this.id = id;
	}

	public UUID getUuid()
	{
		return uuid;
	}

	public void setUuid(UUID uuid)
	{
		this.uuid = uuid;
	}

	public String getBonusMalusName()
	{
		return bonusMalusName;
	}

	public void setBonusMalusName(String bonusMalusName)
	{
		this.bonusMalusName = bonusMalusName;
	}

	public BonusMalusValueType getValueType()
	{
		return valueType;
	}

	public void setValueType(BonusMalusValueType valueType)
	{
		this.valueType = valueType;
	}

	public double getValue()
	{
		return value;
	}

	public void setValue(double value)
	{
		this.value = value;
	}

	public String getInternReason()
	{
		return internReason;
	}

	public void setInternReason(String internReason)
	{
		this.internReason = internReason;
	}

	public String getDisplayReason()
	{
		return displayReason;
	}

	public void setDisplayReason(String displayReason)
	{
		this.displayReason = displayReason;
	}

	public String getServer()
	{
		return server;
	}

	public void setServer(String server)
	{
		this.server = server;
	}

	public String getWorld()
	{
		return world;
	}

	public void setWorld(String world)
	{
		this.world = world;
	}

	public long getDuration()
	{
		return duration;
	}

	public void setDuration(long duration)
	{
		this.duration = duration;
	}

	@Override
	public boolean create(Connection conn, String tablename)
	{
		try
		{
			String sql = "INSERT INTO `" + tablename
					+ "`(`player_uuid`, `bonus_malus_name`, `bonus_malus_value_type`, `bonus_malus_value`,"
					+ " `intern_reason`, `display_reason`,"
					+ " `server`, `world`, `duration`) " 
					+ "VALUES("
					+ "?, ?, ?, ?, "
					+ "?, ?, "
					+ "?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, uuid.toString());
	        ps.setString(2, bonusMalusName);
	        ps.setString(3, valueType.toString());
	        ps.setDouble(4, value);
	        ps.setString(5, internReason);
	        ps.setString(6, displayReason);
	        ps.setString(7, server);
	        ps.setString(8, world);
	        ps.setLong(9, duration);
	        int i = ps.executeUpdate();
	        MysqlHandler.addRows(MysqlHandler.QueryType.INSERT, i);
	        return true;
		} catch (SQLException e)
		{
			this.log(Level.WARNING, "SQLException! Could not create a "+this.getClass().getSimpleName()+" Object!", e);
		}
		return false;
	}

	@Override
	public boolean update(Connection conn, String tablename, String whereColumn, Object... whereObject)
	{
		try
		{
			String sql = "UPDATE `" + tablename
				+ "` SET `player_uuid` = ?, `bonus_malus_name` = ?, `bonus_malus_value_type` = ?, `bonus_malus_value` = ?,"
				+ " `intern_reason` = ?, `display_reason` = ?,"
				+ " `server` = ?, `world` = ?, `duration` = ?" 
				+ " WHERE "+whereColumn;
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, uuid.toString());
	        ps.setString(2, bonusMalusName);
	        ps.setString(3, valueType.toString());
	        ps.setDouble(4, value);
	        ps.setString(5, internReason);
	        ps.setString(6, displayReason);
	        ps.setString(7, server);
	        ps.setString(8, world);
	        ps.setLong(9, duration);
			int i = 10;
			for(Object o : whereObject)
			{
				ps.setObject(i, o);
				i++;
			}			
			int u = ps.executeUpdate();
			MysqlHandler.addRows(MysqlHandler.QueryType.UPDATE, u);
			return true;
		} catch (SQLException e)
		{
			this.log(Level.WARNING, "SQLException! Could not update a "+this.getClass().getSimpleName()+" Object!", e);
		}
		return false;
	}

	@Override
	public ArrayList<Object> get(Connection conn, String tablename, String orderby, String limit, String whereColumn, Object... whereObject)
	{
		try
		{
			String sql = "SELECT * FROM `" + MysqlHandler.Type.BONUSMALUSVALUE.getValue() 
				+ "` WHERE "+whereColumn+" ORDER BY "+orderby+limit;
			PreparedStatement ps = conn.prepareStatement(sql);
			int i = 1;
			for(Object o : whereObject)
			{
				ps.setObject(i, o);
				i++;
			}
			
			ResultSet rs = ps.executeQuery();
			MysqlHandler.addRows(MysqlHandler.QueryType.READ, rs.getMetaData().getColumnCount());
			ArrayList<Object> al = new ArrayList<>();
			while (rs.next()) 
			{
				al.add(
						new BonusMalusValue(
								rs.getInt("id"),
								UUID.fromString(rs.getString("player_uuid")),
								rs.getString("bonus_malus_name"),
								BonusMalusValueType.valueOf(rs.getString("bonus_malus_value_type")),
								rs.getDouble("bonus_malus_value"),
								rs.getString("intern_reason"),
								rs.getString("display_reason"),
								rs.getString("server"),
								rs.getString("world"),
								rs.getLong("duration")));
			}
			return al;
		} catch (SQLException e)
		{
			this.log(Level.WARNING, "SQLException! Could not get a "+this.getClass().getSimpleName()+" Object!", e);
		}
		return new ArrayList<>();
	}
	
	public static ArrayList<BonusMalusValue> convert(ArrayList<Object> arrayList)
	{
		ArrayList<BonusMalusValue> l = new ArrayList<>();
		for(Object o : arrayList)
		{
			if(o instanceof BonusMalusValue)
			{
				l.add((BonusMalusValue) o);
			}
		}
		return l;
	}
}