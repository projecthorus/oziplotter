////////////////////////////////////////////////////////////
// OziPlotter
//
// Terry Baume, 2009-2010
// terry@bogaurd.net
//
// Mark Jessop 2010-2017
// vk5qi@rfhead.net
//
// Developed for use in Project Horus (http://www.projecthorus.org)
// This software is designed to accept telemetry from various sources
// and plot the resultant coordinates in OziExplorer,
// for live tracking not dependant on an internet connection.
//
// 2017-11-11 First Public Release
//
////////////////////////////////////////////////////////////

import com.oziexplorer.*;
import java.io.*;
import javax.swing.JFileChooser;
import java.net.*;
import java.lang.Math;
import java.util.Properties;


public class OziPlotterUDP {

	double version = 0.98;
	
	// Configuration variables
	String hostname, balloonCallsign;
	DatagramSocket udpSource;
	double predictionAsc, predictionDesc, launchLat, launchLon;
	int port, balloonTrack, trackWidth, balloonColour;
	int predictionTrack, predictionColour, predictionBurst, predictionFrequency, predictionAverage;

	int udpTimeout = 5;

	//
	// Entry point, config is loaded here
	//
	public static void main(String[] args) throws Exception {
		Properties configFile = new Properties();
		try {
			configFile.load(new FileInputStream("../OziPlotter.conf"));
		} catch (Exception e) {
			System.err.println("Failed to load config file!");
			System.exit(1);
		}
			OziPlotterUDP o = new OziPlotterUDP(configFile);
			o.run();
	}
		
	//
	// Constructor
	// Loads the configuration variables
	//
	public OziPlotterUDP(Properties configFile) {	
		hostname = configFile.getProperty("HOSTNAME");
		port = Integer.parseInt(configFile.getProperty("PORT"));
		balloonTrack = Integer.parseInt(configFile.getProperty("PAYLOADTRACK"));
		predictionTrack = Integer.parseInt(configFile.getProperty("PREDTRACK"));
		trackWidth = Integer.parseInt(configFile.getProperty("TRACKWIDTH"));
		balloonCallsign = "PAYLOAD";
		balloonColour = Integer.parseInt(configFile.getProperty("PAYLOADCOLOUR"), 16);
		predictionColour = Integer.parseInt(configFile.getProperty("PREDCOLOUR"), 16);
		predictionFrequency = Integer.parseInt(configFile.getProperty("PREDFREQ"));
		predictionAverage = Integer.parseInt(configFile.getProperty("PREDAVERAGE"));
		predictionAsc = Double.parseDouble(configFile.getProperty("PREDASC"));
		predictionDesc = Double.parseDouble(configFile.getProperty("PREDDESC"));
		predictionBurst = Integer.parseInt(configFile.getProperty("PREDBURST"));
		launchLat = Double.parseDouble(configFile.getProperty("LAUNCHLAT"));
		launchLon = Double.parseDouble(configFile.getProperty("LAUNCHLON"));
	}
	
