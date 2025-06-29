import java.io.*;
import java.util.LinkedList;
import java.util.Queue;

public class Trie {
     static class trieNode{
         
         String word;
        int frequency;
        trieNode children[];
        boolean endOfWord;
        
        trieNode(){
            children = new trieNode[26];
            endOfWord = false;
            frequency = 0;
        }
    }
    
    private trieNode root = new trieNode();
    
    Trie(String filePath){
        try{
            populateTrie(filePath);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    
    void populateTrie(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line = reader.readLine();
        while (line != null) {
            String[] parts = line.split("\t");
            String word = parts[0];
            int frequency = Integer.parseInt(parts[1]);
            insert(word, frequency);
            line = reader.readLine();
        }
        reader.close();
    }
    
    public void insert(String word, int frequency){
        if(isAlphabetsOnly(word))
            insert(word, frequency, root);
    }
    
    private void insert(String word, int frequency, trieNode root){
        for(int i = 0; i< word.length(); i++){
            char ch = Character.toLowerCase(word.charAt(i));
            int idx = ch - 'a';
            
            if(root.children[idx] == null)
                root.children[idx] = new trieNode();
            
            if(i == word.length()-1){
                root.children[idx].endOfWord = true;
                root.children[idx].frequency = frequency;
                root.children[idx].word = word;
            }
            
            root = root.children[idx];
            
        }
    }
    
    public boolean search(String word){
        if(isAlphabetsOnly(word))
            return search(word, root);
        return false;
    }
    
    private boolean search(String word, trieNode root){
        for(int i = 0; i < word.length(); i++){
            char ch = Character.toLowerCase(word.charAt(i));
            int idx = ch - 'a';
            
            if(root.children[idx] == null)
                return false;
            
            if(i == word.length() - 1)
                return root.children[idx].endOfWord;
            
            root = root.children[idx];
        }
        return false;
    }
    
    public void delete(String word){
        if(isAlphabetsOnly(word))
            root = delete(word, root, 0);
        else
            System.out.println("No such words found!");
    }
    
    private trieNode delete(String word, trieNode root, int idxOfLetter){
        if(root == null){
            System.out.println("No word found.");
            return null;
        }
        
        if(idxOfLetter == word.length()){
            if(root.endOfWord){
                root.endOfWord = false;
                root.word = null;
//                System.out.println("Word found and unmarked!");
            } else {
                System.out.println("Word not found in Trie.");
            }
            
            if(!hasChildren(root) && root != this.root){
                return null;
            }
            return root;
        }
        
        char ch = Character.toLowerCase(word.charAt(idxOfLetter));
        int idx = ch -'a';
        
        root.children[idx] = delete(word, root.children[idx], idxOfLetter+1);
        if (!hasChildren(root) && !root.endOfWord && root != this.root) {
            return null;
        }
        
        return root;
    }
    
    public boolean hasChildren(trieNode root){
        for(int i = 0; i < 26; i++){
            if(root.children[i] != null)
                return true;
        }
        
        return false;
    }
    
    public boolean isAlphabetsOnly(String word){
        for(int i=0; i<word.length(); i++){
            char ch = Character.toLowerCase(word.charAt(i));
            if(ch < 97 || ch > 122)
                return false;
        }
        return true;
    }
    
    public void traverse(){
        traverse(root);
    }
    
    private void traverse(trieNode root){
        Queue<trieNode> q = new LinkedList<>();
        
        q.add(root);
        q.add(null);
         while(!q.isEmpty()){
             trieNode d = q.remove();
             
             if(d == null){
                 System.out.println();
                 if(q.isEmpty())
                     return;
                 q.add(null);
             }
             
             else {
                 for(int i = 0; i < 26; i++){
                     if(d.children[i] != null){
                         System.out.print((char)(i + 'a') + " ");
                         q.add(d.children[i]);
                     }
                 }
             }
         }
    }
    
    public LinkedList<trieNode> getSuggestions(String prefix) {
        return getSuggestions(prefix, root);
    }
    
    private LinkedList<trieNode> getSuggestions(String prefix, trieNode root) {
        LinkedList<trieNode> suggestions = new LinkedList<>();
        
        // Yaha hum ne prefix k last letter tk jana hai
        for (int i = 0; i < prefix.length(); i++) {
            char ch = Character.toLowerCase(prefix.charAt(i));
            int idx = ch - 'a';
            
            if (root.children[idx] == null) {
                if(root.endOfWord) // Check if only one letter creates a word (e.g., a)
                    suggestions.add(root);
                return suggestions; // No words with this prefix
            }
            
            root = root.children[idx];
        }
        
        dfs(root, suggestions);
        return suggestions;
    }
    
    private void dfs(trieNode node, LinkedList<trieNode> result) {
        if (node == null) return;
        
        if (node.endOfWord && node.word != null) { // Ye sirf double-check hai (word or EOF dono check krna)
            result.add(node); // Agar word hai to list me dalo
        }
        
        for (int i = 0; i < 26; i++) { // Har node pe check lagao
            if (node.children[i] != null) { // Agar null nahi hai to DFS apply karo
                dfs(node.children[i], result);
            }
        }
    }
    
}
