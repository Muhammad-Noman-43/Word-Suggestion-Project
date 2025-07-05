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
    String copiedOrCutText;
    private JTextArea textArea;
    Trie trie;
    private JPopupMenu suggestionPopup;
    private JPopupMenu actionPopupTextSelected;
    private JPopupMenu actionPopupTextNotSelected;
    private StackUsingArray<Operation> undoStack = new StackUsingArray<>();
    private StackUsingArray<Operation> redoStack = new StackUsingArray<>();
    
    JFileChooser fileChooser;
    
    private JList<String> suggestionList;
    private DefaultListModel<String> listModel;
    
    public GUI(Trie t) {
        trie = t;
        // Set up the main frame
        setTitle("Auto Suggest Editor");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));
        setLayout(new BorderLayout(20,0));
        setIconImage(new ImageIcon("download.png").getImage());
        
        // Create main content panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Create text area with padding
        textArea = new JTextArea();
        textArea.setFont(new Font("Consolas", Font.PLAIN, 16));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFocusTraversalKeysEnabled(false); // this will disable TAB behavior
        textArea.setTabSize(4);
        createSuggestions();
        
        textArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        textArea.setMargin(new Insets(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
        
        createActionPopup();
        addTextSelectionListener();
        add(createMenuBar(),BorderLayout.NORTH);
        
        pack();
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e){
                confirmationDialogue();
            }
        });
    }
    
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem newItem = new JMenuItem("New");
        JMenuItem openItem = new JMenuItem("Open...");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem saveAsItem = new JMenuItem("Save As...");
        JMenuItem exitItem = new JMenuItem("Exit");
        
        // action listeners
        newItem.addActionListener(e -> {
            if(textArea.getText().isBlank()){
                textArea.setText("");
                undoStack.clear();
                redoStack.clear();
                return;
            }
            
            int confirm = JOptionPane.showOptionDialog(this,
                    "Do you want to clear current document?", "New Document",
                    JOptionPane.YES_NO_CANCEL_OPTION, INFORMATION_MESSAGE, null,
                    new String[]{"Save work first", "Continue without saving", "Cancel"},
                    "Save");
            
            if (confirm == JOptionPane.YES_OPTION) {
                saveTextFile();
                textArea.setText("");
                undoStack.clear();
                redoStack.clear();
            } else if (confirm == NO_OPTION){
                textArea.setText("");
                undoStack.clear();
                redoStack.clear();
            }
        });
        
        openItem.addActionListener(e -> openTextFile());
        saveItem.addActionListener(e -> saveTextFile());
        exitItem.addActionListener(e -> confirmationDialogue());
        saveAsItem.addActionListener(e -> saveFileAs());
        
        // Add to menu
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Edit Menu
        JMenu editMenu = new JMenu("Edit");
        JMenuItem undoItem = new JMenuItem("Undo");
        JMenuItem redoItem = new JMenuItem("Redo");
        JMenuItem editorSettingsItem = new JMenuItem("Editor Settings...");
        
        undoItem.addActionListener(e -> {
            if (!undoStack.isEmpty()) {
                Operation previous = undoStack.pop();
                redoStack.push(previous);
                undoOperation(previous);
            }
        });
        
        redoItem.addActionListener(e -> {
            if (!redoStack.isEmpty()) {
                Operation next = redoStack.pop();
                undoStack.push(next);
                redoOperation(next);
            }
        });
        
        editorSettingsItem.addActionListener(e -> {
            EditorSettingsWindow settingsWindow = new EditorSettingsWindow(textArea);
            settingsWindow.setVisible(true);
        });
        
        editMenu.add(undoItem);
        editMenu.add(redoItem);
        editMenu.addSeparator();
        editMenu.add(editorSettingsItem);
        
        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        
        aboutItem.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Auto Suggest Editor\nCreated by Muhammad Noman", "About", JOptionPane.INFORMATION_MESSAGE)
        );
        
        helpMenu.add(aboutItem);
        
        // Add menus to the bar
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(helpMenu);
        
        return menuBar;
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
    
    private void openTextFile(){
        fileChooser = new JFileChooser("This PC");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
        if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            File choosenFile = fileChooser.getSelectedFile();
            
            try{
                String contentFromFile = new String(Files.readAllBytes(choosenFile.toPath()));
                textArea.removeAll();
                textArea.setText(contentFromFile);
                undoStack.clear();
                redoStack.clear();
            } catch(IOException e){
                throw new RuntimeException(e);
            }
        }
    }
    
    private int saveTextFile(){
        fileChooser = new JFileChooser("This PC");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
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
                JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage(), 
                                        "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return result;
    }
    
    private int saveFileAs(){
        JOptionPane.showMessageDialog(fileChooser, "Please specify the file type using EXTENSIONS\n to avoid any faults in saving. e.g. file.txt, file.docx");
        fileChooser = new JFileChooser("This PC");
        int result = fileChooser.showSaveDialog(this);
        if(result == JFileChooser.APPROVE_OPTION){
            File fileSavedAs = fileChooser.getSelectedFile();
            
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
        scrollPane.setPreferredSize(new Dimension(120, 150));
        suggestionPopup.add(scrollPane);
        
        addKeyListenersToTextArea();
        
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
    
    private void addKeyListenersToTextArea() {
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
                } else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C) {
                e.consume();
                String selectedText = textArea.getSelectedText();
                if(selectedText != null && !selectedText.equals("")){
                    copiedOrCutText = selectedText;
                }
                } else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_X) {
                    e.consume();
                    String selectedText = textArea.getSelectedText();
                    if(selectedText != null && !selectedText.equals("")){
                        int start = textArea.getSelectionStart();
                        int end = textArea.getSelectionEnd();
                        
                        copiedOrCutText = selectedText;
                        textArea.replaceRange("", start, end);
                        undoStack.push(new Operation(copiedOrCutText, false, start));
                        redoStack.clear();
                    }
                } else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_V) {
                    e.consume();
                    if(copiedOrCutText != null && !copiedOrCutText.isBlank()){
                        int position = textArea.getCaretPosition();
                        textArea.insert(copiedOrCutText, position);
                        undoStack.push(new Operation(copiedOrCutText, true, position));
                        redoStack.clear();
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_TAB){
                    SwingUtilities.invokeLater(() -> {
                        e.consume();
                        int pos = textArea.getCaretPosition();
//                        textArea.insert("\t", pos);
                        undoStack.push(new Operation("\t", true, pos));
                        redoStack.clear();
                        prefix = "";
                        suggestionPopup.setVisible(false);
                    });
                } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    int start = textArea.getSelectionStart();
                    int end = textArea.getSelectionEnd();
                    
                    // When the user selects text (deletes more than one character)
                    if (start != end) {
                        try {
                            String deleted = textArea.getText(start, end - start);
                            undoStack.push(new Operation(deleted, false, start));
                            redoStack.clear();
                        } catch (Exception ignored) {}
                    } else if (start > 0) { // Deletes single character (normal backspace)
                        try {
                            String deleted = textArea.getText(start - 1, 1);
                            undoStack.push(new Operation(deleted, false, start - 1));
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
                    
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        SwingUtilities.invokeLater(() -> {
                            int pos = textArea.getCaretPosition() - 1; // go back one step
                            if (pos >= 0) {
                                undoStack.push(new Operation("\n", true, pos));
                                redoStack.clear();
                                prefix = "";
                            }
                        });
                    }
                    
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
                    undoStack.push(new Operation(typedChar + "", true, pos));
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
    }
    
    private void undoOperation(Operation op){
        int pos = op.posOfcursor;
        String text = op.text;
        boolean wasInserted = op.wasInserted;
        
        if (wasInserted) {
            int end = Math.min(pos + text.length(), textArea.getText().length());
            if (pos >= 0 && end >= pos) {
                textArea.replaceRange("", pos, end);
            }
        } else {
            if (pos >= 0 && pos <= textArea.getText().length()) {
                textArea.insert(text, pos);
            }
        }
    }
    
    private void redoOperation(Operation op){
        int pos = op.posOfcursor;
        String text = op.text;
        boolean wasInserted = op.wasInserted;
        
        if (wasInserted) {
            if (pos >= 0 && pos <= textArea.getText().length()) {
                textArea.insert(text, pos);
            }
        } else {
            int end = Math.min(pos + text.length(), textArea.getText().length());
            if (pos >= 0 && end >= pos) {
                textArea.replaceRange("", pos, end);
            }
        }
    }
    
    private void createActionPopup() {
        actionPopupTextSelected = new JPopupMenu();
        actionPopupTextNotSelected = new JPopupMenu();
        JMenuItem addItem = new JMenuItem("Add to Dictionary");
        JMenuItem deleteItem = new JMenuItem("Delete from Dictionary");
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
        
        copyItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedText = textArea.getSelectedText();
                if(selectedText != null && !selectedText.equals("")){
                    copiedOrCutText = textArea.getSelectedText();
                }
            }
        });
        
        cutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedText = textArea.getSelectedText();
                if(selectedText != null && !selectedText.equals("")){
                    int start = textArea.getSelectionStart();
                    int end = textArea.getSelectionEnd();
                    
                    copiedOrCutText = textArea.getSelectedText();
                    textArea.replaceRange("", start, end);
                    undoStack.push(new Operation(copiedOrCutText, false, start));
                }
            }
        });
        
        pasteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(copiedOrCutText != null && !copiedOrCutText.isBlank()){
                    int position = textArea.getCaretPosition();
                    textArea.insert(copiedOrCutText, position);
                    undoStack.push(new Operation(copiedOrCutText, true, position));
                    redoStack.clear();
                }
            }
        });
        
        // For popup when a text is not selected
        actionPopupTextNotSelected.add(pasteItem);
        
        // For popup when a text is selected
        actionPopupTextSelected.add(copyItem);
        actionPopupTextSelected.add(cutItem);
        actionPopupTextSelected.add(addItem);
        actionPopupTextSelected.add(deleteItem);
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
                                actionPopupTextSelected.show(textArea, viewRect.x, viewRect.y - 80);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else if (textArea.getSelectedText() == null) {
                        try {
                            Rectangle viewRect = textArea.modelToView(textArea.getCaretPosition());
                            if (viewRect != null) {
                                actionPopupTextNotSelected.show(textArea, viewRect.x, viewRect.y - 30);
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
            int pos = textArea.getCaretPosition(); // Get caret position
            int line = textArea.getLineOfOffset(pos); // Get line number
            int lineStart = textArea.getLineStartOffset(line); // Get the starting index of line
            
            String lineText = textArea.getText(lineStart, pos - lineStart); // Yeh text lega poori line ka
            int lastSpace = lineText.lastIndexOf(" "); // Yeh line me sub se pehle aane wali space ka index lega
            
            int start;
            if (lastSpace == -1) { // lastSpace -1 hone ka mtlb = line me kahi pr bhi sapce nahi hai
                start = lineStart; // Line ka start hi starting index hai (for inserting the word)
            } else {
                start = lineStart + lastSpace + 1; // Last space k baad wali position is for inserting words
            }
            
            textArea.replaceRange(word, start, pos); // Simply jo position start ki set hui hai waha text daal do
            
            /*
             Thora exceptional cases wala part
             When you have inserted a suggested word after typing the prefix, the prefix still remains in the stack
             as Operations (character-by-character). So, before inserting the suggested word into the undoStack (as
             String), you have to remove the characters from undoStack
            */
            int prefixLength = pos - start;
            for (int i = 0; i < prefixLength; i++) {
                if (!undoStack.isEmpty()) {
                    Operation last = undoStack.peek();
                    if (last.wasInserted && last.text.length() == 1) {
                        undoStack.pop();
                    } else break;
                }
            }
            
            // Push the whole word
            undoStack.push(new Operation(word, true, start));
            redoStack.clear();
            prefix = "";
            suggestionPopup.setVisible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
