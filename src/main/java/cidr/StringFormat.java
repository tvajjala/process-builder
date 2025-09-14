package cidr;

public class StringFormat {

    public static void main(String[] args) {

        String command = "getent hosts %s | awk '{print $1}'";

        System.out.println(String.format(command,"hello.com"));
    }
}
