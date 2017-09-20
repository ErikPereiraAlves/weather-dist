package com.crossover.trial.weather;

import static com.crossover.trial.weather.AirportLoader.airports;
import static com.crossover.trial.weather.RestWeatherQueryEndpoint.R;
import static com.crossover.trial.weather.RestWeatherQueryEndpoint.atmosphericInformation;
import static com.crossover.trial.weather.RestWeatherQueryEndpoint.numIatasFound;
import static com.crossover.trial.weather.RestWeatherQueryEndpoint.radiusFreq;
import static com.crossover.trial.weather.RestWeatherQueryEndpoint.requestFrequency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;

/**
 * 
 * @author Erik Pereira Alves
 * 
 *         crossover - Java Software Architect.
 *
 */

public class WeatherQueryEndpointCallableTask implements Callable<Response> {

	public final static Logger LOGGER = Logger.getLogger("WeatherQueryEndpointCallableTask");

	public static enum restEnum {
		weather;
	}

	public restEnum restCall;

	public String iata;

	public String radiusString;

	@Override
	public Response call() throws Exception {

		switch (restCall) {

		case weather:
			return weather(iata, radiusString);

		}

		return Response.status(Response.Status.BAD_REQUEST).build();
	}

	/**
	 * Given a query in json format {'iata': CODE, 'radius': km} extracts the
	 * requested airport information and return a list of matching atmosphere
	 * information.
	 *
	 * @param iata
	 *            the iataCode
	 * @param radiusString
	 *            the radius in km
	 *
	 * @return a list of atmospheric information
	 */

	public Response weather(String iata, String radiusString) {

		double radius = radiusString == null || radiusString.trim().isEmpty() ? 0 : Double.valueOf(radiusString);
		updateRequestFrequency(iata, radius);

		List<AtmosphericInformation> retval = new ArrayList<>();
		if (radius == 0) {
			int idx = getAirportDataIdx(iata);
			retval.add(atmosphericInformation.get(idx));
			numIatasFound++;

		} else {
			AirportData ad = findAirportData(iata);
			for (int i = 0; i < airports.size(); i++) {
				if (calculateDistance(ad, airports.get(i)) <= radius) {
					AtmosphericInformation ai = atmosphericInformation.get(i);
					if (ai.getCloudCover() != null || ai.getHumidity() != null || ai.getPrecipitation() != null
							|| ai.getPressure() != null || ai.getTemperature() != null || ai.getWind() != null) {
						numIatasFound++;
						retval.add(ai);

					}
				}
			}
		}
		return Response.status(Response.Status.OK).entity(retval).build();
	}

	/**
	 * Records information about how often requests are made
	 *
	 * @param iata
	 *            an iata code
	 * @param radius
	 *            query radius
	 */
	public void updateRequestFrequency(String iata, Double radius) {
		AirportData airportData = findAirportData(iata);
		requestFrequency.put(airportData, requestFrequency.getOrDefault(airportData, 0) + 1);
		radiusFreq.put(radius, radiusFreq.getOrDefault(radius, 0));
	}

	/**
	 * Given an iataCode find the airport data
	 *
	 * @param iataCode
	 *            as a string
	 * @return airport data or null if not found
	 */
	public static AirportData findAirportData(String iataCode) {

		return airports.stream().filter(ap -> ap.getIata().equals(iataCode)).findFirst().orElse(null);
	}

	/**
	 * Given an iataCode find the airport data
	 *
	 * @param iataCode
	 *            as a string
	 * @return airport data or null if not found
	 */
	public static int getAirportDataIdx(String iataCode) {
		AirportData ad = findAirportData(iataCode);

		return airports.indexOf(ad);
	}

	/**
	 * Haversine distance between two airports.
	 *
	 * @param ad1
	 *            airport 1
	 * @param ad2
	 *            airport 2
	 * @return the distance in KM
	 */
	public double calculateDistance(AirportData ad1, AirportData ad2) {
		double deltaLat = Math.toRadians(ad2.getLatitude() - ad1.getLatitude());
		double deltaLon = Math.toRadians(ad2.getLongitude() - ad1.getLongitude());
		double a = Math.pow(Math.sin(deltaLat / 2), 2)
				+ Math.pow(Math.sin(deltaLon / 2), 2) * Math.cos(ad1.getLatitude()) * Math.cos(ad2.getLatitude());
		double c = 2 * Math.asin(Math.sqrt(a));
		return R * c;
	}

	public void setIata(String iata) {
		this.iata = iata;
	}

	public void setRadiusString(String radiusString) {
		this.radiusString = radiusString;
	}

	public void setRestCall(restEnum restCall) {
		this.restCall = restCall;
	}

}
