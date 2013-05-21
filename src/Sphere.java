import java.util.ArrayList;

import javax.xml.soap.Text;

import com.sun.xml.internal.bind.v2.runtime.output.InPlaceDOMOutput;

import processing.core.*;
import sun.awt.image.OffScreenImage;

public class Sphere {

	// ***************************************
	// Global constants.
	// ***************************************
	String DELIMITER_TXT = "~";
	float AMPLITUDE_SCALE_NBR = 2.5f;
	float HGHT_INTERPOLATION_NBR = 0.03f;
	int FROM_CLR_NBR, TO_CLR_NBR;

	// ***************************************
	// Global variables.
	// ***************************************
	PApplet _parApp;
	float[] _latitudeDegs, _longitudeDegs, _radiusLenNbrs, _audioFrameDataVals;
	float _resNbr;
	int _latitudeLnCnt, _longitudeLnCnt;
	float _modelRadiusNbr; //mean radius of the model (so that it fits into the viewport)
	float _zOffsetNbr = 0;
	float _tmNbr = 0;
	int _fillClrNbr = 0;
	float _lerpClrAmt = 0;
	float[][] _musicData;
	String _loadFilePathTxt;
	int _audioFrameCnt, _frameDataCntNbr, _vertexCnt;


	/**
	 * Constructor.
	 * 
	 * @param inpParApp  Parent PApplet.
	 * @param inpResNbr  Resolution of the sphere.
	 * @param inpModelRadiusNbr  Radius of the sphere.
	 * @param inpExtDatFilePathTxt  Path to an external data file for manipulating the sphere.
	 */
	public Sphere(PApplet inpParApp, float inpResNbr, float inpModelRadiusNbr, String inpExtDatFilePathTxt) {
		_parApp = inpParApp;
		_resNbr = inpResNbr;
		_modelRadiusNbr = inpModelRadiusNbr;

		_latitudeLnCnt = (int)(180/_resNbr);
		_longitudeLnCnt = (int)(360/_resNbr);

		// The number of vertices is the number of longitudinal lines multiplied by the number of latitudinal lines minus 1, plus 2 vertices for the poles.
		int vertexCnt = _longitudeLnCnt * (_latitudeLnCnt - 1) + 2;
		_latitudeDegs = new float[vertexCnt];
		_longitudeDegs = new float[vertexCnt];
		_radiusLenNbrs = new float[vertexCnt];

		//createVertices(0);
		crteVertices();

		FROM_CLR_NBR = _parApp.color(144, 29, 31);
		TO_CLR_NBR = _parApp.color(215, 180, 15);

		_loadFilePathTxt = inpExtDatFilePathTxt;

		loadExtData();
	}


	// ***************************************
	// Getters
	// ***************************************
	public int rtrvAudioFrameCnt() {
		return _audioFrameCnt;
	}


	/**
	 * Creates arrays to store data about all our vertices (i.e., latitude, longitude, and distance from center of sphere).
	 */
	public void crteVertices() {

		// For each latitude we'll draw, we'll create a strip of vertices.
		for(int latitudeDegNbr = -90; latitudeDegNbr <= 90; latitudeDegNbr += _resNbr) {

			// If we're at the poles, we will create just one vertex since all the longitudes share the same vertex here.
			if (PApplet.abs(latitudeDegNbr) == 90) {
				int vertexIdx = calcVertexIdx(latitudeDegNbr, 0);
				_latitudeDegs[vertexIdx] = latitudeDegNbr;
				_longitudeDegs[vertexIdx] = 0;
				_radiusLenNbrs[vertexIdx] = _modelRadiusNbr;
			} else {
				for(int longitudeDegNbr = -180; longitudeDegNbr <= 180; longitudeDegNbr += _resNbr) {
					int vertexIdx = calcVertexIdx(latitudeDegNbr, longitudeDegNbr);
					_latitudeDegs[vertexIdx] = latitudeDegNbr;
					_longitudeDegs[vertexIdx] = longitudeDegNbr;
					_radiusLenNbrs[vertexIdx] = _modelRadiusNbr;
				}
			}
		}
	}


