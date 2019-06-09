import java.util.Random;

import lenz.htw.cywwtaip.net.NetworkClient;
import lenz.htw.cywwtaip.world.GraphNode;

public class Client extends Thread{
    public static void main(String[] args) {
        for(int i = 0; i < 3; i++) {
            Client c = new Client();
            c.start();
        }
    }

    @Override
    public void run() {
        String name = generateName(1) + " of " + generateName(1);
        NetworkClient client = new NetworkClient(null, name, "YEET");

        while (client.isAlive()) {
            client.getBotSpeed(0); // raw constant
            client.getScore(client.getMyPlayerNumber());
            client.changeMoveDirection(1, -0.15f * ((float) Math.random() * 2.0f - 1.0f));

            float[] position = client.getBotPosition(0, 0); // array with x,y,z
            float[] direction = client.getBotDirection(0); // array with x,y,z

            GraphNode[] graph = client.getGraph();
            for (GraphNode n : graph[0].neighbors) {
                System.out.println(n + ": " + n.owner + ", " + n.blocked);
            }

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // simple name generator for funny player names
    private String generateName(int syllableCount) {
        String[] kons = {"qu", "w", "wh", "r", "rr", "rh", "t", "th", "tz", "tr", "z", "zh", "p", "ph", "phl", "pt", "s", "sh", "sch", "sc", "sk", "sl", "sw", "sn", "d", "dh", "dn", "dw", "f", "fl", "fr", "g", "gh", "gl", "gr", "h", "k", "kl", "kh", "kr", "kw", "l", "y", "x", "c", "ch", "cl", "v", "vl", "b", "bl", "bh", "bw", "n", "nl", "nh", "m", "ml"};
        String[] vocs = {"a", "a", "aa", "au", "e", "ei", "ee", "eh", "i", "ii", "ie", "i", "o", "oo", "oof", "oh", "ou", "oe", "oau", "u", "uu", "u", "ui", "ue"};

        Random random = new Random();

        String name = "";
        for(int i = 0; i < syllableCount; i++) {
            name += kons[random.nextInt(kons.length)];
            name += vocs[random.nextInt(vocs.length)];
        }
        name += kons[random.nextInt(kons.length)];

        return name;
    }
}