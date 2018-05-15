public class Application {
    public static void main(String[] args)
    {
        if(args.length > 0)
        {
            Worker.getInstance().startAsServer();
        }
        else
        {
            Worker.getInstance().startAsClient();
        }
    }
}