	/**
	 * Draws the vertices to the screen.
	 * 
	 * @param inpAudioFrameIdx  The audio frame to reference for height data.
	 */
	public void drawSphere(int inpAudioFrameIdx) {
		_audioFrameDataVals = rtrvAudioFrameData(inpAudioFrameIdx);
		float prevAudioFrameLvlNbr, currAudioFrameLvlNbr;

		if (inpAudioFrameIdx > 0) {
			prevAudioFrameLvlNbr = rtrvAudioFrameData(inpAudioFrameIdx - 1)[0];
		} else {
			prevAudioFrameLvlNbr = 0;
		}

		currAudioFrameLvlNbr = rtrvAudioFrameData(inpAudioFrameIdx)[0];

		//_parApp.noStroke();
		_parApp.stroke(_fillClrNbr);
		_lerpClrAmt = _parApp.frameCount / (float)_musicData.length;
		_fillClrNbr = (int) PApplet.lerpColor(FROM_CLR_NBR, TO_CLR_NBR, _lerpClrAmt, PApplet.RGB);
		_parApp.fill(_fillClrNbr);

		float newVertexHghtNbr = 0, adjacentLatitudeLnLatitudeDegNbr = 0, adjacentLatitudeLnLongitudeDegNbr = 0, adjacentLatitudeLnRadiusLenNbr = 0,
				currLatitudeLnLatitudeDegNbr = 0, currLatitudeLnLongitudeDegNbr = 0;
		PVector adjacentVertex, currVertex;
		int adjacentLatitudeVertexIdx = 0, currLatitudeVertexIdx = 0;

		for (int latitudeDegNbr = -90; latitudeDegNbr <= 90; latitudeDegNbr += _resNbr) {

			if (PApplet.abs(latitudeDegNbr) == 90) {
				// Create Triangle Fans for each of the poles.
				_parApp.beginShape(PApplet.TRIANGLE_FAN);

				// Draw the pole vertex.
				currLatitudeVertexIdx = calcVertexIdx(latitudeDegNbr, 0);
				newVertexHghtNbr = calcAndStoreNewVertexHghtNbr(currLatitudeVertexIdx, prevAudioFrameLvlNbr, currAudioFrameLvlNbr);
				currVertex = Main.toCartesian(latitudeDegNbr, 0, newVertexHghtNbr);
				//_parApp.text("Lat: " + latitudeDegNbr, currVertex.x, currVertex.y, currVertex.z + 20);
				_parApp.vertex(currVertex.x, currVertex.y, currVertex.z);

				float adjacentLatitudeDegNbr;

				if (latitudeDegNbr == -90) {
					adjacentLatitudeDegNbr = latitudeDegNbr + _resNbr;
				} else {
					adjacentLatitudeDegNbr = latitudeDegNbr - _resNbr;
				}

				// In order to complete the sphere, you have to go completely around the circle (i.e., include both -180 and 180).
				for(int longitudeDegNbr = -180; longitudeDegNbr <= 180; longitudeDegNbr += _resNbr) {

					adjacentLatitudeVertexIdx = calcVertexIdx(adjacentLatitudeDegNbr, longitudeDegNbr);

					if (longitudeDegNbr == 180) {
						// When we complete the circle, use the heights that we used for longitude = -180 to stitch the mesh together.
						newVertexHghtNbr = _radiusLenNbrs[calcVertexIdx(adjacentLatitudeDegNbr, -180)];
					} else {
						// If we're building the second pole, we need to stitch it to the previous ring.
						if (adjacentLatitudeDegNbr == latitudeDegNbr - _resNbr) {
							adjacentLatitudeVertexIdx = calcVertexIdx(adjacentLatitudeDegNbr, longitudeDegNbr);
							newVertexHghtNbr = _radiusLenNbrs[adjacentLatitudeVertexIdx];
						} else {
							newVertexHghtNbr = calcAndStoreNewVertexHghtNbr(adjacentLatitudeVertexIdx, prevAudioFrameLvlNbr, currAudioFrameLvlNbr);
						}
					}

					adjacentVertex = Main.toCartesian(adjacentLatitudeDegNbr, longitudeDegNbr, newVertexHghtNbr);
					_parApp.vertex(adjacentVertex.x, adjacentVertex.y, adjacentVertex.z);
				}

				_parApp.endShape();
			} else if (latitudeDegNbr - _resNbr != -90) {
				// We already built a triangle fan for the first pole, so we do not want to build a triangle strip over it.
				_parApp.beginShape(PConstants.TRIANGLE_STRIP);

				// In order to complete the sphere, you have to go completely around the circle (i.e., include both -180 and 180).
				for (int longitudeDegNbr = -180; longitudeDegNbr <= 180; longitudeDegNbr += _resNbr) {

					if (PApplet.abs(latitudeDegNbr) < 90) {
						adjacentLatitudeVertexIdx = calcVertexIdx(latitudeDegNbr - _resNbr, longitudeDegNbr);
						adjacentLatitudeLnLatitudeDegNbr = _latitudeDegs[adjacentLatitudeVertexIdx];
						adjacentLatitudeLnLongitudeDegNbr = _longitudeDegs[adjacentLatitudeVertexIdx];
						adjacentLatitudeLnRadiusLenNbr = _radiusLenNbrs[adjacentLatitudeVertexIdx];

						adjacentVertex = Main.toCartesian(adjacentLatitudeLnLatitudeDegNbr, adjacentLatitudeLnLongitudeDegNbr, adjacentLatitudeLnRadiusLenNbr);				
						_parApp.vertex(adjacentVertex.x, adjacentVertex.y, adjacentVertex.z);
					}

					currLatitudeVertexIdx = calcVertexIdx(latitudeDegNbr, longitudeDegNbr);
					currLatitudeLnLatitudeDegNbr = _latitudeDegs[currLatitudeVertexIdx];
					currLatitudeLnLongitudeDegNbr = _longitudeDegs[currLatitudeVertexIdx];

					if (longitudeDegNbr == 180) {
						// When we complete the circle, use the heights that we used for longitude = -180 to stitch the mesh together.
						newVertexHghtNbr = _radiusLenNbrs[calcVertexIdx(latitudeDegNbr, -180)];
					} else {
						newVertexHghtNbr = calcAndStoreNewVertexHghtNbr(currLatitudeVertexIdx, prevAudioFrameLvlNbr, currAudioFrameLvlNbr);
					}

					currVertex = Main.toCartesian(currLatitudeLnLatitudeDegNbr, currLatitudeLnLongitudeDegNbr, newVertexHghtNbr);
					_parApp.vertex(currVertex.x, currVertex.y, currVertex.z);
				}
				_parApp.endShape();
			}
		}
	}



