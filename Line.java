public class Line {

   private Coordinate c1;
   private Coordinate c2;
   private double slopeX;
   private double slopeY;
   private double slopeZ;
   
   // Constructs a new Line (parametric curve) given two coordinates that goes through both coordinates.
   public Line(Coordinate c1, Coordinate c2) {      
      this.c1 = c1;
      this.c2 = c2;
      this.slopeX = c1.x - c2.x;
      this.slopeY = c1.y - c2.y;
      this.slopeZ = c1.z - c2.z;
      if(c1.z < 0) {
         throw new IllegalArgumentException("The moon is below the horizon.");
      } else if(c2.z <= 0) {
         throw new IllegalArgumentException("The target is too low.");
      }
   }
   
   // Calculates and returns a Coordinate at which the the z-value intersects the given level (height).
   // Does not return a valid longitude/latitude; useful return values for debugging or using x, y, z
   // coordinates rather than longitude/latitude.
   public Coordinate zIntersection(double level) {
      // Checks if the target obscures or doesn't line up with the person's position.
      if(level == c1.z && c1.z != c2.z || c1.z == c2.z && level != c1.z) {
         throw new IllegalArgumentException("There is no possible way to view the moon at the top of the target.");
      } else if(level == c1.z && c1.z == c2.z) {
         System.out.println("The moon can be seen at the top of the target from anywhere at this altitude.");
      }
      // The "time" at which the z-value equals the given level.
      double time = (level - c1.z) / -(c1.z - c2.z);
      // Creates a new Coordinate along the parametric curve at the correct "time".
      return new Coordinate(c1.x - slopeX * time, c1.y - slopeY * time, level);
   }

}
