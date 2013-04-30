import processing.core.*;

public class LatitudeStrip {

	PApplet parent;
	float latitude;
	PVector[] vertices;
	
	public LatitudeStrip(PApplet p, PVector newVertex) {
		this.parent = p;
		this.latitude = newVertex.x;
		vertices = new PVector[1];
		vertices[0] = newVertex;
	}
	
	public void addPointToVertices(PVector v) {
		this.vertices = (PVector[]) PApplet.append(this.vertices, v);
	}
}