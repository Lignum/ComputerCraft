package dan200.computercraft.shared.util;

public class ArrayUtil
{
    public static float[] doubleToFloatArray(double[] ds)
    {
        float[] fs = new float[ds.length];

        for( int i = 0; i < fs.length; ++i )
        {
            fs[i] = (float)ds[i];
        }

        return fs;
    }
}
