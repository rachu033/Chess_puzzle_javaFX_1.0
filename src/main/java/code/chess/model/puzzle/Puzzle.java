package code.chess.model.puzzle;

import java.time.LocalDateTime;
import java.util.List;

public class Puzzle {
    private final String pgn;
    private final int rating;
    private final List<String> solution;
    private final LocalDateTime dateTime;
    private boolean isSolved;

    public Puzzle(String pgn, int rating, List<String> solution) {
        this.pgn = pgn;
        this.rating = rating;
        this.solution = solution;
        this.dateTime = LocalDateTime.now();
        this.isSolved = false;
    }

    public Puzzle(String pgn, int rating, List<String> solution, LocalDateTime dateTime) {
        this.pgn = pgn;
        this.rating = rating;
        this.solution = solution;
        this.dateTime = dateTime;
        this.isSolved = false;
    }

    public boolean isSolved() {
        return isSolved;
    }

    public void setSolved(boolean solved) {
        isSolved = solved;
    }

    public String getPgn() {
        return pgn;
    }

    public int getRating() {
        return rating;
    }

    public List<String> getSolution() {
        return solution;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }
}
