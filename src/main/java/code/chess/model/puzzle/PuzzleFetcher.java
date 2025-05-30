package code.chess.model.puzzle;

import com.google.gson.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class PuzzleFetcher {

    public static Puzzle fetchPuzzle() throws IOException, InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException("FetchPuzzle interrupted before request.");
        }

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(5))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://lichess.org/api/puzzle/next"))
                .header("Accept", "application/json")
                .timeout(java.time.Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (Thread.interrupted()) {
            throw new InterruptedException("FetchPuzzle interrupted after request.");
        }

        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch puzzle. HTTP code: " + response.statusCode());
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

        String pgn = json.getAsJsonObject("game").get("pgn").getAsString();
        int rating = json.getAsJsonObject("puzzle").get("rating").getAsInt();

        List<String> solution = new ArrayList<>();
        JsonArray solutionArray = json.getAsJsonObject("puzzle").getAsJsonArray("solution");
        for (JsonElement move : solutionArray) {
            solution.add(move.getAsString());
        }

        return new Puzzle(pgn, rating, solution);
    }

}
