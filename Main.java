import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        TaskResults sessionResults = new TaskResults();
        FileManager fm = new FileManager();

        // завд. 1: Рядок з максимальною кількістю слів
        System.out.println("\n №1: Рядок з максимальною кількістю слів");
        System.out.print("Введіть шлях до існуючого текстового файлу (або натисніть Enter для пропуску): ");
        String textFile = scanner.nextLine().trim();

        if (!textFile.isEmpty()) {
            try {
                String line = fm.findMaxWordsLine(textFile);
                if (line.isEmpty()) {
                    System.out.println("У файлі немає жодного рядка зі словами.");
                } else {
                    sessionResults.maxWordsLine = line;
                    System.out.println("Рядок з найбільшою кількістю слів (" + countWords(line) + " слів):");
                    System.out.println(line);
                }
            } catch (IOException e) {
                System.out.println("Помилка читання файлу: " + e.getMessage());
            }
        }

        // №3 шифрування та дешифрування потоку
        System.out.println("\n№3: Шифрування потоку");
        System.out.print("Введіть рядок для шифрування: ");
        String rawText = scanner.nextLine();
        char keyChar = readKeyChar(scanner);
        String cryptoFileName = "encrypted.txt";

        try {
            fm.writeEncrypted(cryptoFileName, rawText, keyChar);
            System.out.println("Текст зашифровано та збережено у файл " + cryptoFileName);
        } catch (IOException e) {
            System.out.println("Помилка шифрування: " + e.getMessage());
        }

        try {
            String decrypted = fm.readDecrypted(cryptoFileName, keyChar);
            System.out.println("Розшифрований текст з файлу: " + decrypted);
        } catch (IOException e) {
            System.out.println("Помилка дешифрування: " + e.getMessage());
        }

        // №4: Частота тегів за URL
        System.out.println("\n№4: Частота тегів за URL");
        System.out.print("Введіть URL сторінки (наприклад, https://1234.com): ");
        String urlString = scanner.nextLine().trim();

        try {
            String html = downloadPage(urlString);
            Pattern tagPattern = Pattern.compile("<\\s*([a-zA-Z0-9]+)[^>]*>");
            Matcher matcher = tagPattern.matcher(html);
            while (matcher.find()) {
                String tag = matcher.group(1).toLowerCase();
                sessionResults.tagFrequencies.put(tag, sessionResults.tagFrequencies.getOrDefault(tag, 0) + 1);
            }

            // а) у лексикографічному порядку (TreeMap)
            System.out.println("\nА) Теги в лексикографічному порядку:");
            for (Map.Entry<String, Integer> entry : sessionResults.tagFrequencies.entrySet()) {
                System.out.println("<" + entry.getKey() + "> : " + entry.getValue());
            }

            // б) у порядку зростання частоти
            System.out.println("\nБ) Теги в порядку зростання частоти:");
            List<Map.Entry<String, Integer>> list = new ArrayList<>(sessionResults.tagFrequencies.entrySet());
            list.sort(Map.Entry.comparingByValue());
            for (Map.Entry<String, Integer> entry : list) {
                System.out.println("<" + entry.getKey() + "> : " + entry.getValue());
            }
        } catch (Exception e) {
            System.out.println("Помилка підключення або парсингу URL: " + e.getMessage());
        }

        System.out.println("\nПошук та серіалізація");

        System.out.print("Введіть назву тегу для пошуку його частоти (наприклад, div): ");
        String searchTag = scanner.nextLine().trim().toLowerCase();

        if (sessionResults.tagFrequencies.containsKey(searchTag)) {
            System.out.println("Знайдено! Тег <" + searchTag + "> зустрічається "
                    + sessionResults.tagFrequencies.get(searchTag) + " разів.");
        } else {
            System.out.println("Тег <" + searchTag + "> на сторінці не знайдено.");
        }

        String savePath = readNonEmptyLine(scanner,
                "\nВведіть шлях/ім'я файлу для збереження результатів сесії (наприклад, results.dat): ");
        fm.saveResults(savePath, sessionResults);

        System.out.print("Хочете перевірити завантаження збережених даних? (y/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            TaskResults loaded = fm.loadResults(savePath);
            if (loaded != null) {
                System.out.println("Все ок, кількість унікальних тегів у збереженому файлі: "
                        + loaded.tagFrequencies.size());
            }
        }

        scanner.close();
        System.out.println("\nРоботу завершено.");
    }

    private static char readKeyChar(Scanner scanner) {
        while (true) {
            System.out.print("Введіть ключовий символ (наприклад, 'A'): ");
            String input = scanner.nextLine();
            if (!input.isEmpty()) {
                return input.charAt(0);
            }
            System.out.println("Ключ не може бути порожнім, спробуйте ще раз.");
        }
    }

    private static String readNonEmptyLine(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("Значення не може бути порожнім, спробуйте ще раз.");
        }
    }

    private static int countWords(String line) {
        return line.trim().split("\\s+").length;
    }

    //завантаження всього тексту сторінки (щоб теги на кількох рядках теж рахувалися)
    private static String downloadPage(String urlString) throws IOException {
        URL url = URI.create(urlString).toURL();
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        StringBuilder html = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                html.append(inputLine).append('\n');
            }
        }
        return html.toString();
    }
}

