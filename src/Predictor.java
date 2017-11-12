////////////////////////////////////////////////////////////
// Predictor.java
//
// Terry Baume, 2009-2010
// terry@bogaurd.net
//
// Mark Jessop 2010-2017
// vk5qi@rfhead.net
//
// This class is responsible for calculating predictions 
// via the external CUSF predictor binary and rendering
// a prediction track in OziExplorer
//
// Updated in 2016-12 to add abort predictions.
// - Mark Jessop
//
// Updated 2017-02 to add MASSIVE speedups to prediction plotting,
// by using track files.
// - Mark Jessop
//
////////////////////////////////////////////////////////////

import java.io.*;
import java.lang.Math;
import java.util.Calendar;
import java.util.TimeZone;
import com.oziexplorer.*;

public class Predictor {

	// Computed variables
	double currentAscentRate;
	boolean descent;
	
	// Instance variables
	int currentAltitude;
	double currentLatitude;
	double currentLongitude;
	int currentHour;
	int currentMinute;
	int currentSecond;
	
	// Configuration variables
	double defaultAscentRate;
	double defaultDescentRate;
	int burstAltitude;
	int currentTrack;
	int trackNumber;
	int trackWidth;
	int trackColour;
	
	// Abort Track
	int currentAbortTrack;
	int abortTrackNumber;
	int abortTrackColour = 255; // Solid Red.
	
	// What to execute when running the predictor (wtf windows paths)
	String command = "pred.exe -i ..\\gfs";
	
	String prediction_file = System.getProperty("user.dir") + File.separator + "predict.plt";
	String abort_file = System.getProperty("user.dir") + File.separator + "abort.plt";	

	//
	// Constructor
	// Pass in all default (configured) values
	//
	public Predictor(double asc, double desc, int burst, int track, int colour, int width) {
		defaultAscentRate = asc;
		defaultDescentRate = desc;
		burstAltitude = burst;
		trackNumber = track;
		currentTrack = trackNumber;
		trackWidth = width;
		trackColour = colour;
		
		// Abort Track Additions
		abortTrackNumber = track + 2;
		currentAbortTrack = abortTrackNumber;
		
	}
	
	//
	// Update this object
	//
	public void update(double lat, double lon, int alt, int hour, int minute, int second) {
		
		// Work out the times
		int time = (hour * 3600) + (minute * 60) + second;
		int lastTime = (currentHour * 3600) + (currentMinute * 60) + currentSecond;
		
		// Compute asc rate
		currentAscentRate = Math.round((float)(alt - currentAltitude)/(time - lastTime) * 10.0)/10.0;
		
		// Have we entered descent mode?
		if (alt < currentAltitude - 100) descent = true;
		
		// Allow exit from descent mode, if we are in fact still rising.
		if (alt > currentAltitude+5) descent = false;
		
		// Update object
		currentAltitude = alt;
		currentLatitude = lat;
		currentLongitude = lon;
		currentHour = hour;
		currentMinute = minute;
		currentSecond = second;	
		
		System.out.println("\nPredictor updated...");
	}
	
