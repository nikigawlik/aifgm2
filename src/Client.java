import java.util.Arrays;
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
        NetworkClient client = new NetworkClient(null, name, generateCatchphrase());
        int id = client.getMyPlayerNumber();
        System.out.println("My player number is " + id);


        Bot speedyBot = new Bot(Bot.BotType.Speedy, id);
        Bot ghostBot = new Bot(Bot.BotType.Ghosty, id);
        Bot fatBot = new Bot(Bot.BotType.Fatty, id);
        Bot[] bots = {speedyBot, ghostBot, fatBot};

        GraphNode[] graph = client.getGraph();
        HashMap<GraphNode, Float> extraWeights = calculateExtraWeights(graph);

        float energy = 1f;
        boolean botIsCharging = false;
        long t0 = System.nanoTime();

        while (client.isAlive()) {
            // if(id != 0) 
            //     continue;
            float deltaTime = Math.max(0.001f, (System.nanoTime() - t0) / 1000000000f);
            t0 = System.nanoTime();

            client.getBotSpeed(0); // raw constant
            client.getScore(id);
            // client.changeMoveDirection(1, -0.15f * ((float) Math.random() * 2.0f - 1.0f));

            graph = client.getGraph();

            // calculate current energy
            energy -= deltaTime * 0.1f;
            for(int botID = 0; botID < 3; botID++) {
                if(isLoadingZone(client.getBotPosition(id, botID))) {
                    energy = 1f;
                    botIsCharging = false;
                    break;
                }
            }

            // System.out.println("deltaTime: " + deltaTime);
            // System.out.println("energy: " + energy);

            for(int botID = 0; botID < 3; botID++) {
                bots[botID].position = client.getBotPosition(id, botID);
            }

            // target assignment
            if(energy < 0.75 && !botIsCharging) {
                float bestDistance = Float.POSITIVE_INFINITY;
                Bot bestBot = null;
                for (int botID = 0; botID < 3; botID++) {
                    float estimatedDistance = bots[botID].estimateDistanceToNextLoadingZone();
                    if(estimatedDistance < bestDistance) {
                        bestDistance = estimatedDistance;
                        bestBot = bots[botID];
                    }
                }

                bestBot.goToNextLoadingZone();
                botIsCharging = true;
            }

            // other targets
            for (int botID = 0; botID < 3; botID++) {
                if(bots[botID].targetPos == null) {
                    bots[botID].targetPos = MathUtils.randomPointOnUnitSphere(); // random for now TODO
                    // Arrays.stream(graph)
                    // .parallel()
                    // .filter((n) -> (n.owner != 0 && n.owner != id+1))
                    // .min((n1, n2) -> MathUtils.distanceOnUnitSphere(a, b))
                }
            }

            // call update methods            
            for(int botID = 0; botID < 3; botID++) {
                float[] direction = client.getBotDirection(botID); // array with x,y,z
                Bot bot = bots[botID];
                float deltaAngle = bot.update(graph, extraWeights, direction);
                try {
                    client.changeMoveDirection(botID, deltaAngle);
                } catch(RuntimeException e) {
                    e.printStackTrace();
                }
            }

            long t = System.nanoTime() - t0;

            try {
                long delay = 200;
                Thread.sleep(Math.max(delay - t / 1000000, 0));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isLoadingZone(float[] pos) {
        return Math.abs(pos[0]) > 0.95f || Math.abs(pos[1]) > 0.95f || Math.abs(pos[2]) > 0.95f;
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

    private String generateCatchphrase() {
        String[] words = new String[] { "Ignoramus", "Blitzkrieg", "Dawdle", "Polymath", "Repertoire", "Tramontane", "Stellar", "Pithy", "Quiver", "Clod" };

        String[] adjectives = new String[] {"abhorrent", "ablaze", "abnormal", "abrasive", "acidic", "alluring", "ambiguous", "amuck", "apathetic", "aquatic", "auspicious", "axiomatic", "", "barbarous", "bawdy", "belligerent", "berserk", "bewildered", "billowy", "boorish", "brainless", "bustling", "", "cagey", "calculating", "callous", "capricious", "ceaseless", "chemical", "chivalrous", "cloistered", "coherent", "colossal", "combative", "cooing", "cumbersome", "cynical", "", "daffy", "damaged", "deadpan", "deafening", "debonair", "decisive", "defective", "defiant", "demonic", "delerious", "deranged", "devilish", "didactic", "diligent", "direful", "disastrous", "disillusioned", "dispensable", "divergent", "domineering", "draconian", "dynamic", "", "earsplitting", "earthy", "eatable", "efficacious", "elastic", "elated", "elfin", "elite", "enchanted", "endurable", "erratic", "ethereal", "evanescent", "exuberant", "exultant", "", "fabulous", "fallacious", "fanatical", "fearless", "feeble", "feigned", "fierce", "flagrant", "fluttering", "frantic", "fretful", "fumbling", "furtive", "", "gainful", "gamy", "garrulous", "gaudy", "glistening", "grandiose", "grotesque", "gruesome", "guiltless", "guttural", "", "habitual", "hallowed", "hapless", "harmonious", "hellish", "hideous", "highfalutin", "hissing", "holistic", "hulking", "humdrum", "hypnotic", "hysterical", "", "icky", "idiotic", "illustrious", "immense", "immenent", "incandescent", "industrious", "infamous", "inquisitive", "insidious", "invincible", "", "jaded", "jazzy", "jittery", "judicious", "jumbled", "juvenile", "", "kaput", "keen", "knotty", "knowing", "", "lackadaisical", "lamentable", "languid", "lavish", "lewd", "longing", "loutish", "ludicrous", "lush", "luxuriant", "lyrical", "", "macabre", "maddening", "mammoth", "maniacal", "meek", "melodic", "merciful", "mere", "miscreant", "momentous", "", "nappy", "nebulous", "nimble", "nippy", "nonchalant", "nondescript", "noxious", "numberless", "", "oafish", "obeisant", "obsequious", "oceanic", "omniscient", "onerous", "optimal", "ossified", "overwrought", "", "paltry", "parched", "parsimonious", "penitent", "perpetual", "picayune", "piquant", "placid", "plucky", "prickly", "probable", "profuse", "psychedelic", "", "quack", "quaint", "quarrelsome", "questionable", "quirky", "quixotic", "quizzical", "", " rabbid", "rambunctious", "rampat", "raspy", "recondite", "resolute", "rhetorical", "ritzy", "ruddy", "", "sable", "sassy", "savory", "scandalous", "scintillating", "sedate", "shaggy", "shrill", "smoggy", "somber", "sordid", "spiffy", "spurious", "squalid", "statuesque", "steadfast", "stupendous", "succinct", "swanky", "sweltering", "", "taboo", "tacit", "tangy", "tawdry", "tedious", "tenuous", "testy", "thundering", "tightfisted", "torpid", "trite", "truculent", "", "ubiquitous", "ultra", "unwieldy", "uppity", "utopian", "utter", "", "vacuous", "vagabond", "vengeful", "venomous", "verdant", "versed", "victorious", "vigorous", "vivacious", "voiceless", "volatile", "voracious", "vulgar", "", "wacky", "waggish", "wakeful", "warlike", "wary", "whimsical", "whispering", "wiggly", "wiry", "wistful", "woebegone", "woozy", "wrathful", "wretched", "wry", "", " xenial", "xenophilic", "", "yummy", "yappy", "yielding", "", "zany", "zazzy", "zealous", "zesty", "zippy", "zoetic", "zoic", "zonked"};

        
        Random random = new Random();
        String text = "";
        text += adjectives[random.nextInt(adjectives.length)] + " ";
        text += adjectives[random.nextInt(adjectives.length)] + " ";
        text += words[random.nextInt(words.length)] + "!";

        return text;
    }
}