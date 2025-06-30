import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

public class Main {
    public static void main(String[] args) throws IOException {
        Trie t = new Trie("final_merged_frequencies.txt");
        GUI gui = new GUI(t);
        gui.setVisible(true);
    }
}