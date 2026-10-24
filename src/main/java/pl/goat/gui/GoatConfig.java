package pl.goat.gui;

/**
 * Proste, statyczne przechowywanie ustawień GUI w pamięci (reset po restarcie gry).
 * Jeśli chcesz zapis do pliku (np. config/goatgui.json), dopisz to tutaj z Gson —
 * Fabric API zwykle ma Gson na classpath.
 */
public class GoatConfig {

    // ---- Motywy kolorystyczne (Visuals) ----
    public enum Theme {
        ROZOWY("Rozowy", 0xFFE05A8F, 0xFFC94B7D),
        NIEBIESKI("Niebieski", 0xFF3B82F6, 0xFF2563EB),
        ZIELONY("Zielony", 0xFF22C55E, 0xFF16A34A),
        FIOLETOWY("Fioletowy", 0xFF8B5CF6, 0xFF7C3AED);

        public final String label;
        public final int accent;
        public final int accentHover;

        Theme(String label, int accent, int accentHover) {
            this.label = label;
            this.accent = accent;
            this.accentHover = accentHover;
        }

        public Theme next() {
            Theme[] v = values();
            return v[(this.ordinal() + 1) % v.length];
        }
    }

    public static Theme theme = Theme.ROZOWY;

    // ---- Ustawienia (przeniesione tu logicznie spod Visuals) ----
    public static boolean showCoords = false;
    public static boolean showPing = true;
    public static boolean compactList = false;
    public static boolean sortByName = false;

    // ---- LeakCheck.io API v2 (sprawdzanie nicku w wyciekach) ----
    // Baza: https://leakcheck.io/api/v2/query/{nick}?type=username
    // Klucz przekazywany w naglowku X-API-Key.
    public static final String BREACH_API_BASE = "https://leakcheck.io/api/v2/query/";
    public static String breachApiKey = "5ea20588b2648c923c575cb4596186af3d51a2ae";
}
