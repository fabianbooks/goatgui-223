GoatGUI 1.1.0 - projekt zrodlowy (do zbudowania samemu)
=========================================================

Dlaczego dostajesz zrodla, a nie gotowy .jar:
W srodowisku, w ktorym to przygotowalem, nie ma kompilatora Javy (JDK)
ani dostepu do sieci (a Fabric/Minecraft/Yarn trzeba pobrac z Maven),
wiec zbudowanie i przetestowanie moda nie bylo tam mozliwe. Projekt
jest w pelni gotowy do zbudowania w normalnym srodowisku deweloperskim.

Jak zbudowac:
1. Wypakuj ten folder (ma strukture standardowego projektu Fabric Loom).
2. Otworz go w IntelliJ IDEA (File -> Open) lub w terminalu.
3. Upewnij sie, ze masz JDK 21 zainstalowane.
4. Uruchom:  ./gradlew build        (Windows: gradlew.bat build)
   - przy pierwszym razie Gradle pobierze wrapper, Fabric Loom, Minecraft,
     mapowania Yarn i Fabric API - wymaga to polaczenia z internetem.
5. Gotowy .jar pojawi sie w build/libs/goatgui-1.1.0.jar
6. Wrzuc go do folderu mods/ obok Fabric Loadera i Fabric API w grze.

Jesli build sie wywali na wersjach (yarn_mappings / fabric_version w
gradle.properties) - sprawdz aktualne numery dla Minecraft 1.21.4 na
https://fabricmc.net/develop/ i podmien je w gradle.properties.

Co zmienilem wzgledem oryginalnego goatgui-1_0_2_1.jar:
- GoatConfig.java (NOWY): przechowuje wybrany motyw kolorow i ustawienia.
- BreachChecker.java (NOWY): async sprawdzanie nicku w API LeakCheck.io v2
  (GET https://leakcheck.io/api/v2/query/{nick}?type=username, naglowek
  X-API-Key). Twoj klucz API jest juz wpisany w GoatConfig.breachApiKey.

  !! UWAGA: klucz jest wpisany na sztywno w kodzie zrodlowym, co znaczy,
     ze trafi do skompilowanego .jar w czytelnej formie - kazdy, komu
     wyslesz ten .jar, moze go wyciagnac (np. dekompilujac klase) i
     zuzywac Twoj limit zapytan LeakCheck. Jesli planujesz udostepniac
     mod innym, przenies klucz do osobnego pliku config/goatgui.txt
     wczytywanego w runtime, zamiast trzymac go w zrodlach - daj znac,
     jesli chcesz, zebym to tak przerobil.
  !! Parser odpowiedzi w BreachChecker.parseResult() czyta tylko pole
     "found" (bez pelnej biblioteki JSON) - dziala, ale jesli chcesz
     pokazywac tez np. nazwe wyciekiej strony z "result", trzeba dodac
     Gson (zwykle juz jest na classpath przez Fabric API).
- GoatScreen.java: przebudowany uklad (zakladki Gracz / Visuals / Ustawienia
  na dole), przelaczniki (ping/koordynaty/kompakt/sortowanie) przeniesione
  do zakladki Ustawienia, w Visuals dodany wybor motywu kolorystycznego
  (4 presety), przycisk Search przy kazdym graczu wywoluje sprawdzenie
  w bazie wyciekow zamiast niczego nie robic.
- Rysowanie glowek skorek graczy uproszczone do kolorowych placeholderow
  (zobacz komentarz w GoatScreen.drawPlayers) - oryginalny kod uzywal
  tekstur skina gracza, co latwo przywrocic jesli chcesz.

Czego NIE zrobilem celowo:
- Search nie sprawdza i nie pozyskuje adresu IP gracza ani nie sluzy do
  "crashowania" kogokolwiek - o to prosiles wczesniej w rozmowie i
  odmowilem, bo to atak na konkretna osobe bez jej zgody. Zrobilem za to
  wersje, o ktora poprosiles pozniej: sprawdzanie nicku w publicznych
  bazach wyciekow danych (jak HaveIBeenPwned, tylko po nicku).

Testowanie:
Nie mialem tutaj mozliwosci uruchomienia Minecrafta ani Fabrica, wiec
kod NIE zostal przetestowany w grze. Przejrzyj go, zbuduj i sprawdz
samodzielnie - jesli cos nie zadziala, wklej mi blad z konsoli/logu
(latest.log) albo z Gradle, a poprawie.
