import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;

public class MainGUI {

    // core data structures to hold catalogue items and count genres (Tasks A & B)
    private static Map<String, LibraryItem> completeCatalogue = new HashMap<>();
    private static Map<String, Integer> bookGenreCounts = new HashMap<>();
    private static Map<String, Integer> dvdGenreCounts = new HashMap<>();
    private static Map<String, Integer> cdGenreCounts = new HashMap<>();

    // map for Task C to keep a running total of loan days for each OCLC
    private static Map<String, Integer> totalLoanDaysMap = new HashMap<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            // setup main application window
            JFrame frame = new JFrame("Library Catalogue Dashboard");
            frame.setSize(1100, 800);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            // setup layout manager for the top control panel
            JPanel topControls = new JPanel(new GridLayout(4, 1));
            JPanel row1 = new JPanel();
            JPanel row2 = new JPanel();
            JPanel row3 = new JPanel();
            JPanel row4 = new JPanel();
            JPanel row5 = new JPanel();
            JPanel row6 = new JPanel();

            // row 1: basic file loading and report generation
            JButton loadButton = new JButton("Load File");
            JButton showAllButton = new JButton("Show All Items");
            JButton displayReportBtn = new JButton("Display Report");
            JButton saveReportBtn = new JButton("Save Report");

            row1.add(loadButton);
            row1.add(showAllButton);
            row1.add(displayReportBtn);
            row1.add(saveReportBtn);

            // row 2: search functionality for specific items and genres
            JLabel oclcSearchLabel = new JLabel("Search OCLC:");
            JTextField oclcSearchField = new JTextField(10);
            JButton oclcSearchButton = new JButton("Search Item");

            JLabel genreSearchLabel = new JLabel("  Search Genre:");
            JTextField genreSearchField = new JTextField(12);
            JButton genreSearchButton = new JButton("Search Genre");

            row2.add(oclcSearchLabel);
            row2.add(oclcSearchField);
            row2.add(oclcSearchButton);
            row2.add(genreSearchLabel);
            row2.add(genreSearchField);
            row2.add(genreSearchButton);

            // row 3: radio buttons to filter genres by category
            JRadioButton bookRadio = new JRadioButton("Books");
            JRadioButton dvdRadio = new JRadioButton("DVDs");
            JRadioButton cdRadio = new JRadioButton("CDs");
            ButtonGroup categoryGroup = new ButtonGroup();
            categoryGroup.add(bookRadio);
            categoryGroup.add(dvdRadio);
            categoryGroup.add(cdRadio);
            bookRadio.setSelected(true); // default selection

            JButton showGenresBtn = new JButton("Show Genres");

            row3.add(new JLabel("Filter Genres by Category: "));
            row3.add(bookRadio);
            row3.add(dvdRadio);
            row3.add(cdRadio);
            row3.add(showGenresBtn);

            // row 4: loan record analysis buttons (Task C)
            JButton loadLoansBtn = new JButton("Load Loan Records");
            JButton topBooksBtn = new JButton("Top 10 Books");
            JButton topDvdBtn = new JButton("Top 10 DVDs");
            JButton topCdBtn = new JButton("Top 10 CDs");

            row4.add(loadLoansBtn);
            row4.add(new JLabel("   |   "));
            row4.add(topBooksBtn);
            row4.add(topDvdBtn);
            row4.add(topCdBtn);

            // row 5: Custom JSON Export buttons
            JButton btnJsonBooks = new JButton("Books to JSON");
            JButton btnJsonDVDs = new JButton("DVDs to JSON");
            JButton btnJsonCDs = new JButton("CDs to JSON");

            row5.add(new JLabel("Export to JSON: "));
            row5.add(btnJsonBooks);
            row5.add(btnJsonDVDs);
            row5.add(btnJsonCDs);

            // row 6: Custom XML Export buttons
            JButton btnGenerateXml = new JButton("Generate XML Hierarchy");
            row6.add(new JLabel("XML Classification: "));
            row6.add(btnGenerateXml);

            // add all rows to the main top panel
            topControls.add(row1);
            topControls.add(row2);
            topControls.add(row3);
            topControls.add(row4);
            topControls.add(row5);
            topControls.add(row6);

