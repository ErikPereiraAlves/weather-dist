package com.crossover.trial.weather;

import static com.crossover.trial.weather.AirportLoader.airports;
import static com.crossover.trial.weather.AirportLoader.getAirportByIata;
import static com.crossover.trial.weather.AirportLoader.updateAirportDataFile;
import static com.crossover.trial.weather.RestWeatherCollectorEndpoint.gson;
import static com.crossover.trial.weather.RestWeatherQueryEndpoint.atmosphericInformation;
import static com.crossover.trial.weather.RestWeatherQueryEndpoint.findAirportData;
import static com.crossover.trial.weather.RestWeatherQueryEndpoint.getAirportDataIdx;

import java.util.HashSet;
import java.util.Set;
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

public class WeatherCollectorEndpointCallableTask implements Callable<Response> {

	public final static Logger LOGGER = Logger.getLogger("WeatherCollectorEndpointCallableTask");

	public enum restCollectorEnum {
		updateWeather, getAirports, getAirport, addAirport, deleteAirport;
	}

	private restCollectorEnum restCallCollector;

	private String iata;

	private String latitude;

	private String longitude;

	private String radiusString;

	private String pointType;

	private String datapointJson;

	@Override
	public Response call() throws Exception {

		switch (restCallCollector) {

		case updateWeather:
			return updateWeather(iata, pointType, datapointJson);

		case getAirports:
			return getAirports();

		case getAirport:
			return getAirport(iata);

		case addAirport:
			return addAirport(iata, latitude, longitude);

		case deleteAirport:
			return deleteAirport(iata);

		}

		return Response.status(Response.Status.BAD_REQUEST).build();

	}

	public Response updateWeather(String iataCode, String pointType, String datapointJson) {
		try {
			addDataPoint(iataCode, pointType, gson.fromJson(datapointJson, DataPoint.class));
		} catch (WeatherException e) {
			e.printStackTrace();
		}
		return Response.status(Response.Status.OK).build();
	}

	public Response getAirports() {
		Set<String> retval = new HashSet<>();
		for (AirportData ad : airports) {
			retval.add(ad.getIata());
		}
		return Response.status(Response.Status.OK).entity(retval).build();
	}

	public Response getAirport(String iata) {
		AirportData ad = findAirportData(iata);
		return Response.status(Response.Status.OK).entity(ad).build();
	}

	public Response addAirport(String iata, String latString, String longString) {
		AirportData ad = addAirport(iata, Double.valueOf(latString), Double.valueOf(longString));
		return Response.status(Response.Status.OK).entity(ad).build();
	}

	public Response deleteAirport(String iata) {
		AirportData ad = removeAirport(iata);
		return Response.status(Response.Status.OK).entity(ad).build();
		// return Response.status(Response.Status.NOT_IMPLEMENTED).build();
	}

	//
	// Internal support methods
	//

	/**
	 * Update the airports weather data with the collected data.
	 *
	 * @param iataCode
	 *            the 3 letter IATA code
	 * @param pointType
	 *            the point type {@link DataPointType}
	 * @param dp
	 *            a datapoint object holding pointType data
	 *
	 * @throws WeatherException
	 *             if the update can not be completed
	 */
	public void addDataPoint(String iataCode, String pointType, DataPoint dp) throws WeatherException {
		int airportDataIdx = getAirportDataIdx(iataCode);
		AtmosphericInformation ai = atmosphericInformation.get(airportDataIdx);
		updateAtmosphericInformation(ai, pointType, dp);
	}

	/**
	 * update atmospheric information with the given data point for the given
	 * point type
	 *
	 * @param ai
	 *            the atmospheric information object to update
	 * @param pointType
	 *            the data point type as a string
	 * @param dp
	 *            the actual data point
	 */
	public void updateAtmosphericInformation(AtmosphericInformation ai, String pointType, DataPoint dp)
			throws WeatherException {
		final DataPointType dptype = DataPointType.valueOf(pointType.toUpperCase());

		if (pointType.equalsIgnoreCase(DataPointType.WIND.name())) {
			if (dp.getMean() >= 0) {
				ai.setWind(dp);
				ai.setLastUpdateTime(System.currentTimeMillis());
				return;
			}
		}

		if (pointType.equalsIgnoreCase(DataPointType.TEMPERATURE.name())) {
			if (dp.getMean() >= -50 && dp.getMean() < 100) {
				ai.setTemperature(dp);
				ai.setLastUpdateTime(System.currentTimeMillis());
				return;
			}
		}

		if (pointType.equalsIgnoreCase(DataPointType.HUMIDTY.name())) {
			if (dp.getMean() >= 0 && dp.getMean() < 100) {
				ai.setHumidity(dp);
				ai.setLastUpdateTime(System.currentTimeMillis());
				return;
			}
		}

		if (pointType.equalsIgnoreCase(DataPointType.PRESSURE.name())) {
			if (dp.getMean() >= 650 && dp.getMean() < 800) {
				ai.setPressure(dp);
				ai.setLastUpdateTime(System.currentTimeMillis());
				return;
			}
		}

		if (pointType.equalsIgnoreCase(DataPointType.CLOUDCOVER.name())) {
			if (dp.getMean() >= 0 && dp.getMean() < 100) {
				ai.setCloudCover(dp);
				ai.setLastUpdateTime(System.currentTimeMillis());
				return;
			}
		}

		if (pointType.equalsIgnoreCase(DataPointType.PRECIPITATION.name())) {
			if (dp.getMean() >= 0 && dp.getMean() < 100) {
				ai.setPrecipitation(dp);
				ai.setLastUpdateTime(System.currentTimeMillis());
				return;
			}
		}

		throw new IllegalStateException("couldn't update atmospheric data");
	}

	/**
	 * Add a new known airport to our list.
	 *
	 * @param iataCode
	 *            3 letter code
	 * @param latitude
	 *            in degrees
	 * @param longitude
	 *            in degrees
	 *
	 * @return the added airport
	 */
	public static AirportData addAirport(String iataCode, double latitude, double longitude) {

		AirportData ad;

		AirportData airportDataExists = getAirportByIata(iataCode);

		if (null == airportDataExists) {
			ad = new AirportData(iataCode, latitude, longitude);
			ad.setPk(airports.size() + 1);
			airports.add(ad);
			AtmosphericInformation ai = new AtmosphericInformation();
			atmosphericInformation.add(ai);

		} else {
			airportDataExists.setIata(iataCode);
			airportDataExists.setLatitude(latitude);
			airportDataExists.setLongitude(longitude);
			ad = airportDataExists;
		}

		updateAirportDataFile();

		return ad;

	}

	/**
	 * Delete a new known airport to our list.
	 *
	 * @param iataCode
	 *            3 letter code
	 *
	 * @return the removed airport
	 */
	public static AirportData removeAirport(String iataCode) {

		AirportData airportData = getAirportByIata(iataCode);

		if (null != airportData) {
			airports.remove(airportData);
			updateAirportDataFile();// removed, now time to update .dat file.
		} else {
			airportData = new AirportData();// not found,return default empty
											// object.
		}

		return airportData;
	}

	public void setIata(String iata) {
		this.iata = iata;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public void setRadiusString(String radiusString) {
		this.radiusString = radiusString;
	}

	public void setDatapointJson(String datapointJson) {
		this.datapointJson = datapointJson;
	}

	public void setPointType(String pointType) {
		this.pointType = pointType;
	}

	public void setRestCallCollector(restCollectorEnum restCallCollector) {
		this.restCallCollector = restCallCollector;
	}

}
