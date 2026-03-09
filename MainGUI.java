import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MainGUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            // 1. Create the Frame
            JFrame frame = new JFrame("Library Catalogue Parser");
            frame.setSize(800, 600); // Adjusted to a standard window size
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // 2. Create Components
            JPanel panel = new JPanel();
            JTextArea textArea = new JTextArea(30, 60);
            JButton button = new JButton("Load File");
            JScrollPane scrollPane = new JScrollPane(textArea);

            // 3. Add Components
            panel.add(button);
            panel.add(scrollPane);
            frame.add(panel);
            frame.setVisible(true);

            // 4. Add the Action Listener
            button.addActionListener(e -> {
                System.out.println("YOU HAVE PRESSED THE LOAD BUTTON!");
                loadFile(textArea);
            });
        });
    }

    private static void loadFile(JTextArea textArea) {
        JFileChooser fileChooser = new JFileChooser("C:\\Users\\30066458\\Downloads");
        int response = fileChooser.showOpenDialog(null);

        if (response == JFileChooser.APPROVE_OPTION) {
            // Pass the selected file and the text area directly to our parsing method
            parseBookCatalogue(fileChooser.getSelectedFile(), textArea);
        }
    }

    public static void parseBookCatalogue(File file, JTextArea textArea) {
        // 1. Define Boolean flags for every attribute (set to false by default)
        boolean isNextLineTheOCLCNumber = false;
        boolean isNextLineTheTitle = false;
        boolean isNextLineTheAuthors = false;
        boolean isNextLineTheSummary = false;
        boolean isNextLineThePublicationYear = false;

        // 2. Declare an empty record object to hold our data
        LibraryBookRecord currentRecord = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            
            // Clear the text area before loading a new file
            textArea.setText("");

            // 3. Analyse the file, line by line
            while ((line = reader.readLine()) != null) {
                // Remove extra spaces to make String comparisons more accurate
                line = line.trim(); 

                // --- OCLC NUMBER ---
                if (isNextLineTheOCLCNumber) {
                    currentRecord.setOclcNumber(line);
                    isNextLineTheOCLCNumber = false; // Reset the flag
                    continue; // Skip to the next line of the file
                }
                if (line.equals("OCLC Number:")) {
                    // This string indicates the start of a new book record
                    currentRecord = new LibraryBookRecord(); 
                    isNextLineTheOCLCNumber = true;
                    continue;
                }

                // --- TITLE ---
                if (isNextLineTheTitle) {
                    currentRecord.setTitle(line);
                    isNextLineTheTitle = false;
                    continue;
                }
                if (line.equals("Title:")) {
                    isNextLineTheTitle = true;
                    continue;
                }

                // --- AUTHORS ---
                if (isNextLineTheAuthors) {
                    currentRecord.setAuthors(line);
                    isNextLineTheAuthors = false;
                    continue;
                }
                if (line.equals("Authors:")) {
                    isNextLineTheAuthors = true;
                    continue;
                }

                // --- SUMMARY ---
                if (isNextLineTheSummary) {
                    currentRecord.setSummary(line);
                    isNextLineTheSummary = false;
                    continue;
                }
                if (line.equals("Summary:")) {
                    isNextLineTheSummary = true;
                    continue;
                }

                // --- PUBLICATION YEAR ---
                if (isNextLineThePublicationYear) {
                    try {
                        // We must convert the String to an int for the year
                        currentRecord.setPublicationYear(Integer.parseInt(line));
                    } catch (NumberFormatException e) {
                        System.out.println("Error formatting year: " + line);
                    }
                    isNextLineThePublicationYear = false;

                    // Assuming Publication Year is the last piece of data for a book,
                    // our object is now complete! Let's print it to the GUI text area to test.
                    textArea.append(currentRecord.toString() + "\n\n");
                    continue;
                }
                if (line.equals("Publication Year:")) {
                    isNextLineThePublicationYear = true;
                    continue;
                }
            }
        } catch (IOException e) {
            textArea.setText("Error reading file: " + e.getMessage());
        }
    }
}