            // setup text area for displaying results
            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);

            frame.add(topControls, BorderLayout.NORTH);
            frame.add(scrollPane, BorderLayout.CENTER);
            frame.setVisible(true);

            // --- EVENT LISTENERS ---

            // XML Button Listener
            btnGenerateXml.addActionListener(e -> {
                if (completeCatalogue.isEmpty()) {
                    textArea.setText("The catalogue is empty! Please load the library files first.");
                } else {
                    displayXMLHierarchy(textArea);
                }
            });

            // json button listeners
            btnJsonBooks.addActionListener(e -> {
                textArea.setText("--- BOOKS JSON READY ---\n\n" + generateJSONForCategory("Book"));
            });

            btnJsonDVDs.addActionListener(e -> {
                textArea.setText("--- DVDs JSON READY ---\n\n" + generateJSONForCategory("DVD"));
            });

            btnJsonCDs.addActionListener(e -> {
                textArea.setText("--- CDs JSON READY ---\n\n" + generateJSONForCategory("CD"));
            });

            // handles opening files and routing to the correct parser
            loadButton.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser("."); // use current directory to avoid hardcoded paths
                int response = fileChooser.showOpenDialog(null);
                if (response == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    String fileName = selectedFile.getName();

                    if (fileName.contains("Book")) {
                        parseBookCatalogue(selectedFile, textArea);
                    } else if (fileName.contains("DVD")) {
                        parseDVDCatalogue(selectedFile, textArea);
                    } else if (fileName.contains("CD")) {
                        parseCDCatalogue(selectedFile, textArea);
                    } else {
                        textArea.setText("Please select a valid Book, DVD, or CD catalogue file.");
                    }
                }
            });

            // loads the loan records file (make sure catalogue is loaded first)
            loadLoansBtn.addActionListener(e -> {
                if (completeCatalogue.isEmpty()) {
                    textArea.setText("Please load the Book, DVD, and CD files BEFORE loading loan records!");
                    return;
                }
                JFileChooser fileChooser = new JFileChooser(".");
                int response = fileChooser.showOpenDialog(null);
                if (response == JFileChooser.APPROVE_OPTION) {
                    parseLoanRecords(fileChooser.getSelectedFile(), textArea);
                }
            });

            // trigger top 10 calculations
            topBooksBtn.addActionListener(e -> displayTop10("Book", textArea));
            topDvdBtn.addActionListener(e -> displayTop10("DVD", textArea));
            topCdBtn.addActionListener(e -> displayTop10("CD", textArea));

            // resets the display to show all loaded items
            showAllButton.addActionListener(e -> {
                if (completeCatalogue.isEmpty()) {
                    textArea.setText("The catalogue is empty! Please load a file first.");
                } else {
                    displayDirectory(textArea);
                }
            });

            // prints the stat report to the GUI
            displayReportBtn.addActionListener(e -> {
                if (completeCatalogue.isEmpty()) {
                    textArea.setText("The catalogue is empty! Please load the library files first.");
                } else {
                    textArea.setText(generateReportString());
                }
            });

            // saves the stat report to a local txt file
            saveReportBtn.addActionListener(e -> {
                if (completeCatalogue.isEmpty()) {
                    textArea.setText("The catalogue is empty! Please load the library files first.");
                    return;
                }
                try (FileWriter writer = new FileWriter("Library_Catalogue_Report.txt")) {
                    writer.write(generateReportString());
                    textArea.setText("SUCCESS! Report safely saved to 'Library_Catalogue_Report.txt'\n");
                } catch (IOException ex) {
                    textArea.setText("Error saving file: " + ex.getMessage());
                }
            });

            // search for specific item by key
            oclcSearchButton.addActionListener(e -> {
                String searchKey = oclcSearchField.getText().trim();
                if (searchKey.isEmpty()) {
                    textArea.setText("Please enter an OCLC number to search.");
                    return;
                }
                textArea.setText("--- OCLC SEARCH RESULTS ---\n\n");
                if (completeCatalogue.containsKey(searchKey)) {
                    textArea.append(completeCatalogue.get(searchKey).toString());
                } else {
                    textArea.append("Record with OCLC '" + searchKey + "' not found.");
                }
            });

            // loop through catalogue and print titles matching the search genre
            genreSearchButton.addActionListener(e -> {
                String searchGenre = genreSearchField.getText().trim();
                if (searchGenre.isEmpty()) {
                    textArea.setText("Please enter a genre to search (e.g., History, Art).");
                    return;
                }
                textArea.setText("--- GENRE SEARCH RESULTS: " + searchGenre.toUpperCase() + " ---\n\n");
                boolean found = false;
                for (LibraryItem item : completeCatalogue.values()) {
                    if (item.getGenre() != null && item.getGenre().equalsIgnoreCase(searchGenre)) {
                        textArea.append("- " + item.getTitle() + "\n");
                        found = true;
                    }
                }
                if (!found) textArea.append("No records found for the genre: '" + searchGenre + "'.\n");
            });

            // check which radio button is active and list its genres
            showGenresBtn.addActionListener(e -> {
                textArea.setText("--- AVAILABLE GENRES ---\n\n");
                if (bookRadio.isSelected()) {
                    textArea.append("Category: BOOKS\n");
                    for (String genre : bookGenreCounts.keySet()) textArea.append("- " + genre + "\n");
                } else if (dvdRadio.isSelected()) {
                    textArea.append("Category: DVDs\n");
                    for (String genre : dvdGenreCounts.keySet()) textArea.append("- " + genre + "\n");
                } else if (cdRadio.isSelected()) {
                    textArea.append("Category: CDs\n");
                    for (String genre : cdGenreCounts.keySet()) textArea.append("- " + genre + "\n");
                }
            });
        });
    }

    // parses the loan records and totals up days for items with multiple copies
    public static void parseLoanRecords(File file, JTextArea textArea) {
        boolean isNextLineOCLC = false;
        boolean isNextLineDays = false;
        String currentOclc = null;
        int currentDays = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("---")) continue;

                if (isNextLineOCLC) {
                    currentOclc = line;
                    isNextLineOCLC = false;
                    continue;
                }
                if (line.startsWith("OCLC Number:")) {
                    // Before starting a new record, save the previous one to the Map!
                    if (currentOclc != null && currentDays > 0) {
                        totalLoanDaysMap.put(currentOclc, totalLoanDaysMap.getOrDefault(currentOclc, 0) + currentDays);
                    }
                    // Reset variables for the new record
                    currentOclc = null;
                    currentDays = 0;
                    isNextLineOCLC = true;
                    continue;
                }

                if (isNextLineDays) {
                    try {
                        currentDays = Integer.parseInt(line);
                    } catch (NumberFormatException e) {
                    }
                    isNextLineDays = false;
                    continue;
                }
                if (line.startsWith("ON LOAN:")) {
                    isNextLineDays = true;
                    continue;
                }
            }

            // Catch and save the very last record in the text file
            if (currentOclc != null && currentDays > 0) {
                totalLoanDaysMap.put(currentOclc, totalLoanDaysMap.getOrDefault(currentOclc, 0) + currentDays);
            }

            textArea.setText("SUCCESS! Loan records parsed and added to memory.\n");
            textArea.append("You can now use the 'Top 10' buttons.");
        } catch (IOException e) {
            textArea.setText("Error reading loan file: " + e.getMessage());
        }
    }

    // reads book file and creates book objects
    public static void parseBookCatalogue(File file, JTextArea textArea) {
        boolean isNextLineOCLC = false, isNextLineTitle = false, isNextLineAuthors = false;
        boolean isNextLineSummary = false, isNextLineYear = false, isNextLineGenre = false;
        boolean isNextLinePublisher = false, isNextLinePhysDesc = false, isNextLineIsbn = false;

        Book currentRecord = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("---")) continue;

                if (isNextLineOCLC) {
                    currentRecord.setOclcNumber(line);
                    isNextLineOCLC = false;
                    continue;
                }
                if (line.startsWith("OCLC Number:")) {
                    if (currentRecord != null && currentRecord.getOclcNumber() != null) saveRecordToMaps(currentRecord);
                    currentRecord = new Book();
                    isNextLineOCLC = true;
                    continue;
                }

                if (isNextLineTitle) {
                    currentRecord.setTitle(line);
                    isNextLineTitle = false;
                    continue;
                }
                if (line.startsWith("Title:")) {
                    isNextLineTitle = true;
                    continue;
                }

                if (isNextLineAuthors) {
                    currentRecord.setAuthors(line);
                    isNextLineAuthors = false;
                    continue;
                }
                if (line.startsWith("Author:") || line.startsWith("Authors:")) {
                    isNextLineAuthors = true;
                    continue;
                }

                if (isNextLineSummary) {
                    currentRecord.setSummary(line);
                    isNextLineSummary = false;
                    continue;
                }
                if (line.startsWith("Summary:")) {
                    isNextLineSummary = true;
                    continue;
                }

                if (isNextLineYear) {
                    try {
                        currentRecord.setYear(Integer.parseInt(line));
                    } catch (NumberFormatException e) {
                    }
                    isNextLineYear = false;
                    continue;
                }
                if (line.startsWith("Year of publication:") || line.startsWith("Publication Year:")) {
                    isNextLineYear = true;
                    continue;
                }

                if (isNextLinePublisher) {
                    currentRecord.setPublisher(line);
                    isNextLinePublisher = false;
                    continue;
                }
                if (line.startsWith("Publisher:")) {
                    isNextLinePublisher = true;
                    continue;
                }

                if (isNextLineGenre) {
                    currentRecord.setGenre(line);
                    isNextLineGenre = false;
                    continue;
                }
                if (line.startsWith("Genre:")) {
                    isNextLineGenre = true;
                    continue;
                }

                if (isNextLinePhysDesc) {
                    currentRecord.setPhysicalDescription(line);
                    isNextLinePhysDesc = false;
                    continue;
                }
                if (line.startsWith("Physical Description:")) {
                    isNextLinePhysDesc = true;
                    continue;
                }

                if (isNextLineIsbn) {
                    currentRecord.setIsbn(line);
                    isNextLineIsbn = false;
                    continue;
                }
                if (line.startsWith("ISBN:")) {
                    isNextLineIsbn = true;
                    continue;
                }
            }
            // save the last record in the file
            if (currentRecord != null && currentRecord.getOclcNumber() != null) saveRecordToMaps(currentRecord);
            displayDirectory(textArea);
        } catch (IOException e) {
            textArea.setText("Error reading file: " + e.getMessage() + "\n");
        }
    }

    // reads dvd file and creates dvd objects
    public static void parseDVDCatalogue(File file, JTextArea textArea) {
        boolean isNextLineOCLC = false, isNextLineTitle = false, isNextLineCast = false;
        boolean isNextLineCredits = false, isNextLinePlot = false, isNextLineYear = false;
        boolean isNextLineLanguage = false, isNextLinePublisher = false, isNextLineGenre = false;
        boolean isNextLinePhysDesc = false, isNextLineIsbn = false;

        DVD currentRecord = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("---")) continue;

                if (isNextLineOCLC) {
                    currentRecord.setOclcNumber(line);
                    isNextLineOCLC = false;
                    continue;
                }
                if (line.startsWith("OCLC Number:")) {
                    if (currentRecord != null && currentRecord.getOclcNumber() != null) saveRecordToMaps(currentRecord);
                    currentRecord = new DVD();
                    isNextLineOCLC = true;
                    continue;
                }

                if (isNextLineTitle) {
                    currentRecord.setTitle(line);
                    isNextLineTitle = false;
                    continue;
                }
                if (line.startsWith("Title:")) {
                    isNextLineTitle = true;
                    continue;
                }

                if (isNextLineCast) {
                    currentRecord.setCast(line);
                    isNextLineCast = false;
                    continue;
                }
                if (line.startsWith("Cast:")) {
                    isNextLineCast = true;
                    continue;
                }

                if (isNextLineCredits) {
                    currentRecord.setCredits(line);
                    isNextLineCredits = false;
                    continue;
                }
                if (line.startsWith("Credits:")) {
                    isNextLineCredits = true;
                    continue;
                }

                if (isNextLinePlot) {
                    currentRecord.setPlot(line);
                    isNextLinePlot = false;
                    continue;
                }
                if (line.startsWith("Plot:")) {
                    isNextLinePlot = true;
                    continue;
                }

                if (isNextLineYear) {
                    try {
                        currentRecord.setYear(Integer.parseInt(line));
                    } catch (NumberFormatException e) {
                    }
                    isNextLineYear = false;
                    continue;
                }
                if (line.startsWith("Year of release:")) {
                    isNextLineYear = true;
                    continue;
                }

                if (isNextLineLanguage) {
                    currentRecord.setLanguage(line);
                    isNextLineLanguage = false;
                    continue;
                }
                if (line.startsWith("Language:")) {
                    isNextLineLanguage = true;
                    continue;
                }

                if (isNextLinePublisher) {
                    currentRecord.setPublisher(line);
                    isNextLinePublisher = false;
                    continue;
                }
                if (line.startsWith("Publisher:")) {
                    isNextLinePublisher = true;
                    continue;
                }

                if (isNextLineGenre) {
                    currentRecord.setGenre(line);
                    isNextLineGenre = false;
                    continue;
                }
                if (line.startsWith("Genre:")) {
                    isNextLineGenre = true;
                    continue;
                }

                if (isNextLinePhysDesc) {
                    currentRecord.setPhysicalDescription(line);
                    isNextLinePhysDesc = false;
                    continue;
                }
                if (line.startsWith("Physical Description:")) {
                    isNextLinePhysDesc = true;
                    continue;
                }

                if (isNextLineIsbn) {
                    currentRecord.setIsbn(line);
                    isNextLineIsbn = false;
                    continue;
                }
                if (line.startsWith("ISBN:")) {
                    isNextLineIsbn = true;
                    continue;
                }
            }
            if (currentRecord != null && currentRecord.getOclcNumber() != null) saveRecordToMaps(currentRecord);
            displayDirectory(textArea);
        } catch (IOException e) {
            textArea.setText("Error reading file: " + e.getMessage() + "\n");
        }
    }

    // reads cd file and creates cd objects
    public static void parseCDCatalogue(File file, JTextArea textArea) {
        boolean isNextLineOCLC = false, isNextLineTitle = false, isNextLinePerformers = false;
        boolean isNextLineCredits = false, isNextLineDescription = false, isNextLineYear = false;
        boolean isNextLineLanguage = false, isNextLinePublisher = false, isNextLineGenre = false;
        boolean isNextLinePhysDesc = false, isNextLineIsbn = false;

        CD currentRecord = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("---")) continue;

                if (isNextLineOCLC) {
                    currentRecord.setOclcNumber(line);
                    isNextLineOCLC = false;
                    continue;
                }
                if (line.startsWith("OCLC Number:")) {
                    if (currentRecord != null && currentRecord.getOclcNumber() != null) saveRecordToMaps(currentRecord);
                    currentRecord = new CD();
                    isNextLineOCLC = true;
                    continue;
                }

                if (isNextLineTitle) {
                    currentRecord.setTitle(line);
                    isNextLineTitle = false;
                    continue;
                }
                if (line.startsWith("Title:")) {
                    isNextLineTitle = true;
                    continue;
                }

                if (isNextLinePerformers) {
                    currentRecord.setPerformers(line);
                    isNextLinePerformers = false;
                    continue;
                }
                if (line.startsWith("Performers:")) {
                    isNextLinePerformers = true;
                    continue;
                }

                if (isNextLineCredits) {
                    currentRecord.setCredits(line);
                    isNextLineCredits = false;
                    continue;
                }
                if (line.startsWith("Credits:")) {
                    isNextLineCredits = true;
                    continue;
                }

                if (isNextLineDescription) {
                    currentRecord.setDescription(line);
                    isNextLineDescription = false;
                    continue;
                }
                if (line.startsWith("Description:")) {
                    isNextLineDescription = true;
                    continue;
                }

                if (isNextLineYear) {
                    try {
                        currentRecord.setYear(Integer.parseInt(line));
                    } catch (NumberFormatException e) {
                    }
                    isNextLineYear = false;
                    continue;
                }
                if (line.startsWith("Year of release:")) {
                    isNextLineYear = true;
                    continue;
                }

                if (isNextLineLanguage) {
                    currentRecord.setLanguage(line);
                    isNextLineLanguage = false;
                    continue;
                }
                if (line.startsWith("Language:")) {
                    isNextLineLanguage = true;
                    continue;
                }

                if (isNextLinePublisher) {
                    currentRecord.setPublisher(line);
                    isNextLinePublisher = false;
                    continue;
                }
                if (line.startsWith("Publisher:")) {
                    isNextLinePublisher = true;
                    continue;
                }

                if (isNextLineGenre) {
                    currentRecord.setGenre(line);
                    isNextLineGenre = false;
                    continue;
                }
                if (line.startsWith("Genre:")) {
                    isNextLineGenre = true;
                    continue;
                }

                if (isNextLinePhysDesc) {
                    currentRecord.setPhysicalDescription(line);
                    isNextLinePhysDesc = false;
                    continue;
                }
                if (line.startsWith("Physical Description:")) {
                    isNextLinePhysDesc = true;
                    continue;
                }

                if (isNextLineIsbn) {
                    currentRecord.setIsbn(line);
                    isNextLineIsbn = false;
                    continue;
                }
                if (line.startsWith("ISBN:")) {
                    isNextLineIsbn = true;
                    continue;
                }
            }
            if (currentRecord != null && currentRecord.getOclcNumber() != null) saveRecordToMaps(currentRecord);
            displayDirectory(textArea);
        } catch (IOException e) {
            textArea.setText("Error reading file: " + e.getMessage() + "\n");
        }
    }

    // stores object in main map and increments genre counters for reports
    private static void saveRecordToMaps(LibraryItem item) {
        completeCatalogue.put(item.getOclcNumber(), item);
        String genre = item.getGenre();
        if (genre == null || genre.isEmpty()) return;

        if (item instanceof Book) bookGenreCounts.put(genre, bookGenreCounts.getOrDefault(genre, 0) + 1);
        else if (item instanceof DVD) dvdGenreCounts.put(genre, dvdGenreCounts.getOrDefault(genre, 0) + 1);
        else if (item instanceof CD) cdGenreCounts.put(genre, cdGenreCounts.getOrDefault(genre, 0) + 1);
    }

    // generic method to list everything currently loaded
    private static void displayDirectory(JTextArea textArea) {
        textArea.setText("Total unique items in Catalogue: " + completeCatalogue.size() + "\n\n");
        textArea.append("AVAILABLE DIRECTORY:\n--------------------------------------------------\n");
        for (LibraryItem item : completeCatalogue.values()) {
            textArea.append("OCLC: " + item.getOclcNumber() + "  |  Title: " + item.getTitle() + "\n");
        }
        textArea.append("--------------------------------------------------\n");
    }

    // calculates top 10 loans based on selected type
    private static void displayTop10(String type, JTextArea textArea) {
        if (totalLoanDaysMap.isEmpty()) {
            textArea.setText("Please load the Loan Records file first!");
            return;
        }

        // filter list to only contain the requested category
        ArrayList<Map.Entry<String, Integer>> validLoans = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : totalLoanDaysMap.entrySet()) {
            LibraryItem item = completeCatalogue.get(entry.getKey());
            if (item != null) {
                if (type.equals("Book") && item instanceof Book) validLoans.add(entry);
                else if (type.equals("DVD") && item instanceof DVD) validLoans.add(entry);
                else if (type.equals("CD") && item instanceof CD) validLoans.add(entry);
            }
        }

        // sort descending using comparator
        Collections.sort(validLoans, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
                return b.getValue().compareTo(a.getValue());
            }
        });

        textArea.setText("--- TOP 10 LONGEST LOANS: " + type.toUpperCase() + "S ---\n\n");

        int count = 0;
        for (Map.Entry<String, Integer> entry : validLoans) {
            if (count >= 10) break; // only print top 10

            int totalDays = entry.getValue();

            // convert days to weeks (rounding up using modulo trick)
            int weeks = totalDays / 7;
            if (totalDays % 7 > 0) {
                weeks = weeks + 1;
            }

            LibraryItem item = completeCatalogue.get(entry.getKey());
            textArea.append((count + 1) + ". " + item.getTitle() + "\n");
            textArea.append("   Total Time on Loan: " + weeks + " weeks (" + totalDays + " total days)\n\n");

            count++;
        }

        if (count == 0) {
            textArea.append("No loan records found for this category.");
        }
    }

    // builds the big stat report text
    private static String generateReportString() {
        int totalItems = completeCatalogue.size();

        // sum up totals from the genre maps
        int totalBooks = 0;
        for (int count : bookGenreCounts.values()) totalBooks += count;
        int totalDVDs = 0;
        for (int count : dvdGenreCounts.values()) totalDVDs += count;
        int totalCDs = 0;
        for (int count : cdGenreCounts.values()) totalCDs += count;

        StringBuilder report = new StringBuilder();
        report.append("=========================================\n");
        report.append("       LIBRARY CATALOGUE REPORT\n");
        report.append("=========================================\n\n");
        report.append("Total unique library items: ").append(totalItems).append("\n");
        report.append(" - Total Books: ").append(totalBooks).append("\n");
        report.append(" - Total DVDs: ").append(totalDVDs).append("\n");
        report.append(" - Total CDs: ").append(totalCDs).append("\n\n");

        report.append("--- GENRE STATISTICS ---\n\n");
        report.append("Unique Book Genres (").append(bookGenreCounts.size()).append(" total):\n");
        for (Map.Entry<String, Integer> entry : bookGenreCounts.entrySet())
            report.append("  * ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" item(s)\n");

        report.append("\nUnique DVD Genres (").append(dvdGenreCounts.size()).append(" total):\n");
        for (Map.Entry<String, Integer> entry : dvdGenreCounts.entrySet())
            report.append("  * ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" item(s)\n");

        report.append("\nUnique CD Genres (").append(cdGenreCounts.size()).append(" total):\n");
        for (Map.Entry<String, Integer> entry : cdGenreCounts.entrySet())
            report.append("  * ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" item(s)\n");

        return report.toString();
    }

    // Generates a JSON array containing all items that match the requested category
    private static String generateJSONForCategory(String category) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");

        List<LibraryItem> items = completeCatalogue.values().stream()
                .filter(item -> item.getClass().getSimpleName().equalsIgnoreCase(category))
                .collect(Collectors.toList());

        for (int i = 0; i < items.size(); i++) {
            sb.append("  ").append(items.get(i).toJSON().replace("\n", "\n  "));
            if (i < items.size() - 1) sb.append(",\n");
            else sb.append("\n");
        }
        sb.append("]");
        return sb.toString();
    }

    // Generate and display the XML hierarchy
    private static void displayXMLHierarchy(JTextArea textArea) {
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<LIBRARY_COLLECTION>\n");
        String[] types = {"Book", "DVD", "CD"};

        for (String type : types) {
            sb.append("  <").append(type.toUpperCase()).append("_COLLECTION>\n");

            // Group items by genre automatically using a Java Stream
            Map<String, List<LibraryItem>> grouped = completeCatalogue.values().stream()
                    .filter(item -> item.getClass().getSimpleName().equals(type))
                    .collect(Collectors.groupingBy(item -> item.getGenre() != null ? item.getGenre() : "Unknown"));

            // Loop through each genre group and print the records
            for (String genre : grouped.keySet()) {
                String safeGenre = genre.replace("&", "&amp;"); // clean XML characters
                sb.append("    <").append(type.toUpperCase()).append("_GENRE name=\"").append(safeGenre).append("\">\n");

                for (LibraryItem item : grouped.get(genre)) {
                    String safeTitle = item.getTitle().replace("&", "&amp;");
                    sb.append("      <Record>")
                            .append(safeTitle).append(" &amp; OCLC NUMBER: ")
                            .append(item.getOclcNumber()).append(" / ").append(safeGenre)
                            .append("</Record>\n");
                }
                sb.append("    </").append(type.toUpperCase()).append("_GENRE>\n");
            }
            sb.append("  </").append(type.toUpperCase()).append("_COLLECTION>\n");
        }
        sb.append("</LIBRARY_COLLECTION>");

        // Print it directly to the dashboard
        textArea.setText(sb.toString());
        textArea.setCaretPosition(0);
    }
}
