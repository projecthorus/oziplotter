////////////////////////////////////////////////////////////
// Atmosphere.java
//
// Terry Baume, 2009-2010
// terry@bogaurd.net
//
// This class provides a rudimentary model of the atmosphere
// up to ~80km used for calculating sea level desc rates
//
////////////////////////////////////////////////////////////

import java.lang.Math;

public class Atmosphere {

	//
	// Calculate the density for a given altitude
	//
	public static double getDensity(int altitude) {

		double pressureRel;
		double deltaTemperature = 0.0; 		// I don't *think* we need this for now...

		// Constants
		double airMolWeight		= 28.9644; 	// Molecular weight of air
		double densitySL		= 1.225; 	// Density at sea level [kg/m3]
		double pressureSL		= 101325;  	// Pressure at sea level [Pa]
		double temperatureSL	= 288.15; 	// Temperature at sea level [deg K]
		double gamma			= 1.4;
		double gravity			= 9.80665;	// Acceleration of gravity [m/s2]
		double tempGrad			= -0.0065;	// Temperature gradient [deg K/m]
		double RGas				= 8.31432; 	// Gas constant [kg/Mol/K]
		double R				= 287.053;  //

		// Lookups
		int[] altitudes = {0, 11000, 20000, 32000, 47000, 51000, 71000, 84852};
		double[] pressureRels = {1, 2.23361105092158e-1, 5.403295010784876e-2, 8.566678359291667e-3, 1.0945601337771144e-3, 6.606353132858367e-4, 3.904683373343926e-5, 3.6850095235747942e-6};
		double[] temperatures = {288.15, 216.65, 216.65, 228.65, 270.65, 270.65, 214.65, 186.946};
		double[] tempGrads = {-6.5, 0, 1, 2.8, 0, -2.8, -2, 0};
		double gMR = gravity * airMolWeight / RGas;

		// Pick a region to work in
		int i = 0;
		if(altitude > 0) {
			while(altitude > altitudes[i+1]) i++;
		}

		// Lookup based on region
		double baseTemp			= temperatures[i];
		tempGrad				= tempGrads[i] / 1000.0;
		double pressureRelBase	= pressureRels[i];
		int deltaAltitude		= altitude - altitudes[i];
		double temperature		= baseTemp + tempGrad * deltaAltitude;

		// Calculate relative pressure
		if(Math.abs(tempGrad) < 1e-10) {
			pressureRel = pressureRelBase * Math.exp(-1 *gMR * deltaAltitude / 1000.0 / baseTemp);
		} else {
			pressureRel = pressureRelBase * Math.pow(baseTemp / temperature, gMR / tempGrad / 1000.0);
		}

		// Add temperature offset
		temperature  = temperature + deltaTemperature;

		// Finally, work out the density...
		double speedOfSound	= Math.sqrt(gamma * R * temperature);
		double pressure		= pressureRel * pressureSL;
		double density		= densitySL * pressureRel * temperatureSL / temperature ;

		return density;
	}

	//
	// Calculate the descent rate at sea level for a given desc rate at altitude
	//
	public static double seaLevelDescentRate(double v, int h) {
		double rho = getDensity(h);
		return Math.sqrt((rho / 1.22) * Math.pow(v, 2));
	}
}