package util;

public class ColorUtil {

    public static boolean isGreyscale(int color) {
        return isGreyscale(color, 0);
    }

    public static boolean isGreyscale(int color, int maxGreyscaleDifference) {
        // A perfect Greyscale color would be one where R, G, and B were equal.
        int red = red(color);
        int green = green(color);
        int blue = blue(color);

        int max = Math.max(Math.max(red, green), blue);
        int min = Math.min(Math.min(red, green), blue);

        return (max - min) <= maxGreyscaleDifference;
    }

    /**
     * Return the red component of a color int. This is the same as saying
     * (color >> 16) & 0xFF
     */
    public static int red(int color) {
        return (color >> 16) & 0xFF;
    }

    /**
     * Return the green component of a color int. This is the same as saying
     * (color >> 8) & 0xFF
     */
    public static int green(int color) {
        return (color >> 8) & 0xFF;
    }

    /**
     * Return the blue component of a color int. This is the same as saying
     * color & 0xFF
     */
    public static int blue(int color) {
        return color & 0xFF;
    }
}