	// ***************************************
	// Private Methods
	// ***************************************

	/**
	 * Calculates the index of a vertex in our vertex arrays given its latitude and longitude.
	 * 
	 * @param inpLatitudeDegNbr  The latitude of the vertex in degrees.
	 * @param inpLongitudeDegNbr  The longitude of the vertex in degrees.
	 * @return The index of the vertex.
	 */
	private int calcVertexIdx(float inpLatitudeDegNbr, float inpLongitudeDegNbr) {

		int rtnIdx = 0;

		// First, since the domains of latitude and longitude are [-90, 90] and [-180, 180] respectively, we will convert the
		// parameters to the domains [0, 180] and [0, 360].
		inpLatitudeDegNbr += 90;
		inpLongitudeDegNbr += 180;

		// Longitude of 360 degrees is actually just the same as 0 degrees.
		if (inpLongitudeDegNbr == 360) {
			inpLongitudeDegNbr = 0;
		}

		if (inpLatitudeDegNbr == 0) {
			rtnIdx = 0;
		} else if (inpLatitudeDegNbr == 180) {
			rtnIdx = _latitudeDegs.length - 1;
		} else {
			// Subtract the longitudinal line count, because we didn't create these vertices for the poles, 
			// but add 1 for the universal vertex that we did create.
			rtnIdx = (int) (((inpLatitudeDegNbr / _resNbr) * _longitudeLnCnt) + (int)(inpLongitudeDegNbr / _resNbr)) - _longitudeLnCnt + 1;
		}

		return rtnIdx;
	}


	/**
	 * Loads data from external sources.
	 */
	private void loadExtData() {
		String[] audioFrameAnalysisTxt = _parApp.loadStrings(_loadFilePathTxt);
		_audioFrameCnt = audioFrameAnalysisTxt.length;

		_frameDataCntNbr = audioFrameAnalysisTxt[0].split(DELIMITER_TXT).length;
		_musicData = new float[_audioFrameCnt][_frameDataCntNbr];

		for (int audioFrameIdx = 0; audioFrameIdx < _audioFrameCnt - 1; audioFrameIdx++) {
			String bufferTxt = audioFrameAnalysisTxt[audioFrameIdx];
			String[] bufferVals = bufferTxt.split(DELIMITER_TXT);

			for (int bufferIdx = 0; bufferIdx < _frameDataCntNbr; bufferIdx++) {
				_musicData[audioFrameIdx][bufferIdx] = Float.parseFloat(bufferVals[bufferIdx]);
			}
		}
	}


	/**
	 * Retrieves the data associated with a given audio frame.
	 * 
	 * @param inpFrameIdx  Index of the desired audio frame.
	 * @return Array of float values representing the data for this audio frame.
	 */
	private float[] rtrvAudioFrameData(int inpFrameIdx) {
		float[] rtnFrameData = new float[_frameDataCntNbr];

		inpFrameIdx = PApplet.constrain(inpFrameIdx, 0, _audioFrameCnt - 1);

		for (int bufferIdx = 0; bufferIdx < _frameDataCntNbr; bufferIdx++) {
			rtnFrameData[bufferIdx] = _musicData[inpFrameIdx][bufferIdx];
		}

		return rtnFrameData;
	}


	/**
	 * Calculates the new height to assign to a vertex and stores that value in our array of vertex heights.
	 * 
	 * @param inpVertexIdx  The index of this vertex in our arrays.
	 * @param inpPrevAudioFrameLvlNbr  The level number of the previous audio frame.
	 * @param inpCurrAudioFrameLvlNbr  The level number of the current audio frame.
	 * 
	 * @return  The height at which to draw the vertex.
	 */
	private float calcAndStoreNewVertexHghtNbr(int inpVertexIdx, float inpPrevAudioFrameLvlNbr, float inpCurrAudioFrameLvlNbr) {
		float rtnVertexHghtNbr = 0;

		rtnVertexHghtNbr = _modelRadiusNbr + _parApp.random(0, AMPLITUDE_SCALE_NBR * PApplet.lerp(inpPrevAudioFrameLvlNbr, inpCurrAudioFrameLvlNbr, HGHT_INTERPOLATION_NBR));

		_radiusLenNbrs[inpVertexIdx] = rtnVertexHghtNbr;
		return rtnVertexHghtNbr;
	}
}
