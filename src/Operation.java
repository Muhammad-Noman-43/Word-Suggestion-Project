public class Operation {
    char ch;
    boolean wasInserted;
    int posOfcursor;
    
    Operation(char ch, boolean wasInserted, int poc){
        this.posOfcursor = poc;
        this.wasInserted = wasInserted;
        this.ch = ch;
    }
}
