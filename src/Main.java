import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

public class Main {
    public static void main(String[] args) throws IOException {
        Trie t = new Trie();
//        String arr[] = {"Application", "App", "Hello", "Heelo", "Mic", "Check", "One", };
//        for(String str: arr)
//            t.insert(str);
//        System.out.println(t.search("app"));
//        t.traverse();
//        t.delete("app");
//        t.delete("one");
//        t.traverse();
//        System.out.println(t.search("app"));
//
        BufferedReader reader = new BufferedReader(new FileReader("merged.txt"));
        String line = reader.readLine();
        while (line != null) {
            String[] parts = line.split("\t");
            String word = parts[0];
            int frequency = Integer.parseInt(parts[1]);
            t.insert(word, frequency);
            line = reader.readLine();
        }
        reader.close();
//
//        t.traverse();
//        System.out.println(t.search("growth"));
//
//        LinkedList<Trie.trieNode> suggestions = t.getSuggestions("a");
//        for(Trie.trieNode node: suggestions)
//            System.out.println(node.word + " " + node.frequency);
        GUI gui = new GUI();
        gui.setTrie(t);
        gui.setVisible(true);
    }
}