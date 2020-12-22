import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;


public class Moon {
    private ZonedDateTime zdt;
    private double lat;
    private double lon;


    // Constructs a new Moon instance with the given long/lat and current instant datetime
    // Positive longitude is E, negative is W
    public Moon(double latitude, double longitude) {
        this(latitude, longitude, ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("UTC")));
    }


    // Constructs a new Moon instance with the given long/lat and datetime
    // Positive longitude is E, negative is W
    public Moon(double latitude, double longitude, ZonedDateTime zdt) {
        this.lon = longitude;
        this.lat = latitude;
        this.zdt = zdt.withZoneSameInstant(ZoneId.of("UTC"));
    }


    // Returns the position of the moon in a double[] containing ecliptic longitude, latitude, and distance
    private double[] MoonPosition() {
        // Orbital elements of the moon
        double N = res(125.1228 - 0.0529538083 * d());  // Long ascension node (deg)
        double i = res(5.1454);  // Inclination (deg)
        double w = res(318.0634 + 0.1643573223 * d());  // Argument of periapsis (deg)
        double a = res(60.2666);  // Mean distance, or semi-major axis (Earth equatorial radii)
        double e = res(0.054900);  // Eccentricity
        double M = res(115.3654 + 13.0649929509 * d());  // Mean anomaly (deg)

        // Calculate eccentric anomaly - E0 and E used to iterate to reduce error
        double E0 = M + (180 / Math.PI) * e * sin(M) * (1 + e * cos(M));
        double E = E0 - (E0 - (180/Math.PI) * e * sin(E0) - M) / (1 - e * cos(E0));

        // Iterate on E until an acceptably low difference
        while (Math.abs(E - E0) > 0.005) {
            E0 = E;
            E = E0 - (E0 - (180/Math.PI) * e * sin(E0) - M) / (1 - e * cos(E0));
        }

        // Calculate rectangular (x, y) coordinates in the lunar plane
        double x = a * (cos(E) - e);
        double y = a * Math.sqrt(1 - e*e) * sin(E);

        // Convert to distance and true anomaly
        double r = Math.sqrt(x*x + y*y);  // Distance (Earth equatorial radii)
        double v = res(Math.toDegrees(Math.atan2(y, x)));  // True anomaly (deg)

        // Convert to ecliptic coordinates (Earth equatorial radii)
        double xeclip = r * ( cos(N) * cos(v+w) - sin(N) * sin(v+w) * cos(i) );
        double yeclip = r * ( sin(N) * cos(v+w) + cos(N) * sin(v+w) * cos(i) );
        double zeclip = r * sin(v+w) * sin(i);

        // Convert to ecliptic longitude, latitude, and distance
        double lon = res(Math.toDegrees(Math.atan2( yeclip, xeclip )));  // (deg)
        double lat  =  Math.toDegrees(Math.atan2( zeclip, Math.sqrt( xeclip*xeclip + yeclip*yeclip ) ));  // (deg)
        double dist = Math.sqrt( xeclip*xeclip + yeclip*yeclip + zeclip*zeclip );  // (Earth equatorial radii)

        return new double[] {lat, lon, dist};
    }


    // Takes in a double[] containing ecliptic longitude, latitude, and distance, and adjusts for perturbations
    private void Perturbations(double[] position) {
        // Orbital elements of the moon
        double N = res(125.1228 - 0.0529538083 * d());  // Long ascension node (deg)
        double i = res(5.1454);  // Inclination (deg)
        double w = res(318.0634 + 0.1643573223 * d());  // Argument of periapsis (deg)
        double a = res(60.2666);  // Mean distance, or semi-major axis (Earth equatorial radii)
        double e = res(0.054900);  // Eccentricity
        double Mm = res(115.3654 + 13.0649929509 * d());  // Moon mean anomaly (deg)

        // More fundamental components
        double Ls = res(280.460 + (0.9856474 * d()));  // Sun mean longitude (deg)
        double Lm = res(N + w + Mm);  // Moon mean longitude (deg)
        double Ms = res(357.528 + (0.9856003 * d()));  // Sun mean anomaly (deg)
        double D = res(Lm - Ls);  // Moon mean elongation
        double F = res(Lm - N);  // Moon argument of latitude

        // Perturbations in longitude (deg)
        double Lo1 = -1.274 * sin(Mm - (2*D));  // Evection
        double Lo2 = 0.658 * sin(2*D);  // Variation
        double Lo3 = -0.186 * sin(Ms);  // Yearly equation
        double Lo4 = -0.059 * sin(2*Mm - 2*D);
        double Lo5 = -0.057 * sin(Mm - 2*D + Ms);
        double Lo6 = 0.053 * sin(Mm + 2*D);
        double Lo7 = 0.046 * sin(2*D - Ms);
        double Lo8 = 0.041 * sin(Mm - Ms);
        double Lo9 = -0.035 * sin(D);  // Parallactic equation
        double Lo10 = -0.031 * sin(Mm + Ms);
        double Lo11 = -0.015 * sin(2*F - 2*D);
        double Lo12 = 0.011 * sin(Mm - 4*D);

        // Perturbations in latitude (deg)
        double La1 = -0.173 * sin(F - 2*D);
        double La2 = -0.055 * sin(Mm - F - 2*D);
        double La3 = -0.046 * sin(Mm + F - 2*D);
        double La4 = 0.033 * sin(F + 2*D);
        double La5 = 0.017 * sin(2*Mm + F);

        // Perturbations in lunar distance (Earth radii)
        double R1 = -0.58 * cos(Mm - 2*D);
        double R2 = -0.46 * cos(2*D);

        position[0] += (La1 + La2 + La3 + La4 + La5);
        position[1] += (Lo1 + Lo2 + Lo3 + Lo4 + Lo5 + Lo6 + Lo7 + Lo8 + Lo9 + Lo10 + Lo11 + Lo12);
        position[2] += (R1 + R2);
    }


    // Returns a double[] containing Right Ascension and Declination.
    private double[] RaDecl() {
        double[] position = MoonPosition();
        Perturbations(position);
        double oblecl = 23.4393 - (0.0000003563 * d());  // Obliquity of the ecliptic (deg)

        // Convert to rectangular ecliptic coordinates (Earth equatorial radii)
        double xeclip = position[2] * cos(position[1]) * cos(position[0]);  // x ecliptic coordinate
        double yeclip = position[2] * sin(position[1]) * cos(position[0]);  // y ecliptic coordinate
        double zeclip = position[2] * sin(position[0]);  // z ecliptic coordinate

        // Convert to rectangular equatorial coordinates
        double xequat = xeclip;
        double yequat = (yeclip * cos(oblecl)) - (zeclip * sin(oblecl));
        double zequat = (yeclip * sin(oblecl)) + (zeclip * cos(oblecl));

        double RA = res(Math.toDegrees(Math.atan2(yequat, xequat)));
        double Decl = Math.toDegrees(Math.atan2(zequat, Math.sqrt( (xequat*xequat) + (yequat*yequat) )));

        return new double[] {RA, Decl};        
    }


    // Returns a double[] containing location-specific Azimuth and Altitude.
    // Azimuth is in degrees West of True South, and Altitude is in degrees above the horizon.
    public double[] AzAlt() {
        double[] pos = RaDecl();
        double RA = pos[0];
        double Decl = pos[1];
        double SidTime = SiderealTime();
        
        double H = res((SidTime * 15) - RA);  // Hour Angle (deg) measured westward from the South
        double Azimuth = Math.toDegrees(Math.atan2( sin(H), (cos(H) * sin(lat)) - (tan(Decl) * cos(lat)) ));  // Azimuth (deg) measured westward from the South
        double Altitude = Math.toDegrees(Math.asin( (sin(lat) * sin(Decl)) + (cos(lat) * cos(Decl) * cos(H)) ));  // Altitude (deg) measured above the horizon

        return new double[] {Azimuth, Altitude};
    }


    // Helper method: Normalizes degree value to be between 0 and 360 degrees
    private double res(double angle) {
        while (angle < 0) {
            angle += 360;
        }
        while (angle > 360) {
            angle -= 360;
        }

        return angle;
    }


    // Helper method: Returns the Sidereal Time from the stored location and ZDT
    private double SiderealTime() {
        double L = res(280.460 + (0.9856474 * d()));  // Mean longitude of the Sun, corrected for the aberration of light
        double GMST0 = L / 15.0 + 12;  // Sidereal time (hours) at Greenwich 00:00
        double SidTime = GMST0 + UTfromZDT() + lon/15;
        return SidTime;
    }


    // Helper method: Returns the days since Greenwich Noon, 1 January 2000
    private double d() {
        return JulianDate() - 2451543.5;
    }


    // Helper method: Returns the sine of the input in degrees.
    private double sin(double angle) {
        return Math.sin(Math.toRadians(angle));
    }


    // Helper method: Returns the cosine of the input in degrees.
    private double cos(double angle) {
        return Math.cos(Math.toRadians(angle));
    }


    // Helper method: Returns the tangent of the input in degrees.
    private double tan(double angle) {
        return Math.tan(Math.toRadians(angle));
    }

    
    // Helper method: Returns the Julian Date calculated from the stored ZDT
    private double JulianDate() {
        int D = zdt.getDayOfMonth();
        int M = zdt.getMonthValue();
        int Y = zdt.getYear();
        double UT = UTfromZDT();

        double JD = (367 * Y) - (int)(7 * (Y + (int)((M + 9) / 12.0)) / 4.0) 
                    - (int)(3.0 * ((int)((Y + (M - 9) / 7.0) / 100.0) + 1) / 4.0)
                    + (int)(275 * M / 9.0) + D + 1721028.5 + UT/24;
        return JD;
    }


    // Helper method: Returns UT (hours + decimals) from the stored ZDT
    private double UTfromZDT() {
        return zdt.getHour() + (zdt.getMinute() / 60.0) + (zdt.getSecond() / 3600.0);
    }


    public static void main(String[] args) {
        // For testing \/
        Moon moon = new Moon(47, -122);  // Latutde & Longitude for Lander Hall
        double[] RaDecl = moon.RaDecl();
        double[] AzAlt = moon.AzAlt();
    }
}

