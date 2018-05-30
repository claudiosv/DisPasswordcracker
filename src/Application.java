public class Application {
    public static void main(String[] args)
    {
        if (args.length == 0){
            System.out.println("Must specify if start as server [-s] or client [-c <subnet>]");
            System.exit(-1);
        }else{
            if(args[0].equalsIgnoreCase("-s")) {
                Worker.getInstance().startAsServer();
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
    }
}
