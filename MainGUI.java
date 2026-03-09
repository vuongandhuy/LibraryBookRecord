import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class MainGUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            // 1. Create the Frame
            JFrame frame = new JFrame("My Application");
            frame.setSize(2000, 3000);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // 2. Create Components
            JPanel panel = new JPanel();
            JTextArea textArea = new JTextArea(50, 30); // Give it size (rows, cols)
            JButton button = new JButton("Load File");
            JScrollPane scrollPane = new JScrollPane(textArea);

            // 3. Create the File Chooser
            // NOTE: Use double slashes "\\" for Windows paths


            // 4. Add Components
            panel.add(button);
            panel.add(scrollPane);
            frame.add(panel);
            frame.setVisible(true);

            // 5. Add the Action Listener (THE FIX)
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
            try (BufferedReader reader = new BufferedReader(new FileReader(fileChooser.getSelectedFile()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    textArea.append(line + "\n");
                }
            } catch (IOException e) {
                System.out.println("Error reading file");
            }
        }
    }

    public void parseBookCatalogue(String rawData) {
        //Set the variable to false by default
        boolean isNextLineTheOCLCNumber = false;
    }
}
