import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainGUI {

    // MAP 1: Stores books by OCLC Number
    private static Map<String, LibraryBookRecord> bookCatalogueMap = new HashMap<>();

    // MAP 2: Stores books by Genre
    private static Map<String, ArrayList<LibraryBookRecord>> genreMap = new HashMap<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            JFrame frame = new JFrame("Library Catalogue Parser & Search");
            frame.setSize(1000, 700); // Made it a tiny bit wider to fit the new button
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            JPanel topControls = new JPanel(new GridLayout(2, 1));
            JPanel row1 = new JPanel();
            JPanel row2 = new JPanel();

            // --- Row 1 Components ---
            JButton loadButton = new JButton("Load File");
            JButton showAllButton = new JButton("Show All Books"); // NEW BUTTON
            JLabel oclcSearchLabel = new JLabel("  Search OCLC:");
            JTextField oclcSearchField = new JTextField(12);
            JButton oclcSearchButton = new JButton("Search Book");

            row1.add(loadButton);
            row1.add(showAllButton); // ADDED TO GUI
            row1.add(oclcSearchLabel);
            row1.add(oclcSearchField);
            row1.add(oclcSearchButton);

            // --- Row 2 Components ---
            JLabel genreSearchLabel = new JLabel("Search Genre (e.g., History):");
            JTextField genreSearchField = new JTextField(15);
            JButton genreSearchButton = new JButton("Search Genre");

            row2.add(genreSearchLabel);
            row2.add(genreSearchField);
            row2.add(genreSearchButton);

            topControls.add(row1);
            topControls.add(row2);

            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);

            frame.add(topControls, BorderLayout.NORTH);
            frame.add(scrollPane, BorderLayout.CENTER);
            frame.setVisible(true);

            // --- ACTION LISTENERS ---

            // 1. LOAD BUTTON
            loadButton.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser("C:\\Users\\30066458\\Downloads");
                int response = fileChooser.showOpenDialog(null);
                if (response == JFileChooser.APPROVE_OPTION) {
                    parseBookCatalogue(fileChooser.getSelectedFile(), textArea);
                }
            });

            // 2. SHOW ALL BOOKS BUTTON (The "Back to Menu" feature)
            showAllButton.addActionListener(e -> {
                if (bookCatalogueMap.isEmpty()) {
                    textArea.setText("The catalogue is empty! Please load a file first.");
                } else {
                    // Calls our new helper method to reprint the menu
                    displayDirectory(textArea);
                }
            });

            // 3. OCLC SEARCH BUTTON
            oclcSearchButton.addActionListener(e -> {
                String searchKey = oclcSearchField.getText().trim();
                if (searchKey.isEmpty()) {
                    textArea.setText("Please enter an OCLC number to search.");
                    return;
                }

                textArea.setText("--- OCLC SEARCH RESULTS ---\n\n");
                if (bookCatalogueMap.containsKey(searchKey)) {
                    textArea.append(bookCatalogueMap.get(searchKey).toString());
                } else {
                    textArea.append("❌ Book with OCLC '" + searchKey + "' not found.");
                }
            });

            // 4. GENRE SEARCH BUTTON
            genreSearchButton.addActionListener(e -> {
                String searchGenre = genreSearchField.getText().trim();
                if (searchGenre.isEmpty()) {
                    textArea.setText("Please enter a genre to search (e.g., History, Art).");
                    return;
                }

                textArea.setText("--- GENRE SEARCH RESULTS: " + searchGenre.toUpperCase() + " ---\n\n");

                if (genreMap.containsKey(searchGenre)) {
                    ArrayList<LibraryBookRecord> genreBooks = genreMap.get(searchGenre);
                    textArea.append("Found " + genreBooks.size() + " book(s) in this genre:\n\n");
                    for (LibraryBookRecord book : genreBooks) {
                        textArea.append(book.toString() + "\n");
                    }
                } else {
                    textArea.append("❌ No books found for the genre: '" + searchGenre + "'.\n");
                }
            });
        });
    }

    public static void parseBookCatalogue(File file, JTextArea textArea) {
        boolean isNextLineTheOCLCNumber = false;
        boolean isNextLineTheTitle = false;
        boolean isNextLineTheAuthors = false;
        boolean isNextLineTheSummary = false;
        boolean isNextLineThePublicationYear = false;
        boolean isNextLineTheGenre = false;

        LibraryBookRecord currentRecord = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("---")) {
                    continue;
                }

                if (isNextLineTheOCLCNumber) {
                    currentRecord.setOclcNumber(line);
                    isNextLineTheOCLCNumber = false;
                    continue;
                }
                if (line.startsWith("OCLC Number:")) {
                    if (currentRecord != null && currentRecord.getOclcNumber() != null) {
                        saveRecordToMaps(currentRecord);
                    }
                    currentRecord = new LibraryBookRecord();
                    isNextLineTheOCLCNumber = true;
                    continue;
                }

                if (isNextLineTheTitle) {
                    currentRecord.setTitle(line);
                    isNextLineTheTitle = false;
                    continue;
                }
                if (line.startsWith("Title:")) {
                    isNextLineTheTitle = true;
                    continue;
                }

                if (isNextLineTheAuthors) {
                    currentRecord.setAuthors(line);
                    isNextLineTheAuthors = false;
                    continue;
                }
                if (line.startsWith("Author:") || line.startsWith("Authors:")) {
                    isNextLineTheAuthors = true;
                    continue;
                }

                if (isNextLineTheSummary) {
                    currentRecord.setSummary(line);
                    isNextLineTheSummary = false;
                    continue;
                }
                if (line.startsWith("Summary:")) {
                    isNextLineTheSummary = true;
                    continue;
                }

                if (isNextLineThePublicationYear) {
                    try {
                        currentRecord.setPublicationYear(Integer.parseInt(line));
                    } catch (NumberFormatException e) {
                        System.out.println("Error formatting year: " + line);
                    }
                    isNextLineThePublicationYear = false;
                    continue;
                }
                if (line.startsWith("Year of publication:") || line.startsWith("Publication Year:")) {
                    isNextLineThePublicationYear = true;
                    continue;
                }

                if (isNextLineTheGenre) {
                    currentRecord.setGenre(line);
                    isNextLineTheGenre = false;
                    continue;
                }
                if (line.startsWith("Genre:")) {
                    isNextLineTheGenre = true;
                    continue;
                }
            }

            if (currentRecord != null && currentRecord.getOclcNumber() != null) {
                saveRecordToMaps(currentRecord);
            }

            // Once parsing is done, call our helper method to show the menu!
            displayDirectory(textArea);

        } catch (IOException e) {
            textArea.setText("Error reading file: " + e.getMessage() + "\n");
        }
    }

    // --- HELPER METHODS ---

    // 1. Helper to save to the maps
    private static void saveRecordToMaps(LibraryBookRecord record) {
        bookCatalogueMap.put(record.getOclcNumber(), record);

        if (record.getGenre() != null && !record.getGenre().isEmpty()) {
            String bookGenre = record.getGenre();
            if (!genreMap.containsKey(bookGenre)) {
                genreMap.put(bookGenre, new ArrayList<>());
            }
            genreMap.get(bookGenre).add(record);
        }
    }

    // 2. NEW: Helper to print the Directory Menu
    private static void displayDirectory(JTextArea textArea) {
        // We use .setText() to clear the screen of any old search results
        textArea.setText("Total unique books in Catalogue: " + bookCatalogueMap.size() + "\n\n");
        textArea.append("AVAILABLE BOOKS DIRECTORY:\n");
        textArea.append("--------------------------------------------------\n");

        for (LibraryBookRecord book : bookCatalogueMap.values()) {
            textArea.append("OCLC: " + book.getOclcNumber() + "  |  Title: " + book.getTitle() + "\n");
        }

        textArea.append("--------------------------------------------------\n");
        textArea.append("Tip: Copy an OCLC number or a Genre (like 'History') to use the search boxes above!\n\n");
    }
}