	//
	// Run & plot predictions
	//
	public void runPredictions() throws Exception {
		
		// Internal variables
		String outputLine;
		boolean drawnBurstWaypoint = false;
		double pointLat = 0, pointLon = 0, pointAlt = 0, lastPointAlt = 0;
		
		// Default asc/desc
		double descRate = defaultDescentRate;
		double ascRate = defaultDescentRate;
		
		// Work out the date etc
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		//Calendar cal_Two = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		int day = cal.get(Calendar.DATE);
		int month = cal.get(Calendar.MONTH) + 1;
		int year = cal.get(Calendar.YEAR);
		
		// Work out if we should use the default asc rate,
		// the asc rate fed in, or if we need to calculate a desc rate
		if (descent) {
		
			// Payload is falling,
			// calculate sea level desc rate
			descRate = Math.round(Atmosphere.seaLevelDescentRate(currentAscentRate, currentAltitude)*10.0)/10.0;
			
			// Don't bother running a prediction if desc rate is slower than -1 m/s, we must have landed
			if (descRate < 1) return;
			
			// Switch to descent mode
			//command = command + " -d";
			
		} else if (currentAscentRate <= 0.5) {	
			// This is unlikely to occur (probably not launched yet),
			// use the default asc rate as per config
			ascRate = defaultAscentRate;
		} else if (currentAscentRate > 0.5) {
			// This seems like a sensible ascent rate, lets use it
			ascRate = currentAscentRate;
		}
	
		// Fire up the predictor binary
		Process predictor;
		if (descent) {
			predictor = Runtime.getRuntime().exec(command + " -d"); 
		} else {
			predictor = Runtime.getRuntime().exec(command);
		}

		// We need to handle STDOUT and STDERR in their own threads to avoid deadlocking
		// as BufferedReader calls are blocking - we use InputStreamHandler to do this

		// STDOUT
		StringBuffer inBuffer = new StringBuffer();
		InputStream inStream = predictor.getInputStream();
		new InputStreamHandler(inBuffer, inStream);

		// STDERR
		StringBuffer errBuffer = new StringBuffer();
		InputStream errStream = predictor.getErrorStream();
		new InputStreamHandler(errBuffer, errStream);
		
		// Some debug output
		if (descent) {
			System.out.println("\nRunning predictor (descent mode):");
		} else {
			System.out.println("\nRunning predictor:");
		}
		System.out.println(" * Latitude: " + currentLatitude);
		System.out.println(" * Longitude: " + currentLongitude);
		System.out.println(" * Altitude: " + currentAltitude + " m");
		if (!descent) System.out.println(" * Ascent rate: " + ascRate + " m/s");
		System.out.println(" * Descent rate: " + descRate  + " m/s");
		if (!descent) System.out.println(" * Burst altitude: " + burstAltitude + " m");
		System.out.println(" * Time: " + String.format("%02d", currentHour) + ":" + String.format("%02d", currentMinute) + " " + day + "/" + month + "/" + year);
		
		// Feed in the scenario
		OutputStreamWriter predictorInput = new OutputStreamWriter(predictor.getOutputStream());
		
		predictorInput.write("[launch-site]\n");
		predictorInput.write("latitude = " + currentLatitude + "\n");
		predictorInput.write("altitude = " + currentAltitude + "\n");
		predictorInput.write("longitude =" + currentLongitude + "\n");
		
		predictorInput.write("[atmosphere]\n");
		predictorInput.write("wind-error = 0\n");
		predictorInput.write("[altitude-model]\n");
		
		predictorInput.write("ascent-rate = " + ascRate + "\n");
		predictorInput.write("descent-rate = " + descRate + "\n");
		predictorInput.write("burst-altitude = " + burstAltitude + "\n");
		
		predictorInput.write("[launch-time]\n");
		predictorInput.write("hour = " + currentHour + "\n");
		predictorInput.write("month = " + month + "\n");
		predictorInput.write("second = 0\n");
		predictorInput.write("year = " + year + "\n");
		predictorInput.write("day = " + day + "\n");
		predictorInput.write("minute = " + currentMinute + "\n");
		
		predictorInput.flush();
		predictorInput.close();

		// Let the predictor run its course
		predictor.waitFor();
		System.out.println("Predictor Finished. Writing Oziexplorer Track.");
		
		// Clear any previous predictions & reset the track, delete waypoints from previous predictions
		OziAPI.deleteWaypoint("BURST");
		OziAPI.deleteWaypoint("LANDING");
		
		
		TrackFile predict_track = new TrackFile();
		
		predict_track.setTrackDesc("OziPlotter prediction track");
		predict_track.setTrackWidth(trackWidth);
		predict_track.setTrackType(TrackType.LINE);
		predict_track.setTrackColor(trackColour); 
		
		// Plot our current position
		predict_track.add(new TrackPoint(false,new LatLon(currentLatitude, currentLongitude), currentAltitude));

		// Read the datapoints from the predictor
		BufferedReader predictorOutput = new BufferedReader(new StringReader(inBuffer.toString()));
		while ((outputLine = predictorOutput.readLine ()) != null) {
			
			// Try to process the output as valid coords
			try {
				String[] pointData = outputLine.split(",");
				pointAlt = Double.parseDouble(pointData[3]);
				
				// Check if we've passed the highest point, if so plot a way point
				if (pointAlt < lastPointAlt && !descent && !drawnBurstWaypoint) {
						drawnBurstWaypoint = true;
						OziAPI.addWaypoint(new Waypoint("BURST", 0, new LatLon(pointLat, pointLon)));
				}
				
				pointLat = Double.parseDouble(pointData[1]);
				pointLon = Double.parseDouble(pointData[2]);
				predict_track.add(new TrackPoint(false,new LatLon(pointLat, pointLon), pointAlt));
				lastPointAlt = pointAlt;
				
			} catch (Exception e) {
				// We don't care if we got a bad point
			}
		}
		  
		// Plot a point for the landing & tidy up
		OziAPI.addWaypoint(new Waypoint("LANDING", 0, new LatLon(pointLat, pointLon)));
		predictorOutput.close();
		predict_track.write(prediction_file);
		//predict_track.close();
		OziAPI.loadTrackFile(currentTrack,prediction_file);
		OziAPI.showTrack(currentTrack);
		
		// Switch our current track to the alternate track number (the old track), and clear it ready for the next prediction.
		if(currentTrack == trackNumber){
			currentTrack = trackNumber + 1;
		}else{
			currentTrack = trackNumber;
		}
		OziAPI.clearTrack(currentTrack);
		
		OziAPI.refreshMap();
		System.out.println("Oziexplorer Updated!");
		
		if( (!descent) && (currentAltitude<burstAltitude)){
			runAbortPredictions();
		}else{
			OziAPI.deleteWaypoint("ABORT");
			OziAPI.clearTrack(abortTrackNumber);
			OziAPI.clearTrack(abortTrackNumber+1);
			
		}
	}
		
