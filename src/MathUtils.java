import lenz.htw.cywwtaip.world.GraphNode;

public class MathUtils {
    public static float distanceSquared(float[] a, float[] b) {
        float sum = 0;
        for(int i = 0; i < 3; i++) {
            float d = (a[i] - b[i]);
            sum += d * d;
        }
        return sum;
    }

    public static void subtract(float[] a, float[] b) {
        for(int i = 0; i < 3; i++) {
            a[i] -= b[i];
        }
    }

    public static float[] copy(float[] orig) {
        return new float[] {orig[0], orig[1], orig[2]};
    }

    public static void normalize(float[] vec) {
        scale(vec, 1f / length(vec));
    }

    public static float length(float[] vec) {
        return (float)Math.sqrt(vec[0]*vec[0] + vec[1]*vec[1] + vec[2]*vec[2]);
    }

    public static void scale(float[] vec, float scale) {
        for(int i = 0; i < 3; i++) {
            vec[i] *= scale;
        }
    }

    public static float[] cross(float[] a, float[] b) {
        float[] result = new float[3];
        for(int i = 0; i < 3; i++) {
            result[i] = a[(i+1)%3] * b[(i+2)%3] - a[(i+2)%3] * b[(i+1)%3];
        }
        return result;
    }

    public static float distanceOnUnitSphere(float[] a, float[] b) {
        return (float) Math.acos(a[0] * b[0] + a[1] * b[1] + a[2] * b[2]);
    }

    public static float distanceOnUnitSphere(GraphNode a, GraphNode b) {
        return (float) Math.acos(a.x * b.x + a.y * b.y + a.z * b.z); // distance on unit sphere
    }

    public static float distanceOnUnitSphere(GraphNode a, float[] b) {
        return (float) Math.acos(a.x * b[0] + a.y * b[1] + a.z * b[2]); // distance on unit sphere
    }

    public static float[] randomPointOnUnitSphere() {
        float[] vec = new float[] {(float) Math.random() * 2f - 1f, (float) Math.random() * 2f - 1f, (float) Math.random() * 2f - 1f};
        MathUtils.normalize(vec);
        return vec;
    }

    public static void main(String[] args) {
        // float[] test = cross(new float[] {-1, 0, 0}, new float[] {0, 0, 1});
        // System.out.println(Arrays.deepToString(test));
    }
}