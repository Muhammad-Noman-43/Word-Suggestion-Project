import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;

import static javax.swing.JOptionPane.*;

public class GUI extends JFrame {
    
    File wordsFile = new File("final_merged_frequencies.txt");
    Path path = Paths.get(wordsFile.getPath());
    
    String prefix = new String("");
    private JTextArea textArea;
    private JTabbedPane tabbedPane;
    Trie trie;
    JButton openBtn, saveBtn;
    private JPopupMenu suggestionPopup;
    private JPopupMenu actionPopup;
    private StackUsingArray<Operation> undoStack = new StackUsingArray<>();
    private StackUsingArray<Operation> redoStack = new StackUsingArray<>();
    
    JFileChooser fileChooser;
    
    private JList<String> suggestionList;
    private DefaultListModel<String> listModel;
    JPanel homePanel;
    
    
    void setTrie(Trie t){
        trie = t;
    }
    
    public GUI(Trie t) {
        trie = t;
        // Set up the main frame
        setTitle("Muhammad's Editor");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));
        setLayout(new BorderLayout(20,10));
        
        // Create main content panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Create text area with padding
        textArea = new JTextArea();
        textArea.setFont(new Font("Times New Roman", Font.PLAIN, 16));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        createSuggestions();
        
        // Add padding to text area using a border
        textArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        add(createHomePanel(), BorderLayout.NORTH);
        
        // Add scroll pane for text area
        JScrollPane scrollPane = new JScrollPane(textArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add main panel to frame
        add(mainPanel, BorderLayout.CENTER);
        
        createActionPopup();
        addTextSelectionListener();
        
        pack();
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e){
                confirmationDialogue();
            }
        });
    }
    
    private void confirmationDialogue() {
        
        if(textArea.getText().trim().isEmpty())
            System.exit(0);
        
        int result = JOptionPane.showOptionDialog(this, "Do you want to save your work?", "Confirmation Box",
                YES_NO_CANCEL_OPTION, WARNING_MESSAGE, null, new String[]{"Save", "Don't Save", "Cancel"}, null);
        
        if(result == YES_OPTION){
            if(saveTextFile() == JFileChooser.APPROVE_OPTION){
                System.exit(0);
            }
        } else if (result == NO_OPTION){
            System.exit(0);
        }
    }
    
    private JPanel createHomePanel() {
        homePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        homePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Open File Button
        openBtn = new JButton("Open File");
        openBtn.setPreferredSize(new Dimension(100, 30));
        openBtn.setFocusable(false);
        
        // Save file button
        saveBtn = new JButton("Save File");
        saveBtn.setPreferredSize(new Dimension(100, 30));
        saveBtn.setFocusable(false);
        
        addActionListenerToButtons();
        return homePanel;
    }
    
    private void addActionListenerToButtons() {
        fileChooser = new JFileChooser("This PC");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
        openBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openTextFile();
            }
        });
        
        saveBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveTextFile();
            }
        });
        
        homePanel.add(openBtn);
        homePanel.add(saveBtn);
    }
    
    private void openTextFile(){
        if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            File choosenFile = fileChooser.getSelectedFile();
            
            try{
                String contentFromFile = new String(Files.readAllBytes(choosenFile.toPath()));
                textArea.removeAll();
                textArea.setText(contentFromFile);
            } catch(IOException e){
                throw new RuntimeException(e);
            }
        }
    }
    
    private int saveTextFile(){
        int result = fileChooser.showSaveDialog(this);
        if(result == JFileChooser.APPROVE_OPTION){
            File fileSavedAs = fileChooser.getSelectedFile();
            
            if (!fileSavedAs.getName().toLowerCase().endsWith(".txt")) {
                fileSavedAs = new File(fileSavedAs.getAbsolutePath() + ".txt");
            }
            
            try {
                String content = textArea.getText();
                Files.write(fileSavedAs.toPath(), content.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }
    
    void createSuggestions() {
        
        // List and Model
        listModel = new DefaultListModel<>();
        suggestionList = new JList<>(listModel);
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        
        // Popup menu
        suggestionPopup = new JPopupMenu();
        suggestionPopup.setFocusable(false);
        JScrollPane scrollPane = new JScrollPane(suggestionList);
        scrollPane.setPreferredSize(new Dimension(80, 150));
        suggestionPopup.add(scrollPane);
        
        // Key listener for typing in the text area
        textArea.addKeyListener(new KeyAdapter() {
            
            @Override
            public void keyPressed(KeyEvent e){
                
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z) {
                    if (!undoStack.isEmpty()) {
                        Operation previous = undoStack.pop();
                        redoStack.push(previous);
                        undoOperation(previous);
                    }
                } else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Y) {
                    if (!redoStack.isEmpty()) {
                        Operation next = redoStack.pop();
                        undoStack.push(next);
                        redoOperation(next);
                    }
                }  else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    int pos = textArea.getCaretPosition();
                    if (pos > 0) {
                        try {
                            char deletedChar = textArea.getText(pos - 1, 1).charAt(0);
                            undoStack.push(new Operation(deletedChar, false, pos - 1));
                            redoStack.clear();
                        } catch (Exception ignored) {}
                    }
                }
                
                if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_SPACE){
                    try {
                        prefix = setPrefix(e);
                    } catch (BadLocationException ex) {
                        throw new RuntimeException(ex);
                    }
                    
                    updatePopupSuggestions(prefix);
                }
                
                if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            suggestionPopup.setVisible(false);
                        }
                    });
                    
                    if(e.getKeyCode() == KeyEvent.VK_ENTER)
                        prefix = "";
                }
            }
            
            @Override
            public void keyTyped(KeyEvent e) {
                // Note that the keyPressed has a caret positioning + 1
                // It's because keyPressed is not synced with the UI and the components
                // If caret position was 2 and the key Ctrl+Z is pressed, the caret position would still be
                // 2 and not 1. But keyTyped is synced with the UI and components (i.e., their positioning)
                int pos = textArea.getCaretPosition();
                char typedChar = e.getKeyChar();
                if (!Character.isISOControl(typedChar)) {
                    undoStack.push(new Operation(typedChar, true, pos));
                    redoStack.clear();
                }
                try {
                    prefix = setPrefix(e);
                } catch (BadLocationException ex) {
                    throw new RuntimeException(ex);
                }
                updatePopupSuggestions(prefix);
            }
            
        });
        
