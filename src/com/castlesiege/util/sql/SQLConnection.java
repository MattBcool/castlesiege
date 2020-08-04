package com.castlesiege.util.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class SQLConnection
{
	private String sshHost = "198.204.247.130";
	private String sshuser = "root";
	private String sshpassword = "9zhns9nxk4a8";
	private String dbuserName = "root";
	private String dbpassword = "9zhns9nxk4a8";

	private int localPort = 3306; // any free port can be used
	private String remoteHost = "127.0.0.1";
	private int remotePort = 3306;
	private String localSSHUrl = "localhost";

	public Connection connection = null;
	public Session session = null;

	public void connectToServer(String dataBaseName) throws SQLException
	{
		connectSSH();
		connectToDataBase(dataBaseName);
	}

	public void connectSSH() throws SQLException
	{
		String driverName = "com.mysql.jdbc.Driver";

		try
		{
			java.util.Properties config = new java.util.Properties();
			JSch jsch = new JSch();
			session = jsch.getSession(sshuser, sshHost, 22);
			session.setPassword(sshpassword);
			config.put("StrictHostKeyChecking", "no");
			config.put("ConnectionAttempts", "3");
			session.setConfig(config);
			session.connect();

			System.out.println("SSH Connected");

			Class.forName(driverName).newInstance();

			int assinged_port = session.setPortForwardingL(localPort, remoteHost, remotePort);

			System.out.println("localhost:" + assinged_port + " -> " + remoteHost + ":" + remotePort);
			System.out.println("Port Forwarded");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void connectToDataBase(String dataBaseName) throws SQLException
	{
		try
		{
			// mysql database connectivity
			MysqlDataSource dataSource = new MysqlDataSource();
			dataSource.setServerName(localSSHUrl);
			dataSource.setPortNumber(localPort);
			dataSource.setUser(dbuserName);
			dataSource.setAllowMultiQueries(true);

			dataSource.setPassword(dbpassword);
			dataSource.setDatabaseName(dataBaseName);

			connection = dataSource.getConnection();

			System.out.print("Connection to server successful!:" + connection + "\n\n");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void closeConnections()
	{
		CloseDataBaseConnection();
		CloseSSHConnection();
	}

	public boolean checkConnection() throws SQLException
	{
		return connection != null && !connection.isClosed();
	}

	public void CloseDataBaseConnection()
	{
		try
		{
			if(connection != null && !connection.isClosed())
			{
				System.out.println("Closing Database Connection");
				connection.close();
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}

	}

	public void CloseSSHConnection()
	{
		if(session != null && session.isConnected())
		{
			System.out.println("Closing SSH Connection");
			session.disconnect();
		}
	}

	// works ONLY FOR single query (one SELECT or one DELETE etc)
	public ResultSet executeMyQuery(String query, String dataBaseName)
	{
		ResultSet resultSet = null;

		try
		{
			connectToServer(dataBaseName);
			Statement stmt = connection.createStatement();
			resultSet = stmt.executeQuery(query);
			System.out.println("Database connection success");
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}

		return resultSet;
	}

	public void DeleteOrganisationReferencesFromDB(String organisationsLike)
	{
		try
		{
			connectToServer("ServerName");
			Statement stmt = connection.createStatement();

			ResultSet resultSet = stmt.executeQuery("select * from DB1");

			String organisationsToDelete = "";
			List<String> organisationsIds = new ArrayList<String>();

			// create string with id`s values to delete organisations references
			while(resultSet.next())
			{
				String actualValue = resultSet.getString("id");
				organisationsIds.add(actualValue);
			}

			for(int i = 0; i < organisationsIds.size(); i++)
			{
				organisationsToDelete = " " + organisationsToDelete + organisationsIds.get(i);
				if(i != organisationsIds.size() - 1)
				{
					organisationsToDelete = organisationsToDelete + ", ";
				}
			}

			stmt.executeUpdate(" DELETE FROM `DB1`.`table1` WHERE `DB1`.`table1`.`organisation_id` in ( " + organisationsToDelete + " );");

		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			closeConnections();
		}
	}

	public List<String> getOrganisationsDBNamesBySubdomain(String organisationsLike)
	{
		List<String> organisationDbNames = new ArrayList<String>();
		ResultSet resultSet = executeMyQuery("select `DB`.organisation.dbname from `DB1`.organisation where subdomain like '" + organisationsLike + "%'", "DB1");
		try
		{
			while(resultSet.next())
			{
				String actualValue = resultSet.getString("dbname");
				organisationDbNames.add(actualValue);
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			closeConnections();
		}
		return organisationDbNames;
	}

	public List<String> getAllDBNames()
	{
		// get all live db names incentral DB
		List<String> organisationDbNames = new ArrayList<String>();
		ResultSet resultSet = executeMyQuery("show databases", "DB1");
		try
		{
			while(resultSet.next())
			{
				String actualValue = resultSet.getString("Database");
				organisationDbNames.add(actualValue);
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			closeConnections();
		}
		return organisationDbNames;
	}

	public void deleteDataBasesByName(List<String> DataBasesNamesList)
	{
		try
		{
			connectSSH();
			int dataBasesAmount = DataBasesNamesList.size();
			for(int i = 0; i < dataBasesAmount; i++)
			{
				connectToDataBase(DataBasesNamesList.get(i));

				Statement stmt = connection.createStatement();
				stmt.executeUpdate("DROP database `" + DataBasesNamesList.get(i) + "`");

			}

		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			CloseDataBaseConnection();
			closeConnections();
		}
	}
}