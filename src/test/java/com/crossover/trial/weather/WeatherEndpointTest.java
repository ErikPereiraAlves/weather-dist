package com.crossover.trial.weather;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.Before;
import org.junit.Test;
import static com.crossover.trial.weather.AirportLoader.*;
import static com.crossover.trial.weather.RestWeatherQueryEndpoint.numIatasFound;
import java.util.List;

import javax.validation.constraints.AssertTrue;

import static org.junit.Assert.assertEquals;

public class WeatherEndpointTest {

	private WeatherQueryEndpoint _query = new RestWeatherQueryEndpoint();

	private WeatherCollectorEndpoint _update = new RestWeatherCollectorEndpoint();

	private Gson _gson = new Gson();

	private DataPoint _dp;

	@Before
	public void setUp() throws Exception {
		RestWeatherQueryEndpoint.init();
		/// mean, first, second, thrid and count keys
		_dp = new DataPoint.Builder().withCount(10).withFirst(10).withSecond(20).withMean(22).withThird(30).build();
		_update.updateWeather("BOS", "wind", _gson.toJson(_dp));
		_query.weather("BOS", "0").getEntity();
	}

	@Test
	public void testPing() throws Exception {
		String ping = _query.ping();
		JsonElement pingResult = new JsonParser().parse(ping);
		assertEquals(airports.size(), pingResult.getAsJsonObject().get("datasize").getAsInt());
		assertEquals(airports.size(),
				pingResult.getAsJsonObject().get("iata_freq").getAsJsonObject().entrySet().size());
	}

	@Test
	public void testGet() throws Exception {
		List<AtmosphericInformation> ais = (List<AtmosphericInformation>) _query.weather("BOS", "0").getEntity();
		assertEquals(ais.get(0).getWind(), _dp);
	}

	@Test
	public void testGetNearby() throws Exception {
		// check datasize response
		numIatasFound = 0;
		_update.updateWeather("JFK", "wind", _gson.toJson(_dp));
		_dp.setMean(40);
		_update.updateWeather("EWR", "wind", _gson.toJson(_dp));
		_dp.setMean(30);
		_update.updateWeather("LGA", "wind", _gson.toJson(_dp));

		List<AtmosphericInformation> ais = (List<AtmosphericInformation>) _query.weather("JFK", "200").getEntity();

		assertEquals(ais.size(), numIatasFound);

	}

	@Test
	public void testUpdate() throws Exception {

		DataPoint windDp = new DataPoint.Builder().withCount(10).withFirst(10).withSecond(20).withMean(22).withThird(30)
				.build();
		_update.updateWeather("BOS", "wind", _gson.toJson(windDp));
		_query.weather("BOS", "0").getEntity();

		String ping = _query.ping();
		JsonElement pingResult = new JsonParser().parse(ping);
		assertEquals(airports.size(), pingResult.getAsJsonObject().get("datasize").getAsInt());

		DataPoint cloudCoverDp = new DataPoint.Builder().withCount(4).withFirst(10).withSecond(60).withMean(50)
				.withThird(30).build();
		_update.updateWeather("BOS", "cloudcover", _gson.toJson(cloudCoverDp));

		List<AtmosphericInformation> ais = (List<AtmosphericInformation>) _query.weather("BOS", "0").getEntity();
		assertEquals(ais.get(0).getWind(), windDp);
		assertEquals(ais.get(0).getCloudCover(), cloudCoverDp);
	}

	// ---- insert and delete airports to the airports.dat
	@Test
	public void testInsertAndRemove() throws Exception {
		AirportData airportData = (AirportData) _update.addAirport("FOR", "3.7736", "38.5286").getEntity();
		assertEquals(airportData.getIata(), "FOR");

		airportData = (AirportData) _update.addAirport("REC", "8.1259", "34.9240").getEntity();
		assertEquals(airportData.getIata(), "REC");

		airportData = (AirportData) _update.deleteAirport("REC").getEntity();
		assertEquals(airportData.getIata(), "REC");
	}

}