//         Handle list item selection with mouse
        suggestionList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 1) {
                    String selected = suggestionList.getSelectedValue();
                    if (selected != null) {
                        insertSuggestedWord(selected);
                    }
                }
            }
        });
    }
    
    private void undoOperation(Operation op){
        int pos = op.posOfcursor;
        char ch = op.ch;
        boolean wasInserted = op.wasInserted;
        
        if(wasInserted){
            textArea.replaceRange("", pos, pos + 1);
        } else {
            textArea.insert(ch + "", pos);
        }
    }
    
    private void redoOperation(Operation op){
        int pos = op.posOfcursor;
        char ch = op.ch;
        boolean wasInserted = op.wasInserted;
        
        if(wasInserted){
            textArea.insert(ch + "", pos);
        } else {
            textArea.replaceRange("", pos, pos + 1);
        }
    }
    
    private void createActionPopup() {
        actionPopup = new JPopupMenu();
        JMenuItem addItem = new JMenuItem("Add to Trie");
        JMenuItem deleteItem = new JMenuItem("Delete from Trie");
        JMenuItem copyItem = new JMenuItem("Copy Text");
        JMenuItem cutItem = new JMenuItem("Cut text");
        JMenuItem pasteItem = new JMenuItem("Paste text");
        
        addItem.addActionListener(e -> {
            String selectedText = textArea.getSelectedText();
            if (selectedText != null && !selectedText.isBlank()) {
                if(!trie.search(selectedText.trim().toLowerCase())){
                    trie.insert(selectedText.trim().toLowerCase(), 12000);
                    String content = selectedText + "\t" + "12000\n";
                    try {
                        Files.write(path, content.getBytes(), StandardOpenOption.APPEND);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    showMessageDialog(this, "Word added to Trie: " + selectedText);
                } else {
                    showMessageDialog(this, "Word already in Trie.");
                }
            }
        });
        
        deleteItem.addActionListener(e -> {
            String selectedText = textArea.getSelectedText();
            if (selectedText != null && trie.search(selectedText.trim().toLowerCase())) {
                trie.delete(selectedText.trim().toLowerCase());
                showMessageDialog(this, "Word deleted from Trie: " + selectedText);
            } else {
                showMessageDialog(this, "Word not found in Trie.");
            }
        });
        
        actionPopup.add(copyItem);
        actionPopup.add(cutItem);
        actionPopup.add(pasteItem);
        actionPopup.add(addItem);
        actionPopup.add(deleteItem);
    }
    
    private void addTextSelectionListener() {
        textArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if(SwingUtilities.isRightMouseButton(e)){
                    if (textArea.getSelectedText() != null && !textArea.getSelectedText().isBlank()) {
                        try {
                            Rectangle viewRect = textArea.modelToView(textArea.getSelectionStart());
                            if (viewRect != null) {
                                actionPopup.show(textArea, viewRect.x, viewRect.y - 80);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
    }
    
    
    private String setPrefix(KeyEvent e) throws BadLocationException {
        int caret = textArea.getCaretPosition();
        String textBeforeCaret = textArea.getText(0, caret);
        textBeforeCaret += e.getKeyChar();
        String[] parts = textBeforeCaret.split("\\s+");
        
        if(parts.length == 0 || (parts.length == 1 && parts[0].equals(""))){
            return "";
        }
        
        String prefixToChangeTo = parts[parts.length-1];
        StringBuilder cleaned = new StringBuilder();
        for (char c : prefixToChangeTo.toCharArray()) {
            if (Character.isLetter(c)) {
                cleaned.append(c);
            }
        }
        return cleaned.toString();
    }
    
    private void updatePopupSuggestions(String prefix) {
        if (prefix.trim().isEmpty()) {
            suggestionPopup.setVisible(false);
            return;
        }
        
        List<Trie.trieNode> list = trie.getSuggestions(prefix);
        // Remember, to get the index of any node in LL, we (internally) use traverse method
        Collections.sort(list, (a, b) -> b.frequency - a.frequency);
        
//         Just the first 5 letters
        while(list.size() > 7){
            list.remove(list.size() - 1);
        }
        
        if (list.isEmpty()) {
            suggestionPopup.setVisible(false);
            return;
        }
        
        listModel.clear();
        for (Trie.trieNode node : list) {
            listModel.addElement(node.word);
        }
        
        try {
            Rectangle caretCoords = textArea.modelToView(textArea.getCaretPosition());
            suggestionPopup.show(textArea, caretCoords.x, caretCoords.y + 20);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void insertSuggestedWord(String word) {
        try {
            int pos = textArea.getCaretPosition();
            String text = textArea.getText(0, pos);
            
            // Remove the current prefix from text and insert suggestion
            String textBeforeCaret = textArea.getText(0, pos);
            int start = textBeforeCaret.lastIndexOf(" ") + 1;
            int end = pos;
            textArea.replaceRange(word, start, end);
            prefix = ""; // reset the prefix
            suggestionPopup.setVisible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
