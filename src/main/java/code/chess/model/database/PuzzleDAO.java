package code.chess.model.database;

import code.chess.model.puzzle.Puzzle;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class PuzzleDAO {
    private static final Log logger = LogFactory.getLog(PuzzleDAO.class);

    private final ObjectMapper mapper = new ObjectMapper();

    public PuzzleDAO() {}

    public void savePuzzle(Puzzle puzzle) {
        String sql = "INSERT INTO puzzles (pgn, rating, solution, saved_at, isSolved) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, puzzle.getPgn());
            stmt.setInt(2, puzzle.getRating());
            stmt.setString(3, mapper.writeValueAsString(puzzle.getSolution()));
            stmt.setString(4, Timestamp.valueOf(puzzle.getDateTime()).toString());
            stmt.setInt(5, puzzle.isSolved() ? 1 : 0);
            stmt.executeUpdate();
        } catch (Exception e) {
            logger.error("Error saving puzzle: " + puzzle.getPgn(), e);
        }
    }

    public List<Puzzle> getAllPuzzles() {
        List<Puzzle> list = new ArrayList<>();
        String sql = "SELECT pgn, rating, solution, saved_at, isSolved FROM puzzles";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String pgn = rs.getString("pgn");
                int rating = rs.getInt("rating");
                List<String> solution = mapper.readValue(rs.getString("solution"), new TypeReference<>() {});
                boolean isSolved = rs.getInt("isSolved") == 1;
                Timestamp timestamp = Timestamp.valueOf(rs.getString("saved_at"));
                LocalDateTime createdAt = timestamp.toLocalDateTime();

                Puzzle puzzle = new Puzzle(pgn, rating, solution, createdAt);
                puzzle.setSolved(isSolved);
                list.add(puzzle);
            }
        } catch (Exception e) {
            logger.error("Error retrieving puzzles", e);
        }
        return list;
    }

    public Puzzle getPuzzleByPgn(String pgn) {
        String sql = "SELECT pgn, rating, solution, saved_at, isSolved FROM puzzles WHERE pgn = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pgn);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int rating = rs.getInt("rating");
                    List<String> solution = mapper.readValue(rs.getString("solution"), new TypeReference<>() {});
                    boolean isSolved = rs.getInt("isSolved") == 1;
                    Timestamp timestamp = Timestamp.valueOf(rs.getString("saved_at"));
                    LocalDateTime createdAt = timestamp.toLocalDateTime();
                    Puzzle puzzle = new Puzzle(pgn, rating, solution, createdAt);
                    puzzle.setSolved(isSolved);
                    return puzzle;
                }
            }
        } catch (Exception e) {
            logger.error("Error retrieving puzzle with PGN: " + pgn, e);
        }
        return null;
    }

    public void markPuzzleAsSolved(String pgn) {
        String sql = "UPDATE puzzles SET isSolved = 1 WHERE pgn = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pgn);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error marking puzzle as solved for PGN: " + pgn, e);
        }
    }
}
