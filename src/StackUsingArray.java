public class StackUsingArray<T>{
    T[] array;
    StackUsingArray(){
        array = (T[]) new Object[2000];
    }
    int top = -1;
    
    public void push(T data){
        if(isFull()){
            System.out.println("Stack Overflow");
            return;
        }
        array[++top] = data;
    }
    
    public T pop(){
        if(isEmpty()){
            System.out.println("Stack underflow");
            return null;
        }
        T data = array[top];
        array[top--] = null;
        return data;
    }
    
    public T peek(){
        if(isEmpty()){
            System.out.println("Stack Empty");
            return null;
        }
        return array[top];
    }
    
    boolean isEmpty(){
        if(top == -1)
            return true;
        return false;
    }
    
    boolean isFull(){
        if(top == array.length-1)
            return true;
        return false;
    }
    
    void clear(){
        for (int i = 0; i <= top; i++) {
            array[i] = null;
        }
        top = -1;
    }
}
