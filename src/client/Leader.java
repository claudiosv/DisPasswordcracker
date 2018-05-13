package client;
/**
 * Leader
 */
public class Leader extends Worker {

    public void parsePacket(String packet) {
        String[] contents = packet.split(" ");

        switch (contents[0]) {
        case "SOLVE": // SOLVE 0957u387r84r7thisisahash98fre98
            // search hashmap
        case "RANGE": // e.g. RANGE 500-1000
            // work all integers in the range
        case "CURR": // CURR 500
            // updates the worker on the current pool max so that in case the leader is
            // lost,
            // the client can assume leader position and work from there
        case "MORE": // MORE
            // this is something only the leader can reply to
            // reply error NOT LEADER if mistaken, otherwise send new WORK packet
        case "IPLIST": // IPLIST 127.0.0.1 127.0.0.2
            // if leader, inform the client of the current other members in pool
            // this way, the clients can pick the lowest ip as the new leader
            // the new leader will have to reregister!!!!
        case "SOLVED": // SOLVED 0957u387r84r7thisisahash98fre98 666666
            // only the leader receives this
        case "STOP": // STOP 0957u387r84r7thisisahash98fre98
            // leader informs workers to stop all work on this hash (since a solution is
            // found)
        }

    }
}
