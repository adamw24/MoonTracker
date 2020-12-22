public class Coordinate {
   
   public final double x;
   public final double y;
   public final double z;
   
   // Creates a new Coordinate given an x, y, and z.
   public Coordinate(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }
   
   // Creates a new Coordinate given two degrees, one for longitude and one for latitude.
   public Coordinate(double latitude, double longitude) {
      // https://math.stackexchange.com/questions/989900/calculate-x-y-z-from-two-specific-degrees-on-a-sphere
      this(Math.cos(longitude) * Math.sin(latitude), Math.cos(longitude) * Math.cos(latitude), Math.sin(longitude));
   }
   
   // Returns a string representation of the Coordinate.
   public String toString() {
      return "(" + x + ", " + y + ", " + z + ")";
   }
   
}
