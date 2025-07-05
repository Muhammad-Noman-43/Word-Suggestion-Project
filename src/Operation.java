public class Operation {
    String text;
    boolean wasInserted;
    int posOfcursor;
    
    Operation(String txt, boolean wasInserted, int poc){
        this.posOfcursor = poc;
        this.wasInserted = wasInserted;
        this.text = txt;
    }
}