// результати(серіалізація за замовчуванням)
class TaskResults implements Serializable {
    private static final long serialVersionUID = 1L;
    public String maxWordsLine = "";
    public Map<String, Integer> tagFrequencies = new TreeMap<>();
}

class FileManager {

    // завд.1
    public String findMaxWordsLine(String filePath) throws IOException {
        String result = "";
        int maxWords = 0;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                int wordCount = line.trim().split("\\s+").length;
                if (wordCount > maxWords) {
                    maxWords = wordCount;
                    result = line;
                }
            }
        }
        return result;
    }

    // завд.3: запис зашифрованого тексту через FilterWriter
    public void writeEncrypted(String filePath, String text, char key) throws IOException {
        try (CryptoWriter cw = new CryptoWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8), key)) {
            cw.write(text);
        }
    }

    // завд.3: читання та розшифрування через FilterReader
    public String readDecrypted(String filePath, char key) throws IOException {
        StringBuilder decrypted = new StringBuilder();
        try (CryptoReader cr = new CryptoReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8), key)) {
            int c;
            while ((c = cr.read()) != -1) {
                decrypted.append((char) c);
            }
        }
        return decrypted.toString();
    }

    public void saveResults(String filePath, TaskResults results) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(results);
            System.out.println("Дані успішно серіалізовано та збережено у файл: " + filePath);
        } catch (IOException e) {
            System.out.println("Помилка збереження файлу: " + e.getMessage());
        }
    }

    public TaskResults loadResults(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            TaskResults results = (TaskResults) ois.readObject();
            System.out.println("Дані успішно завантажено з файлу: " + filePath);
            return results;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Помилка читання файлу: " + e.getMessage());
            return null;
        }
    }
}

// завд. 3: шифрування (код символу збільшується на код ключового символу)
class CryptoWriter extends FilterWriter {
    private final char key;

    public CryptoWriter(Writer out, char key) {
        super(out);
        this.key = key;
    }

    // Метод шифрування
    public char encrypt(char c) {
        return (char) (c + key);
    }

    @Override
    public void write(int c) throws IOException {
        super.write(encrypt((char) c));
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        for (int i = off; i < off + len; i++) {
            write(cbuf[i]);
        }
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        for (int i = off; i < off + len; i++) {
            write(str.charAt(i));
        }
    }
}

// завд. 3: дешефрування
class CryptoReader extends FilterReader {
    private final char key;

    public CryptoReader(Reader in, char key) {
        super(in);
        this.key = key;
    }

    // метод дешифрування
    public char decrypt(char c) {
        return (char) (c - key);
    }

    @Override
    public int read() throws IOException {
        int c = super.read();
        return (c == -1) ? -1 : decrypt((char) c);
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int readChars = super.read(cbuf, off, len);
        if (readChars == -1) return -1;
        for (int i = off; i < off + readChars; i++) {
            cbuf[i] = decrypt(cbuf[i]);
        }
        return readChars;
    }
}