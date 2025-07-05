import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Trie t = new Trie("final_merged_frequencies.txt");
        GUI gui = new GUI(t);
        gui.setVisible(true);
    }
}