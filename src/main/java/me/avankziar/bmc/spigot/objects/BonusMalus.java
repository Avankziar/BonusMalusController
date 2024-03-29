package main.java.me.avankziar.bmc.spigot.objects;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;

import main.java.me.avankziar.bmc.spigot.database.MysqlHandable;
import main.java.me.avankziar.bmc.spigot.database.MysqlHandler;
import main.java.me.avankziar.ifh.general.bonusmalus.BonusMalusType;

public class BonusMalus implements MysqlHandable
{
	private String bonusMalusName;
	private String displayBonusMalusName;
	private BonusMalusType bonusMalusType;
	private ArrayList<String> explanation;
	
	public BonusMalus(){}
	
	public BonusMalus(String bonusMalusName, String displayBonusMalusName,
			BonusMalusType bonusMalusType, String[] explanation)
	{
		setBonusMalusName(bonusMalusName);
		setDisplayBonusMalusName(displayBonusMalusName);
		setBonusMalusType(bonusMalusType);
		ArrayList<String> ex = new ArrayList<>();
		if(explanation != null)
		{
			for(String s : explanation)
			{
				ex.add(s);
			}
		}		
		setExplanation(ex);
	}

	public String getBonusMalusName()
	{
		return bonusMalusName;
	}

	public void setBonusMalusName(String bonusMalusName)
	{
		this.bonusMalusName = bonusMalusName;
	}

	public String getDisplayBonusMalusName()
	{
		return displayBonusMalusName;
	}

	public void setDisplayBonusMalusName(String displayBonusMalusName)
	{
		this.displayBonusMalusName = displayBonusMalusName;
	}

	public BonusMalusType getBonusMalusType()
	{
		return bonusMalusType;
	}

	public void setBonusMalusType(BonusMalusType bonusMalusType)
	{
		this.bonusMalusType = bonusMalusType;
	}

	public ArrayList<String> getExplanation()
	{
		return explanation;
	}

	public void setExplanation(ArrayList<String> explanation)
	{
		this.explanation = explanation;
	}

	@Override
	public boolean create(Connection conn, String tablename)
	{
		try
		{
			String sql = "INSERT INTO `" + tablename
					+ "`(`bonus_malus_name`, `display_name`,"
					+ " `bonus_malus_type`, `explanation`) " 
					+ "VALUES("
					+ "?, ?, ?, "
					+ "?)";
			PreparedStatement ps = conn.prepareStatement(sql);
	        ps.setString(1, bonusMalusName);
	        ps.setString(2, displayBonusMalusName);
	        ps.setString(3, bonusMalusType.toString());
	        ps.setString(4, String.join("~!~", explanation));
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
				+ "` SET `bonus_malus_name` = ?, `display_name` = ?, `bonus_malus_type` = ?,"
				+ " `explanation` = ?" 
				+ " WHERE "+whereColumn;
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, bonusMalusName);
	        ps.setString(2, displayBonusMalusName);
	        ps.setString(3, bonusMalusType.toString());
	        ps.setString(4, String.join("~!~", explanation));
			int i = 5;
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
			String sql = "SELECT * FROM `" + MysqlHandler.Type.BONUSMALUS.getValue() 
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
						new BonusMalus(rs.getString("bonus_malus_name"),
						rs.getString("display_name"),
						BonusMalusType.valueOf(rs.getString("bonus_malus_type")),
						rs.getString("explanation").split("~!~")));
			}
			return al;
		} catch (SQLException e)
		{
			this.log(Level.WARNING, "SQLException! Could not get a "+this.getClass().getSimpleName()+" Object!", e);
		}
		return new ArrayList<>();
	}
	
	public static ArrayList<BonusMalus> convert(ArrayList<Object> arrayList)
	{
		ArrayList<BonusMalus> l = new ArrayList<>();
		for(Object o : arrayList)
		{
			if(o instanceof BonusMalus)
			{
				l.add((BonusMalus) o);
			}
		}
		return l;
	}
}