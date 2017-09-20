package com.crossover.trial.weather;

import com.crossover.trial.weather.WeatherQueryEndpointCallableTask.restEnum;
import com.google.gson.Gson;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;
import static com.crossover.trial.weather.AirportLoader.*;

/**
 * The Weather App REST endpoint allows clients to query, update and check
 * health stats. Currently, all data is held in memory. The end point deploys to
 * a single container
 *
 * @author code test administrator
 */
@Path("/query")
public class RestWeatherQueryEndpoint implements WeatherQueryEndpoint {

	private static int pool = 100; // to be analyzed size needed for project.

	private static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(pool);

	public final static Logger LOGGER = Logger.getLogger("WeatherQuery");

	/** earth radius in KM */
	public static final double R = 6372.8;

	/** shared gson json to object factory */
	public static final Gson gson = new Gson();

	/** shared gson json to object factory */
	public static int numIatasFound = 0; // used for test assertion only.

	/**
	 * atmospheric information for each airport, idx corresponds with
	 * airportData
	 */
	protected static List<AtmosphericInformation> atmosphericInformation = new LinkedList<>();

	/**
	 * Internal performance counter to better understand most requested
	 * information, this map can be improved but for now provides the basis for
	 * future performance optimizations. Due to the stateless deployment
	 * architecture we don't want to write this to disk, but will pull it off
	 * using a REST request and aggregate with other performance metrics
	 * {@link #ping()}
	 */
	public static Map<AirportData, Integer> requestFrequency = new HashMap<AirportData, Integer>();

	public static Map<Double, Integer> radiusFreq = new HashMap<Double, Integer>();

	static {
		init();
	}

	/**
	 * Retrieve service health including total size of valid data points and
	 * request frequency information.
	 *
	 * @return health stats for the service as a string
	 */
	@Override
	public String ping() {
		Map<String, Object> retval = new HashMap<>();

		int datasize = 0;
		for (AtmosphericInformation ai : atmosphericInformation) {
			// we only count recent readings
			if (ai.getCloudCover() != null || ai.getHumidity() != null || ai.getPressure() != null
					|| ai.getPrecipitation() != null || ai.getTemperature() != null || ai.getWind() != null) {
				// updated in the last day
				if (ai.getLastUpdateTime() > System.currentTimeMillis() - 86400000) {
					datasize++;
				}
			}
		}
		retval.put("datasize", datasize);

		Map<String, Double> freq = new HashMap<>();
		// fraction of queries
		for (AirportData data : airports) {
			double frac = (double) requestFrequency.getOrDefault(data, 0) / requestFrequency.size();
			freq.put(data.getIata(), frac);
		}
		retval.put("iata_freq", freq);

		int m = radiusFreq.keySet().stream().max(Double::compare).orElse(1000.0).intValue() + 1;

		int[] hist = new int[m];
		for (Map.Entry<Double, Integer> e : radiusFreq.entrySet()) {
			int i = e.getKey().intValue() % 10;
			hist[i] += e.getValue();
		}
		retval.put("radius_freq", hist);

		return gson.toJson(retval);
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
	@Override
	public Response weather(String iata, String radiusString) {

		Response response = Response.status(Response.Status.BAD_REQUEST).build();
		WeatherQueryEndpointCallableTask obj = new WeatherQueryEndpointCallableTask();
		obj.setRestCall(restEnum.weather);
		obj.setIata(iata);
		obj.setRadiusString(radiusString);

		Future<Response> future = executor.submit(obj);
		boolean listen = true;
		while (listen) {
			if (future.isDone()) {
				try {
					response = future.get();
					listen = false;

				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
		}

		return response;
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

	/**
	 * A dummy init method that loads hard coded data ERIK's UPDATE: not so
	 * dummy anymore, now reading from .dat file...
	 */
	protected static void init() {

		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(pool);
		airports.clear();
		atmosphericInformation.clear();
		requestFrequency.clear();
		setUpToDateFlag(false);
		getAirports();
	}

}
