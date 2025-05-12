package code.chess.model.database;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String DB_PATH = System.getProperty("user.home") + "/chess.db";
    private static final Log logger = LogFactory.getLog(Database.class);

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
    }

    public static void initialize() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            String sql = """
            CREATE TABLE IF NOT EXISTS puzzles (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                pgn TEXT NOT NULL,
                rating INTEGER,
                solution TEXT NOT NULL,
                saved_at TEXT NOT NULL,
                isSolved INTEGER DEFAULT 0
            )
            """;
            stmt.execute(sql);
        } catch (SQLException e) {
            logger.error("Error initializing database: ", e);
        }
    }
}