	//
	// Run and plot Abort Track Prediction.
	//
	public void runAbortPredictions() throws Exception {
		
		// Internal variables
		String outputLine;
		boolean drawnBurstWaypoint = false;
		double pointLat = 0, pointLon = 0, pointAlt = 0, lastPointAlt = 0;
		double lastPointLat = 0, lastPointLon=0;
		
		
		// Default asc/desc
		double descRate = defaultDescentRate;
		double ascRate = defaultDescentRate;
		
		// Work out the date etc
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		//Calendar cal_Two = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		int day = cal.get(Calendar.DATE);
		int month = cal.get(Calendar.MONTH) + 1;
		int year = cal.get(Calendar.YEAR);
		
		// Work out if we should use the default asc rate,
		// the asc rate fed in, or if we need to calculate a desc rate
		if (descent) {
			// If we're in descent, we shouldn't be running anyway.
			return;
			
		} else if (currentAscentRate <= 0.5) {	
			// This is unlikely to occur (probably not launched yet),
			// use the default asc rate as per config
			ascRate = defaultAscentRate;
		} else if (currentAscentRate > 0.5) {
			// This seems like a sensible ascent rate, lets use it
			ascRate = currentAscentRate;
		}
		
		// Set the abort track burst altitude to the current altitude plus 20 seconds of rise time.
		int abortBurstAltitude = currentAltitude + (int)(60*ascRate);
	
		// Fire up the predictor binary
		Process predictor;
		if (descent) {
			predictor = Runtime.getRuntime().exec(command + " -d"); 
		} else {
			predictor = Runtime.getRuntime().exec(command);
		}

		// We need to handle STDOUT and STDERR in their own threads to avoid deadlocking
		// as BufferedReader calls are blocking - we use InputStreamHandler to do this

		// STDOUT
		StringBuffer inBuffer = new StringBuffer();
		InputStream inStream = predictor.getInputStream();
		new InputStreamHandler(inBuffer, inStream);

		// STDERR
		StringBuffer errBuffer = new StringBuffer();
		InputStream errStream = predictor.getErrorStream();
		new InputStreamHandler(errBuffer, errStream);
		
		// Some debug output
		System.out.println("\nRunning predictor for Abort Track.");
		
		System.out.println(" * Latitude: " + currentLatitude);
		System.out.println(" * Longitude: " + currentLongitude);
		System.out.println(" * Altitude: " + currentAltitude + " m");
		if (!descent) System.out.println(" * Ascent rate: " + ascRate + " m/s");
		System.out.println(" * Descent rate: " + descRate  + " m/s");
		if (!descent) System.out.println(" * Burst altitude: " + abortBurstAltitude + " m");
		System.out.println(" * Time: " + String.format("%02d", currentHour) + ":" + String.format("%02d", currentMinute) + " " + day + "/" + month + "/" + year);
		
		// Feed in the scenario
		OutputStreamWriter predictorInput = new OutputStreamWriter(predictor.getOutputStream());
		
		predictorInput.write("[launch-site]\n");
		predictorInput.write("latitude = " + currentLatitude + "\n");
		predictorInput.write("altitude = " + currentAltitude + "\n");
		predictorInput.write("longitude =" + currentLongitude + "\n");
		
		predictorInput.write("[atmosphere]\n");
		predictorInput.write("wind-error = 0\n");
		predictorInput.write("[altitude-model]\n");
		
		predictorInput.write("ascent-rate = " + ascRate + "\n");
		predictorInput.write("descent-rate = " + descRate + "\n");
		predictorInput.write("burst-altitude = " + abortBurstAltitude + "\n");
		
		predictorInput.write("[launch-time]\n");
		predictorInput.write("hour = " + currentHour + "\n");
		predictorInput.write("month = " + month + "\n");
		predictorInput.write("second = 0\n");
		predictorInput.write("year = " + year + "\n");
		predictorInput.write("day = " + day + "\n");
		predictorInput.write("minute = " + currentMinute + "\n");
		
		predictorInput.flush();
		predictorInput.close();

		// Let the predictor run its course
		predictor.waitFor();
		System.out.println("Predictor Finished. Writing Abort Track.");
		
		// Clear any previous predictions & reset the track, delete waypoints from previous predictions
		OziAPI.deleteWaypoint("ABORT");
		
		TrackFile abort_track = new TrackFile();
		
		abort_track.setTrackDesc("OziPlotter Abort prediction track");
		abort_track.setTrackWidth(trackWidth);
		abort_track.setTrackType(TrackType.LINE);
		abort_track.setTrackColor(abortTrackColour); 
		
		// Plot our current position
		abort_track.add(new TrackPoint(false,new LatLon(currentLatitude, currentLongitude), currentAltitude));

		// Read the datapoints from the predictor
		BufferedReader predictorOutput = new BufferedReader(new StringReader(inBuffer.toString()));
		while ((outputLine = predictorOutput.readLine ()) != null) {
			
			// Try to process the output as valid coords
			try {
				String[] pointData = outputLine.split(",");
				pointAlt = Double.parseDouble(pointData[3]);
				
				// Check if we've passed the highest point, if so plot a way point
				if (pointAlt < lastPointAlt && !descent && !drawnBurstWaypoint) {
						drawnBurstWaypoint = true;
						abort_track.add(new TrackPoint(false, new LatLon(lastPointLat, lastPointLon), lastPointAlt));
						//OziAPI.addWaypoint(new Waypoint("", 0, new LatLon(pointLat, pointLon)));
				}
				pointLat = Double.parseDouble(pointData[1]);
				pointLon = Double.parseDouble(pointData[2]);
				if(drawnBurstWaypoint){
					abort_track.add(new TrackPoint(false, new LatLon(pointLat, pointLon), pointAlt));
				}
				lastPointAlt = pointAlt;
				lastPointLon = pointLon;
				lastPointLat = pointLat;
				
			} catch (Exception e) {
				// We don't care if we got a bad point
			}
		}
		  
		// Plot a point for the landing & tidy up
		OziAPI.addWaypoint(new Waypoint("ABORT", 0, new LatLon(pointLat, pointLon)));
		predictorOutput.close();
		abort_track.write(abort_file);
		//predict_track.close();
		OziAPI.loadTrackFile(currentAbortTrack,abort_file);
		OziAPI.showTrack(currentAbortTrack);
		
		// Switch our current track to the alternate track number (the old track), and clear it ready for the next prediction.
		if(currentAbortTrack == abortTrackNumber){
			currentAbortTrack = abortTrackNumber + 1;
		}else{
			currentAbortTrack = abortTrackNumber;
		}
		OziAPI.clearTrack(currentAbortTrack);
		
		OziAPI.refreshMap();
		System.out.println("Oziexplorer Updated!");
	}		

	//
	// Check if a process is still running
	//
	public boolean isAlive(Process p) {
		try	{
			p.exitValue();
			return false;
		} catch (IllegalThreadStateException e) {
			return true;
		}
	}

}