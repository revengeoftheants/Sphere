import processing.core.*;

public class Crater {
	PApplet parent;
	private String name;
	private double latitude, longitude, craterRadius, floorDiam, rimToFloorDepth, depth, rimFlankWidth, avgModelRadiusPxlNbr;
	private int ageClass;
	private PVector cartesianCoordAtAvgModelRadius;
	
	public PVector[] craterRim;
	public PVector[] craterRimFlank;
	public PVector[] craterFloor;
	//private PVector center;

	public Crater(PApplet p, String name, double latitudeValue, double longitudeValue, double craterRadiusValue, double floorDiamValue, double rimToFloorDepthValue, double depthValue, double rimFlankWidthValue, int ageClass, double modelRadiusPxlNbr) {
		this.parent = p;
		
		this.name = name;
		this.latitude = latitudeValue;
		this.longitude = longitudeValue;
		this.craterRadius = craterRadiusValue;
		this.floorDiam = floorDiamValue;
		this.rimToFloorDepth = rimToFloorDepthValue;
		this.depth = depthValue;
		this.rimFlankWidth = rimFlankWidthValue;
		this.ageClass = ageClass;
		this.avgModelRadiusPxlNbr = modelRadiusPxlNbr;
		
		
		this.cartesianCoordAtAvgModelRadius = Main.toCartesian((float)latitude, (float)longitude, (float)avgModelRadiusPxlNbr);
	} 
	
	public String getName() {
		return this.name;
	}
	
	public double getLatitude() {
		return this.latitude;
	}
	
	public double getLongitude() {
		return this.longitude;
	}
	
	public double getRadius() {
		return this.craterRadius;
	}
	
	public double getDepth() {
		return this.depth;
	}
	
	public double getAgeClass() {
		return this.ageClass;
	}
	
	public void createNewRim(int numOfPoints) {
		craterRim = new PVector[numOfPoints];
		craterRimFlank = new PVector[numOfPoints];
		craterFloor = new PVector[numOfPoints];
	}
	
	public PVector getRimPoint(float angle, float moonRadius) {
		float rad = mapToScreen((float)this.craterRadius);
		double x = this.longitude + rad * PApplet.cos(angle);
		double y = this.latitude + rad * PApplet.sin(angle);
		double z = mapToScreen((float)(moonRadius + (this.rimToFloorDepth - this.depth)));
		x = round(x, 1);
		y = round(y, 1);
		z = round(z, 1);
		//PVector rimPoint = new PVector((float) x, (float) y, (float) z);
		PVector rimPoint = toCartesian((float) x, (float) y, (float) z);
		return rimPoint;
	}
	
	public PVector getRimFlankPoint(float angle, float moonRadius) {
		float rad = mapToScreen((float) (this.craterRadius + this.rimFlankWidth));
		double x = this.latitude + rad * PApplet.cos(angle);
		double y = this.longitude + rad * PApplet.sin(angle);
		double z = mapToScreen(moonRadius);
		x = round(x, 1);
		y = round(y, 1);
		z = round(z, 1);
		//PVector rimFlankPoint = new PVector((float) x, (float) y, (float) z);
		PVector rimFlankPoint = toCartesian((float) x, (float) y, (float) z);
		return rimFlankPoint;
	}
	
	public PVector getFloorPoint(float angle, float moonRadius) {
		float rad = mapToScreen((float) (this.floorDiam / 2));
		double x = this.latitude + rad * PApplet.cos(angle);
		double y = this.longitude + rad * PApplet.sin(angle);
		double z = moonRadius - this.rimToFloorDepth - this.depth;
		x = round(x, 1);
		y = round(y, 1);
		z = round(z, 1);
		PVector floorPoint = new PVector((float) x, (float) y, (float) z);
		return floorPoint;
	}
	
	public boolean isPointInsideCrater(PVector v) {
		double distFromCenter = cartesianCoordAtAvgModelRadius.dist(v);

		boolean insideCrater;
		if(distFromCenter < this.craterRadius + this.rimFlankWidth) {
			insideCrater = true;
		} else {
			insideCrater = false;
		}
		return insideCrater;
	}
	
	public float mapToScreen(float initRadius) { //a function for mapping the radius of each point of the sphere from kilometers to screen pixels
		float screenRadius = PApplet.map(initRadius, 0, 1737, 0, 300);
		return screenRadius;
	}
	
	public PVector toCartesian(float lat, float lng, float elev){ //transform from spherical coordinates to Cartesian (rectilinear) coordinates

		double x = elev * Math.cos(Math.toRadians(lng)) * Math.sin(Math.toRadians(lat));
		double y = elev * Math.sin(Math.toRadians(lng)) * Math.sin(Math.toRadians(lat));
		double z = elev * Math.cos(Math.toRadians(lat));

		PVector v = new PVector((float) x,(float) y,(float) z);
		return v;
	}
	
	public static double round(double value, int places) { //got off StackOverflow--may need to rewrite
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}
	
	@Override
	public String toString() {
		return "Latitude: " + this.getLatitude() + " Longitude: " + this.getLongitude() + " Depth: " + this.getDepth();
	}
}
