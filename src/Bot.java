import java.util.Arrays;
import java.util.HashMap;

import lenz.htw.cywwtaip.world.GraphNode;

public class Bot {
    public enum BotType {
        Speedy,
        Ghosty,
        Fatty
    }

    final public BotType type;
    final public int ownerID;
    final float[][] loadingZones = {{0, 0, 1}, {0, 0, -1}, {0, 1, 0}, {0, -1, 0}, {1, 0, 0}, {-1, 0, 0}};

    private Pathfinding pathfindingThread = null;
    private GraphNode[] path;
    
    public float[] position;
    public float[] targetPos;

    public Bot(BotType type, int ownerID) {
        this.type = type;
        this.ownerID = ownerID;
        // targetPos = new float[] {1f, 0f, 0f};
    }

    public float estimateDistanceToNextLoadingZone() {
        float adjustmentFactor = type == BotType.Speedy ? 1.0f : 1.7f; // factor to account for non-direct paths
        return adjustmentFactor * (float) Arrays.stream(loadingZones)
        .mapToDouble((vec) -> MathUtils.distanceOnUnitSphere(vec, position))
        .min().getAsDouble();
    }

    public void goToNextLoadingZone() {
        targetPos = Arrays.stream(loadingZones)
        .min((v1, v2) -> Float.compare(
            MathUtils.distanceOnUnitSphere(v1, position), 
            MathUtils.distanceOnUnitSphere(v2, position)))
        .get();
    }
    
    public float update(GraphNode[] graph, HashMap<GraphNode, Float> extraWeights, float[] direction) {
        // float[] delta = MathUtils.copy(position);
        // if(prevPos == null) prevPos = position;
        // MathUtils.subtract(delta, prevPos);
        // float speed = MathUtils.length(delta);
        // System.out.println("Bot " + type + " speed: " + speed);

        float returnAngle = 0f;

        if(MathUtils.distanceOnUnitSphere(position, targetPos) < 0.3f) {
            targetPos = null;
        }

        // if path is null consume a pathfinding thread
        if(path == null) {
            if(pathfindingThread != null) {
                if(!pathfindingThread.isAlive()) {
                    path = pathfindingThread.resultPath;
                    pathfindingThread = null;
                }
            }
        }

        // if pathfinding thread is null create a new one
        if(pathfindingThread == null && targetPos != null) {
            GraphNode currentNode = path != null? path[path.length - 1] : closestNode(graph, position);
            GraphNode targetNode = closestNode(graph, targetPos);

            // TODO dynamic limit maybe
            pathfindingThread = new Pathfinding(currentNode, targetNode, extraWeights, 2000, type == BotType.Ghosty, ownerID);
            pathfindingThread.start();
        }

        if(path != null) {
            // find the furthest path node that you have reached and remove closer ones
            boolean reached = false;
            GraphNode nextNode = path[path.length - 1];
            for(int i = path.length - 1; i >= 0; i--) {
                if(path[i] == null) 
                continue;
                if(reached) {
                    path[i] = null; 
                    continue;
                }
                
                // check if we are close to this node
                if(MathUtils.distanceOnUnitSphere(new float[] {path[i].x, path[i].y, path[i].z}, position) < 0.1f) {
                    if(i == path.length - 1) {
                        // arrived at end
                        path = null;
                        break;
                    } else {
                        reached = true;
                    }
                } else {
                    nextNode = path[i];
                }
            }
            
            
            if(nextNode != null) {
                returnAngle = lookTowards(position, direction, posFromNode(nextNode));
            }
        }

        if(returnAngle == 0f) {
            // just wiggle around, it's better than going straight
            // System.out.println("wiggle wiggle wiggle");
            returnAngle = (float) Math.random() + 4f;
        }
        
        // prevPos = MathUtils.copy(position);

        return returnAngle;
    }

    private GraphNode closestNode(GraphNode[] graph, float[] position) {
        return Arrays.stream(graph).parallel()
            .min((a, b) -> Float.compare(
                MathUtils.distanceSquared(posFromNode(a), position),
                MathUtils.distanceSquared(posFromNode(b), position)))
            .get();
    }

    private static float lookTowards(float[] position, float[] direction, float[] target) {
        float[] y = MathUtils.copy(position); 
        MathUtils.normalize(y);

        float[] z = MathUtils.cross(target, y);
        MathUtils.normalize(z);

        float[] x = MathUtils.cross(y, z);

        // do the transform baby
        // x1 x2 x3     d1
        // y1 y2 y3  *  d2
        // z1 z2 z3     d3
        float[] dt = {
            direction[0] * x[0] + direction[1] * x[1] + direction[2] * x[2],
            direction[0] * y[0] + direction[1] * y[1] + direction[2] * y[2],
            direction[0] * z[0] + direction[1] * z[1] + direction[2] * z[2]
        };

        float phi = (float) Math.atan2(-dt[2], dt[0]);
        return -phi;
    }

    private static float[] posFromNode(GraphNode g) {
        return new float[] { g.x, g.y, g.z };
    }
}