public class HelloWorld {
    public native void print();
    static {
        System.out.println(System.getProperty("java.library.path"));
        System.loadLibrary("hello");
    }

    public static void main(String[] args) {
        HelloWorld h=new HelloWorld();
        h.print();;
    }
}
