package pl.goat.gui;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sprawdza, czy dany nick pojawil sie w publicznie znanych wyciekach danych,
 * przez LeakCheck.io API v2:
 *   GET https://leakcheck.io/api/v2/query/{nick}?type=username
 *   naglowek: X-API-Key: <klucz>
 *
 * Odpowiedz (wg dokumentacji LeakCheck):
 *   {"success": true, "found": 0, "quota": 400, "result": []}
 *   {"success": true, "found": 2, "quota": 399, "result": [ {...}, {...} ]}
 *
 * UWAGA: ten kod nie byl przetestowany w tym srodowisku (brak dostepu do sieci
 * w sandboxie) - sprawdz samemu w grze i daj znac jesli cos nie gra.
 */
public class BreachChecker {

    public enum Status { LOADING, FOUND, CLEAN, ERROR }

    public static class Result {
        public final Status status;
        public final String detail;

        public Result(Status status, String detail) {
            this.status = status;
            this.detail = detail;
        }
    }

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    // Cache wynikow na sesje gry, zeby nie spamowac API przy kazdym otwarciu GUI
    private static final Map<String, Result> CACHE = new ConcurrentHashMap<>();

    public static Result cached(String nick) {
        return CACHE.get(nick.toLowerCase());
    }

    /**
     * Asynchroniczne zapytanie. Wynik laduje w cache, GoatScreen odczytuje go
     * przy kolejnym rysowaniu klatki (polling cache w drawPlayers()).
     */
    public static void checkAsync(String nick) {
        String key = nick.toLowerCase();
        CACHE.put(key, new Result(Status.LOADING, null));

        if (GoatConfig.breachApiKey == null || GoatConfig.breachApiKey.isBlank()) {
            CACHE.put(key, new Result(Status.ERROR, "Brak klucza API"));
            return;
        }

        String encoded = URLEncoder.encode(nick, StandardCharsets.UTF_8);
        String url = GoatConfig.BREACH_API_BASE + encoded + "?type=username";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("X-API-Key", GoatConfig.breachApiKey)
                .timeout(Duration.ofSeconds(8))
                .GET()
                .build();

        CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> {
                    // logujemy surowa odpowiedz do konsoli/latest.log - przydatne do
                    // diagnozy, jesli LeakCheck zwroci cos innego niz oczekujemy
                    System.out.println("[GoatGUI/BreachChecker] HTTP " + resp.statusCode() + " body=" + resp.body());
                    CACHE.put(key, parseResult(resp.statusCode(), resp.body()));
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    CACHE.put(key, new Result(Status.ERROR, "Siec: " + ex.getClass().getSimpleName()));
                    return null;
                });
    }

    private static Result parseResult(int statusCode, String body) {
        switch (statusCode) {
            case 200 -> {
                // bardzo prosty parser pola "found": N bez pelnej biblioteki JSON
                // (Gson jest zazwyczaj na classpath przez Fabric API - mozesz to
                // podmienic na porzadne parsowanie, jesli chcesz tez pokazac
                // np. nazwe zrodla wycieku z "result").
                int found = extractIntField(body, "\"found\"");
                if (found > 0) {
                    return new Result(Status.FOUND, "Znaleziono w " + found + " wyciekach");
                }
                return new Result(Status.CLEAN, "Brak w znanych wyciekach");
            }
            case 401 -> {
                return new Result(Status.ERROR, "Brak naglowka X-API-Key");
            }
            case 400 -> {
                return new Result(Status.ERROR, "Nieprawidlowy klucz/zapytanie");
            }
            case 403 -> {
                return new Result(Status.ERROR, "Wymagany aktywny plan LeakCheck");
            }
            case 429 -> {
                return new Result(Status.ERROR, "Za duzo zapytan, zwolnij");
            }
            default -> {
                return new Result(Status.ERROR, "HTTP " + statusCode);
            }
        }
    }

    /** Minimalny, odporny na brak spacji parser pojedynczego pola liczbowego z JSON-a. */
    private static int extractIntField(String json, String fieldKey) {
        try {
            int idx = json.indexOf(fieldKey);
            if (idx < 0) return 0;
            int colon = json.indexOf(':', idx + fieldKey.length());
            if (colon < 0) return 0;
            int i = colon + 1;
            while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
            int start = i;
            while (i < json.length() && Character.isDigit(json.charAt(i))) i++;
            if (start == i) return 0;
            return Integer.parseInt(json.substring(start, i));
        } catch (Exception e) {
            return 0;
        }
    }
}
