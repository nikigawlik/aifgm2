import java.util.HashMap;
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
        int id = client.getMyPlayerNumber();

        Bot speedyBot = new Bot(Bot.BotType.Speedy);
        Bot fatBot = new Bot(Bot.BotType.Fatty);
        Bot wallBot = new Bot(Bot.BotType.Ghosty);
        Bot[] bots = {speedyBot, fatBot, wallBot};

        GraphNode[] graph = client.getGraph();
        HashMap<GraphNode, Float> extraWeights = calculateExtraWeights(graph);

        while (client.isAlive()) {
            long t0 = System.nanoTime();

            client.getBotSpeed(0); // raw constant
            client.getScore(id);
            // client.changeMoveDirection(1, -0.15f * ((float) Math.random() * 2.0f - 1.0f));

            graph = client.getGraph();
            
            for(int botID = 0; botID < 3; botID++) {
                float[] position = client.getBotPosition(id, botID); // array with x,y,z
                float[] direction = client.getBotDirection(botID); // array with x,y,z
                Bot bot = bots[botID];
                float deltaAngle = bot.update(graph, extraWeights, position, direction);
                client.changeMoveDirection(botID, deltaAngle);
            }

            long t = System.nanoTime() - t0;

            try {
                long delay = 100;
                Thread.sleep(Math.max(delay - t / 1000000, 0));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private HashMap<GraphNode, Float> calculateExtraWeights(GraphNode[] graph) {
        HashMap<GraphNode, Float> extraWeights = new HashMap<>();

        int steps = 5;
        float wallValue = 10f;
        float decrement = wallValue / steps;
        float[] weightBuffer = new float[graph.length];

        for(int i = 0; i < graph.length; i++) {
            if(graph[i].blocked)
                weightBuffer[i] = 10f;
        }
        
        for(int step = 0; step < steps; step++) {

            // expand obstacles by 1
            for (int i = 0; i < graph.length; i++) {
                GraphNode node = graph[i];
                for (GraphNode neighbor : node.neighbors) {
                    weightBuffer[i] = Math.max(extraWeights.getOrDefault(neighbor, 0f) - decrement, weightBuffer[i]);
                }
            }

            for(int i = 0; i < graph.length; i++) {
                extraWeights.put(graph[i], weightBuffer[i]);
            }
        }

        return extraWeights;
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