import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Objects;
//import java.util.Stack;

public class GUI extends JFrame {
    
    StackUsingLL<String> wordStack = new StackUsingLL<>();
    String prefix = new String("");
    private JTextArea textArea;
    private JTabbedPane tabbedPane;
    Trie trie;
    private JPopupMenu suggestionPopup;
    private JList<String> suggestionList;
    private DefaultListModel<String> listModel;
    
    
    void setTrie(Trie t){
        trie = t;
    }
    
    public GUI() {
        // Set up the main frame
        setTitle("Word-Like Interface");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));
        
        // Create menu bar
        createMenuBar();
        
        // Create main content panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Home", createHomePanel());
        tabbedPane.addTab("Insert", createInsertPanel());
        tabbedPane.addTab("Layout", createLayoutPanel());
        
        mainPanel.add(tabbedPane, BorderLayout.NORTH);
        
        // Create text area with padding
        textArea = new JTextArea();
        textArea.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        createSuggestions();
        
        // Add padding to text area using a border
        textArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Add scroll pane for text area
        JScrollPane scrollPane = new JScrollPane(textArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add main panel to frame
        add(mainPanel);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(new JMenuItem("New"));
        fileMenu.add(new JMenuItem("Open"));
        fileMenu.add(new JMenuItem("Save"));
        fileMenu.addSeparator();
        fileMenu.add(new JMenuItem("Exit"));
        
        // Edit menu
        JMenu editMenu = new JMenu("Edit");
        editMenu.add(new JMenuItem("Cut"));
        editMenu.add(new JMenuItem("Copy"));
        editMenu.add(new JMenuItem("Paste"));
        
        // View menu
        JMenu viewMenu = new JMenu("View");
        viewMenu.add(new JCheckBoxMenuItem("Ruler"));
        viewMenu.add(new JCheckBoxMenuItem("Status Bar"));
        
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        
        setJMenuBar(menuBar);
    }
    
    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Add some buttons like in Word's Home tab
        String[] buttons = {"Font", "Paragraph", "Styles", "Editing"};
        for (String btn : buttons) {
            JButton button = new JButton(btn);
            button.setPreferredSize(new Dimension(80, 25));
            panel.add(button);
        }
        
        return panel;
    }
    
    private JPanel createInsertPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Add some buttons like in Word's Insert tab
        String[] buttons = {"Table", "Picture", "Shapes", "Chart"};
        for (String btn : buttons) {
            JButton button = new JButton(btn);
            button.setPreferredSize(new Dimension(80, 25));
            panel.add(button);
        }
        
        return panel;
    }
    
    private JPanel createLayoutPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Add some buttons like in Word's Layout tab
        String[] buttons = {"Margins", "Orientation", "Size", "Columns"};
        for (String btn : buttons) {
            JButton button = new JButton(btn);
            button.setPreferredSize(new Dimension(80, 25));
            panel.add(button);
        }
        
        return panel;
    }
    
    
//    void createSuggestions(){
//        textArea.addKeyListener(new KeyAdapter() {
            
//            @Override // Special keys do not generate keyTyped event
//            public void keyPressed(KeyEvent e) {
//                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
//                    System.out.println("Backspace pressed");
//                }
//                else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
//                    System.out.println("Enter pressed");
//                }
//            }
            
//            @Override // Special keys do not generate keyTyped event
//            public void keyReleased(KeyEvent e) {
//                System.out.println("Key released: " + KeyEvent.getKeyText(e.getKeyCode()));
//            }
            
//            @Override
//            public void keyTyped(KeyEvent e) {
//                System.out.println("Key typed: " + e.getKeyChar());
//                chEntered = e.getKeyChar();
//                System.out.println("ChEnterd: " + chEntered);
//                prefix = setPrefix(chEntered);
//                System.out.println("Prefix : " + prefix);
                
//                getSuggestionsFromTrie(prefix, trie);
//            }
//        });
//
//    }
    
    void createSuggestions() {
        // List and Model setup
        listModel = new DefaultListModel<>();
        suggestionList = new JList<>(listModel);
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        
        // Popup menu setup
        suggestionPopup = new JPopupMenu();
        suggestionPopup.setFocusable(false);
        suggestionPopup.add(new JScrollPane(suggestionList));
        
        // Key listener for typing in the text area
        textArea.addKeyListener(new KeyAdapter() {
            
            @Override
            public void keyPressed(KeyEvent e){
                if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE){
                    prefix = setPrefix(e);
                    updatePopupSuggestions(prefix);
                }
            }
            
            @Override
            public void keyTyped(KeyEvent e) {
                prefix = setPrefix(e);
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
    
    
    String setPrefix(KeyEvent e){
        char charEntered = Character.toLowerCase(e.getKeyChar());
        if(charEntered >=97 && charEntered <= 122){
            return prefix + charEntered;
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE){
            if(!prefix.isEmpty()){
                System.out.println("Pressed Space");
                wordStack.push(prefix);
                wordStack.push(" ");
            } else if(wordStack.peek() == " "){
                wordStack.push(" ");
            }
            return "";
        } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE){
            System.out.println("Pressed Backspace");
            if(!wordStack.isEmpty() && wordStack.peek() == " " && prefix.isEmpty()){
                wordStack.pop();
                if(wordStack.peek().equals(" "))
                    return prefix;
                return !wordStack.isEmpty() ? wordStack.pop() : "";
            }
            
            StringBuilder temp = new StringBuilder(prefix);
            temp.deleteCharAt(temp.length()-1);
            return temp.toString();
        }
        return prefix;
    }
    
    void updatePopupSuggestions(String prefix) {
        if (prefix.isEmpty()) {
            suggestionPopup.setVisible(false);
            return;
        }
        
        List<Trie.trieNode> list = trie.getSuggestions(prefix);
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
    
    void insertSuggestedWord(String word) {
        try {
            int pos = textArea.getCaretPosition();
            String text = textArea.getText(0, pos);
            
            // Remove the current prefix from text and insert suggestion
            int start = text.lastIndexOf(' ') + 1;
            textArea.replaceRange(word + " ", start, pos);
            wordStack.push(word);
            wordStack.push(" ");
            prefix = ""; // reset the prefix
            suggestionPopup.setVisible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
}
