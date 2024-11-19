import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class TagExtractor extends JFrame {
    private JTextArea outputArea;
    private JFileChooser fileChooser;
    private JButton selectFileButton, selectStopWordsButton, saveOutputButton;
    private File textFile;
    private File stopWordsFile;
    private Map<String, Integer> wordFrequencyMap = new TreeMap<>();
    private Set<String> stopWords = new TreeSet<>();

    public TagExtractor() {
        setTitle("Tag Extractor");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top panel with buttons
        JPanel topPanel = new JPanel();
        selectFileButton = new JButton("Select Text File");
        selectStopWordsButton = new JButton("Select Stop Words File");
        saveOutputButton = new JButton("Save Output");
        topPanel.add(selectFileButton);
        topPanel.add(selectStopWordsButton);
        topPanel.add(saveOutputButton);

        // Output area with scroll
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Button actions
        selectFileButton.addActionListener(new FileSelectorActionListener());
        selectStopWordsButton.addActionListener(new StopWordsSelectorActionListener());
        saveOutputButton.addActionListener(new SaveOutputActionListener());
    }

    private void loadStopWords(File file) throws IOException {
        stopWords.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stopWords.add(line.trim().toLowerCase());
            }
        }
    }

    private void processFile(File file) throws IOException {
        wordFrequencyMap.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.replaceAll("[^a-zA-Z]", " ").toLowerCase().split("\\s+");
                for (String word : words) {
                    if (!word.trim().isEmpty() && !stopWords.contains(word)) {
                        wordFrequencyMap.put(word, wordFrequencyMap.getOrDefault(word, 0) + 1);
                    }
                }
            }
        }
    }

    private void displayResults() {
        outputArea.setText("");
        for (Entry<String, Integer> entry : wordFrequencyMap.entrySet()) {
            outputArea.append(entry.getKey() + ": " + entry.getValue() + "\n");
        }
    }

    private class FileSelectorActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(TagExtractor.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                textFile = fileChooser.getSelectedFile();
                try {
                    if (stopWordsFile != null) {
                        processFile(textFile);
                        displayResults();
                    } else {
                        JOptionPane.showMessageDialog(TagExtractor.this,
                                "Please select a stop words file first.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(TagExtractor.this,
                            "Error reading file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private class StopWordsSelectorActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(TagExtractor.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                stopWordsFile = fileChooser.getSelectedFile();
                try {
                    loadStopWords(stopWordsFile);
                    JOptionPane.showMessageDialog(TagExtractor.this,
                            "Stop words loaded successfully.", "Info", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(TagExtractor.this,
                            "Error reading stop words file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private class SaveOutputActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (wordFrequencyMap.isEmpty()) {
                JOptionPane.showMessageDialog(TagExtractor.this,
                        "No tags to save. Please select and process a text file first.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            fileChooser = new JFileChooser();
            int result = fileChooser.showSaveDialog(TagExtractor.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File saveFile = fileChooser.getSelectedFile();
                try (PrintWriter writer = new PrintWriter(new FileWriter(saveFile))) {
                    for (Entry<String, Integer> entry : wordFrequencyMap.entrySet()) {
                        writer.println(entry.getKey() + ": " + entry.getValue());
                    }
                    JOptionPane.showMessageDialog(TagExtractor.this,
                            "Tags saved successfully.", "Info", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(TagExtractor.this,
                            "Error saving file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TagExtractor extractor = new TagExtractor();
            extractor.setVisible(true);
        });
    }
}