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
import main.java.me.avankziar.ifh.general.bonusmalus.MultiplicationCalculationType;

public class BonusMalus implements MysqlHandable
{
	private String bonusMalusName;
	private String displayBonusMalusName;
	private boolean isBooleanBonus; //Define if the bonus/malus as boolean understood is.
	private BonusMalusType bonusMalusType;
	private MultiplicationCalculationType multiplicationCalculationType;
	private ArrayList<String> explanation;
	
	public BonusMalus(){}
	
	public BonusMalus(String bonusMalusName, String displayBonusMalusName,
			boolean isBooleanBonus, BonusMalusType bonusMalusType,
			MultiplicationCalculationType multiplicationCalculationType, String[] explanation)
	{
		setBonusMalusName(bonusMalusName);
		setDisplayBonusMalusName(displayBonusMalusName);
		setBooleanBonus(isBooleanBonus);
		setBonusMalusType(bonusMalusType);
		setMultiplicationCalculationType(multiplicationCalculationType);
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

	public boolean isBooleanBonus()
	{
		return isBooleanBonus;
	}

	public void setBooleanBonus(boolean isBooleanBonus)
	{
		this.isBooleanBonus = isBooleanBonus;
	}

	public BonusMalusType getBonusMalusType()
	{
		return bonusMalusType;
	}

	public void setBonusMalusType(BonusMalusType bonusMalusType)
	{
		this.bonusMalusType = bonusMalusType;
	}

	public MultiplicationCalculationType getMultiplicationCalculationType()
	{
		return multiplicationCalculationType;
	}

	public void setMultiplicationCalculationType(MultiplicationCalculationType multiplicationCalculationType)
	{
		this.multiplicationCalculationType = multiplicationCalculationType;
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
					+ "`(`bonus_malus_name`, `display_name`, `is_boolean_bonus`,"
					+ " `bonus_malus_type`, `multiplication_calculation_type`, `explanation`) " 
					+ "VALUES("
					+ "?, ?, ?, "
					+ "?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
	        ps.setString(1, bonusMalusName);
	        ps.setString(2, displayBonusMalusName);
	        ps.setBoolean(3, isBooleanBonus);
	        ps.setString(4, bonusMalusType.toString());
	        ps.setString(5, multiplicationCalculationType.toString());
	        ps.setString(6, String.join("~!~", explanation));
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
				+ "` SET `bonus_malus_name` = ?, `display_name` = ?, `is_boolean_bonus` = ?, `bonus_malus_type` = ?,"
				+ " `multiplicationcalculationtype` = ?, `explanation` = ?" 
				+ " WHERE "+whereColumn;
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, bonusMalusName);
	        ps.setString(2, displayBonusMalusName);
	        ps.setBoolean(3, isBooleanBonus);
	        ps.setString(4, bonusMalusType.toString());
	        ps.setString(5, multiplicationCalculationType.toString());
	        ps.setString(6, String.join("~!~", explanation));
			int i = 7;
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
						rs.getBoolean("is_boolean_bonus"),
						BonusMalusType.valueOf(rs.getString("bonus_malus_type")),
						MultiplicationCalculationType.valueOf(rs.getString("multiplication_calculation_type")),
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