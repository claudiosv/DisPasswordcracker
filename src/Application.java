public class Application {
    public static void main(String[] args)
    {
        try{
        if (args.length == 0){
            System.out.println("Must specify if start as server [-s] or client [-c <subnet>]");
            System.exit(-1);
        }else{
            if(args[0].equalsIgnoreCase("-s")) {
                Worker.getInstance().startAsServer(false);
            }else if(args[0].equalsIgnoreCase("-c")) {
                Worker.getInstance().startAsClient(args[1]);
                /*we could check if the ip is valid
                but we are computer scientists we know how to write an IP right? */
            }else{
                System.out.println("Malformed input!");
                System.out.println("Must specify if start as server [-s] or client [-c <subnet>]");
                System.exit(-1);
            }
        }
    } catch (OutOfMemoryError oE){
            System.out.println("Debug: OUT OF MEMORY");
            System.exit(-1);
        }
    }
}
