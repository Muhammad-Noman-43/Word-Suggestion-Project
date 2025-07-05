import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EditorSettingsWindow extends JFrame {
    private JTextArea textArea;
    private JColorChooser fontColorChooser;
    private JColorChooser backgroundColorChooser;
    private JComboBox<String> fontFamilyCombo;
    private JComboBox<String> fontStyleCombo;
    private JSlider fontSizeSlider;
    private JLabel fontSizeLabel;
    private JCheckBox boldCheck;
    private JCheckBox italicCheck;
    
    public EditorSettingsWindow(JTextArea parentTextArea) {
        this.textArea = parentTextArea;
        initializeComponents();
        setupLayout();
        setupEventListeners();
        loadCurrentSettings();
        
        setTitle("Editor Settings");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
    }
    
    private void initializeComponents() {
        String[] fontFamilies = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        fontFamilyCombo = new JComboBox<>(fontFamilies);
        fontFamilyCombo.setSelectedItem("Monospaced");
        
        String[] fontStyles = {"Plain", "Bold", "Italic", "Bold Italic"};
        fontStyleCombo = new JComboBox<>(fontStyles);
        
        fontSizeSlider = new JSlider(8, 72, 16);
        fontSizeSlider.setMajorTickSpacing(10);
        fontSizeSlider.setMinorTickSpacing(2);
        fontSizeSlider.setPaintTicks(true);
        fontSizeSlider.setPaintLabels(true);
        
        fontSizeLabel = new JLabel("16");
        
        boldCheck = new JCheckBox("Bold");
        italicCheck = new JCheckBox("Italic");
        
        fontColorChooser = new JColorChooser(Color.BLACK);
        backgroundColorChooser = new JColorChooser(Color.WHITE);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        JPanel fontPanel = createFontPanel();
        tabbedPane.addTab("Font", fontPanel);
        
        JPanel colorPanel = createColorPanel();
        tabbedPane.addTab("Colors", colorPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton applyButton = new JButton("Apply All");
        JButton resetButton = new JButton("Reset");
        JButton closeButton = new JButton("Close");
        
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyAllSettings();
            }
        });
        
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetToDefaults();
            }
        });
        
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        buttonPanel.add(applyButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(closeButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createFontPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        northPanel.add(new JLabel("Font Family:"));
        northPanel.add(fontFamilyCombo);
        northPanel.add(Box.createHorizontalStrut(20));
        northPanel.add(new JLabel("Font Style:"));
        northPanel.add(fontStyleCombo);
        
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        centerPanel.add(new JLabel("Font Size:"));
        centerPanel.add(fontSizeSlider);
        centerPanel.add(fontSizeLabel);
        
        panel.add(northPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createColorPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel fontColorPanel = new JPanel(new BorderLayout());
        fontColorPanel.setBorder(BorderFactory.createTitledBorder("Font Color"));
        fontColorPanel.add(fontColorChooser, BorderLayout.CENTER);
        
        JPanel backgroundColorPanel = new JPanel(new BorderLayout());
        backgroundColorPanel.setBorder(BorderFactory.createTitledBorder("Background Color"));
        backgroundColorPanel.add(backgroundColorChooser, BorderLayout.CENTER);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fontColorPanel, backgroundColorPanel);
        splitPane.setResizeWeight(0.5);
        
        panel.add(splitPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void setupEventListeners() {
        fontFamilyCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyFontSettings();
            }
        });
        
        fontStyleCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyFontSettings();
            }
        });
        
        fontSizeSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                fontSizeLabel.setText(String.valueOf(fontSizeSlider.getValue()));
                applyFontSettings();
            }
        });
        
        boldCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyFontSettings();
            }
        });
        
        italicCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyFontSettings();
            }
        });
        
        fontColorChooser.getSelectionModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                applyColorSettings();
            }
        });
        
        backgroundColorChooser.getSelectionModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                applyColorSettings();
            }
        });
    }
    
    private void loadCurrentSettings() {
        if (textArea != null) {
            Font currentFont = textArea.getFont();
            fontFamilyCombo.setSelectedItem(currentFont.getFamily());
            fontSizeSlider.setValue(currentFont.getSize());
            fontSizeLabel.setText(String.valueOf(currentFont.getSize()));
            
            if (currentFont.isBold() && currentFont.isItalic()) {
                fontStyleCombo.setSelectedItem("Bold Italic");
            } else if (currentFont.isBold()) {
                fontStyleCombo.setSelectedItem("Bold");
            } else if (currentFont.isItalic()) {
                fontStyleCombo.setSelectedItem("Italic");
            } else {
                fontStyleCombo.setSelectedItem("Plain");
            }
            
            fontColorChooser.setColor(textArea.getForeground());
            backgroundColorChooser.setColor(textArea.getBackground());
        }
    }
    
    private void applyFontSettings() {
        if (textArea == null) return;
        
        String fontFamily = (String) fontFamilyCombo.getSelectedItem();
        int fontSize = fontSizeSlider.getValue();
        
        int fontStyle = Font.PLAIN;
        String selectedStyle = (String) fontStyleCombo.getSelectedItem();
        
        if (selectedStyle.contains("Bold")) {
            fontStyle |= Font.BOLD;
        }
        if (selectedStyle.contains("Italic")) {
            fontStyle |= Font.ITALIC;
        }
        
        Font newFont = new Font(fontFamily, fontStyle, fontSize);
        textArea.setFont(newFont);
        
        if (selectedStyle.contains("Bold")) {
            boldCheck.setSelected(true);
        } else {
            boldCheck.setSelected(false);
        }
        
        if (selectedStyle.contains("Italic")) {
            italicCheck.setSelected(true);
        } else {
            italicCheck.setSelected(false);
        }
    }
    
    private void applyColorSettings() {
        if (textArea == null) return;
        
        textArea.setForeground(fontColorChooser.getColor());
        textArea.setBackground(backgroundColorChooser.getColor());
    }
    
    private void applyAllSettings() {
        applyFontSettings();
        applyColorSettings();
        JOptionPane.showMessageDialog(this, "All settings applied successfully!", "Settings Applied", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void resetToDefaults() {
        fontFamilyCombo.setSelectedItem("Consolas");
        fontStyleCombo.setSelectedItem("Plain");
        fontSizeSlider.setValue(16);
        fontSizeLabel.setText("16");
        boldCheck.setSelected(false);
        italicCheck.setSelected(false);
        
        fontColorChooser.setColor(Color.BLACK);
        backgroundColorChooser.setColor(Color.WHITE);
        
        applyAllSettings();
    }
}