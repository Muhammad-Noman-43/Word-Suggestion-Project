import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

public class Main {
    public static void main(String[] args) throws IOException {
        Trie t = new Trie("B:\\University Material\\4th Semester\\Data Structures\\Project\\final_merged_frequencies.txt");
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

//
//        t.traverse();
//        System.out.println(t.search("growth"));
//
//        LinkedList<Trie.trieNode> suggestions = t.getSuggestions("a");
//        for(Trie.trieNode node: suggestions)
//            System.out.println(node.word + " " + node.frequency);
        GUI gui = new GUI(t);
        gui.setVisible(true);
    }
}