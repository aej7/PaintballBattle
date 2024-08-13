package pb.ajneb97.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.bukkit.configuration.file.FileConfiguration;

public class DatabaseConnection implements AutoCloseable {

	private volatile Connection connection;
	private final String host;
	private final int port;
	private final String database;
	private final String username;
	private final String password;
	private static final Logger logger = LogManager.getLogger(DatabaseConnection.class);

  public DatabaseConnection(FileConfiguration config){
		this.host = Objects.requireNonNull(config.getString("mysql-database.host"));
		this.port = Integer.parseInt(Objects.requireNonNull(config.getString("mysql-database.port")));
		this.database = Objects.requireNonNull(config.getString("mysql-database.database"));
		this.username = Objects.requireNonNull(config.getString("mysql-database.username"));
		this.password = Objects.requireNonNull(config.getString("mysql-database.password"));

		connectToDatabase();
		initializeTables();
	}
	
	public String getTablePlayers() {
    return "paintball_data";
	}
	
	public String getTablePerks(){
    return "paintball_perks";
	}
	
	public String getTableHats(){
    return "paintball_hats";
	}
	
	public String getDatabase() {
		return this.database;
	}
	
	private synchronized void connectToDatabase() {
		try {
				if(connection != null && !isConnectionClosed()) {
					logger.warn("Already connected to the database.");
					return;
				}

				Class.forName("com.mysql.cj.jdbc.Driver");
				String url = String.format("jdbc:mysql://%s:%d/%s", host, port, database);
				connection = DriverManager.getConnection(url, username, password);

				logger.info("Successfully connected to the database.");
		} catch (SQLException e) {
			logger.error("An error occurred while trying to connect to the database.", e);
		} catch (ClassNotFoundException e) {
			logger.error("MySQL JDBC Driver not found.", e);
		}
	}

	private boolean isConnectionClosed() {
		try {
			return connection == null || connection.isClosed();
		} catch (SQLException e) {
			logger.error("An error occurred while trying to check if the connection is closed.", e);
			return true;
		}
	}

	private void initializeTables() {
		MySql.createTablePlayers(this);
		MySql.createTablePerks(this);
		MySql.createTableHats(this);
	}
	
	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	@Override
	public void close() throws SQLException {
		if (connection != null && !connection.isClosed()) {
			connection.close();
			logger.info("Database connection closed.");
		}
	}
}
