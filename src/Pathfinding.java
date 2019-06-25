import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import lenz.htw.cywwtaip.world.GraphNode;

public class Pathfinding extends Thread {
    private GraphNode start;
    private GraphNode goal;
    private HashMap<GraphNode, Float> extraWeights;
    private long msLimit;
    private boolean ignoreWalls;
    private int ownerID;

    public GraphNode[] resultPath = null;

    public Pathfinding(GraphNode start, GraphNode goal, HashMap<GraphNode, Float> extraWeights, long msLimit, boolean ignoreWalls, int ownerID) {
        this.start = start;
        this.goal = goal;
        this.extraWeights = extraWeights;
        this.msLimit = msLimit;
        this.ignoreWalls = ignoreWalls;
        this.ownerID = ownerID;
    }

    public void run() {
        // for the limit
        long t0 = System.nanoTime();
        long nsLimit = msLimit * 1000000;

        // The set of nodes already evaluated
        HashSet<GraphNode> closedSet = new HashSet<>();
    
        // The set of currently discovered nodes that are not evaluated yet.
        // Initially, only the start node is known.
        HashSet<GraphNode> openSet = new HashSet<>();
        openSet.add(start);
    
        // For each node, which node it can most efficiently be reached from.
        // If a node can be reached from many nodes, cameFrom will eventually contain the
        // most efficient previous step.
        HashMap<GraphNode, GraphNode> cameFrom = new HashMap<>();
    
        // For each node, the cost of getting from the start node to that node.
        HashMap<GraphNode, Float> gScore = new HashMap<>();
    
        // The cost of going from start to start is zero.
        gScore.put(start, 0f);
    
        // For each node, the total cost of getting from the start node to the goal
        // by passing by that node. That value is partly known, partly heuristic.
        HashMap<GraphNode, Float> fScore = new HashMap<>();
    
        // For the first node, that value is completely heuristic.
        fScore.put(start, heuristic_cost_estimate(start, goal));
    
        while(!openSet.isEmpty()) {
            if(System.nanoTime() - t0 > nsLimit) {
                // abort search
                // return path to node closest to the goal
                GraphNode best = closedSet.stream().min((a, b) -> Float.compare(
                        heuristic_cost_estimate(a, goal), 
                        heuristic_cost_estimate(b, goal)
                    )).get();

                resultPath = reconstruct_path(cameFrom, best);
                return;
            }

            // current := the node in openSet having the lowest fScore[] value
            GraphNode current = Collections.min(openSet, (a, b) -> Float.compare(
                fScore.getOrDefault(a, Float.POSITIVE_INFINITY), 
                fScore.getOrDefault(b, Float.POSITIVE_INFINITY)));
            if (current == goal) {
                resultPath = reconstruct_path(cameFrom, current);
                return;
            }
            
            openSet.remove(current);
            closedSet.add(current);
            
            // for each neighbor of current
            for (GraphNode neighbor : current.neighbors) {
                if(neighbor.blocked && !ignoreWalls) continue;

                if(closedSet.contains(neighbor))
                    continue;		// Ignore the neighbor which is already evaluated.
                
                if (!openSet.contains(neighbor)) // Discover a new node
                    openSet.add(neighbor);
                
                // The distance from start to a neighbor
                float cost = heuristic_cost_estimate(neighbor, current) // identical to the estimate for direct neighbors
                    + (ignoreWalls? 0f : extraWeights.getOrDefault(neighbor, 0f)) // some extra costs that are assigned to nodes
                ;

                // see how many points we would get and reduce cost accordingly
                float points = (neighbor.owner == 0? 1f: (neighbor.owner == ownerID? 0f : 1.5f));
                cost -= points * 0.02f;

                float tentative_gScore = gScore.getOrDefault(current, Float.POSITIVE_INFINITY) + cost;
                if (tentative_gScore >= gScore.getOrDefault(neighbor, Float.POSITIVE_INFINITY))
                    continue;		// This is not a better path.
                
                // This path is the best until now. Record it!
                cameFrom.put(neighbor, current);
                gScore.put(neighbor, tentative_gScore);
                fScore.put(neighbor, gScore.getOrDefault(neighbor, Float.POSITIVE_INFINITY) + heuristic_cost_estimate(neighbor, goal));
            }
        }
            
        resultPath = null;
        return;
    }

    private static float heuristic_cost_estimate(GraphNode a, GraphNode b) {
        return MathUtils.distanceOnUnitSphere(a, b); // distance on unit sphere
    }

    private static GraphNode[] reconstruct_path(HashMap<GraphNode, GraphNode> cameFrom, GraphNode current) {
        ArrayList<GraphNode> total_path = new ArrayList<>();
        total_path.add(current);

        while(cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            total_path.add(current);
        }

        Collections.reverse(total_path);

        GraphNode[] nodeArray = new GraphNode[total_path.size()];

        return total_path.toArray(nodeArray);
    }
}