	//
	// Run method
	// This is where everything happens
	//
	public void run() throws Exception {
	
		// Degree character
		char deg = 248;
		
		// Tracking vars
		int lastAlt = 0;
		int lastTime = 0;
		int lastPredUpdate = 0;
		int lastPredTime = 0;
		boolean descent = false;

		int telemetryTimer = 0;

		
		// Instantiate predictor with config
		Predictor predictor = new Predictor(predictionAsc, predictionDesc, predictionBurst, predictionTrack, predictionColour, trackWidth);
   
		// Startup
		System.out.println("OziPlotter " + version + " Terry Baume & Mark Jessop, 2010-2017\r\n");
		System.out.println("Project Horus (http://www.projecthorus.org)\r\n");
               
        // Look for OziExplorer
        System.out.println("Looking for OziExplorer...");
        if (!OziAPI.findOzi()) {
			System.out.println("OziExplorer not detected, please start OziExplorer first!");
			System.exit(0);
		} else {
			System.out.println("OziExplorer " + OziAPI.getOziVersion() + " found!\r\n");
		}
		
		// Add a waypoint for launch
		OziAPI.deleteWaypoint("LAUNCH");
		OziAPI.addWaypoint(new Waypoint("LAUNCH", 0, new LatLon(launchLat, launchLon)));
		
		// Setup a track for the baloon flightpath	
		OziAPI.setTrackDescription(balloonTrack, "OziPlotter payload track");
		OziAPI.setTrackWidth(balloonTrack, trackWidth);
		OziAPI.setTrackType(balloonTrack, TrackType.LINE);
		OziAPI.setTrackColor(balloonTrack, balloonColour); 
		OziAPI.showTrack(balloonTrack);
		OziAPI.hideAllTracks(); OziAPI.showAllTracks(); 
		OziAPI.refreshMap();
		
		// Open the UDP Listener
		System.out.println("Opening UDP Socket on Port 8942\r\n");
		udpSource = new DatagramSocket(8942);
		udpSource.setSoTimeout(udpTimeout*1000);
		
			
		// Handle the stream reading & processing
		String udpLine; String callsign; char c;

		while (OziAPI.findOzi()) {

			// Get setup to read a UDP packet.
			byte[] packetbuf = new byte[1024];
			DatagramPacket packet = new DatagramPacket(packetbuf, packetbuf.length);
			// Attempt to read a UDP packet. Timeout after 5 seconds. 
			try{
				udpSource.receive(packet);
			}catch (Exception e) {
				// Probably a timeout. Increment our counter. and try again.
				telemetryTimer += udpTimeout;
				continue;
			}

			udpLine = new String(packet.getData(),0,packet.getLength());
			// Remove any trailing return chars
			udpLine = udpLine.replaceAll("\\r","");
			udpLine = udpLine.replaceAll("\\n","");
			System.out.println("Read line: " + udpLine + "\r\n");

			// Expected Strings:
			// TELEMETRY,HH:MM:SS,lat,lon,alt\n
			// WAYPOINT,<waypointname>,lat,lon,message\n


			try{
				String[] fields = udpLine.split(",");

				if( fields[0].equals("TELEMETRY")) {
					// Attempt to extract field data from the string.
					String time;
					double lat = 0, lon = 0, ascRate = 0, speed=0.0;
					int currentTime = 0, hour = 0, minute = 0, second = 0;
					int txCount = 0, alt = 0, numSats = 0, extTemp = 0, intTemp = 0, rssi = 0, snr = 0;
					// Try to parse the telemetry data
					time = fields[1];
					lat = Double.parseDouble(fields[2]);
					lon = Double.parseDouble(fields[3]);
					alt = Integer.parseInt(fields[4]);
					
					// Break up the time
					hour = Integer.parseInt(time.split("\\:")[0]);
					minute = Integer.parseInt(time.split("\\:")[1]);
					second = Integer.parseInt(time.split("\\:")[2]);
					currentTime = (hour * 3600) + (minute * 60) + second;
					
					// Ascent rate
					ascRate = Math.round((float)(alt - lastAlt)/(currentTime - lastTime) * 10.0)/10.0;
					
					// Play some beeps when the balloon first bursts
					if (!descent && alt < (lastAlt - 100)) { beep(10); }
					
					// Enter descent mode
					if (ascRate < -3) { descent = true; }
					if (descent) { System.out.println("BALLOON HAS BURST!\n"); }
					
					// Print position data
					System.out.println("Position data:");
					System.out.println(" * Transmission time: " + time);
					System.out.println(" * Altitude: " + alt + " m");
					System.out.println(" * Ascent rate: " + ascRate + " m/s");
					System.out.println(" * Latitude: " + lat);
					System.out.println(" * Longitude: " + lon + "\n");
					

					
					// Update tracking vars
					lastAlt = alt;
					lastTime = currentTime;

					// Look for a valid fix
					if (lat != 0 && lon != 0) {
					
						// Plot the point in OziExplorer
						plotPoint(lat, lon, alt);
						
						// Update predictor?
						if (lastTime - lastPredUpdate > predictionAverage || lastPredUpdate > lastTime) {
							predictor.update(lat, lon, alt, hour, minute, second);
							lastPredUpdate = lastTime;
						}
						
						// Draw predictions?
						if (lastTime - lastPredTime > predictionFrequency || lastPredTime > lastTime) {
							predictor.runPredictions();
							lastPredTime = lastTime;
						}
						
					} else {
						System.out.println("No GPS lock, not plotting...");
					}

					telemetryTimer = 0;
				} else if (fields[0].equals("WAYPOINT")) {
					// Extract fields
					String waypointName, waypointComment;
					double lat = 0, lon = 0;

					waypointName = fields[1];
					lat = Double.parseDouble(fields[2]);
					lon = Double.parseDouble(fields[3]);
					waypointComment = fields[4];

					// Delete waypoint if it exists, and add the new waypoint.
					OziAPI.deleteWaypoint(waypointName);
					// TODO: Modify waypoint symbol number to be a car symbol.
					OziAPI.addWaypoint(new Waypoint(waypointName, 0, new LatLon(lat,lon)));
					System.out.println("Updated Waypoint named " + waypointName + " at " + lat + "," + lon + ".\n");

				} else {
					System.out.println("Unknown message type: " + udpLine);
				}

			} catch (Exception e) {
					System.out.println("Data not in expected format!\r\n");
					e.printStackTrace();
			}
			
			System.out.println("-------------------------------------------------------------------------------");
		}
		
		if (!OziAPI.findOzi()) System.out.println("Lost connection to OziExplorer, exiting!");
		
    }
	
    //
	// Plot a point in a track
	//
    public void plotPoint(double lat, double lon, int alt) throws OziException{
		OziAPI.addTrackPoint(balloonTrack, new TrackPoint(false,new LatLon(lat, lon), alt));
		OziAPI.deleteWaypoint(balloonCallsign);
		OziAPI.addWaypoint(new Waypoint(balloonCallsign, 0, new LatLon(lat, lon)));
		OziAPI.refreshMap();
	}
	
	//
	// Plot a waypoint
	//
    public void plotPoint(double lat, double lon) throws OziException {
		OziAPI.addWaypoint(new Waypoint("?", 0, new LatLon(lat, lon)));
		OziAPI.refreshMap();
	}
	
	//
	// Play some beeps (print some bells chars)
	//
	public void beep(int beeps) {
		for (int i=0; i<beeps; i++) {
			System.out.print("\007");
			System.out.flush();
		}
	}
}