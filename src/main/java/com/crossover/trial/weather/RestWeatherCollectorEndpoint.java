package com.crossover.trial.weather;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.crossover.trial.weather.WeatherCollectorEndpointCallableTask.restCollectorEnum;
import com.google.gson.Gson;

/**
 * A REST implementation of the WeatherCollector API. Accessible only to airport
 * weather collection sites via secure VPN.
 *
 * @author code test administrator
 */

@Path("/collect")
public class RestWeatherCollectorEndpoint implements WeatherCollectorEndpoint {
	public final static Logger LOGGER = Logger.getLogger(RestWeatherCollectorEndpoint.class.getName());

	private static int pool = 100; // to be analyzed size needed for project.

	private static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(pool);

	/** shared gson json to object factory */
	public final static Gson gson = new Gson();

	@Override
	public Response ping() {
		return Response.status(Response.Status.OK).entity("ready").build();
	}

	@Override
	public Response updateWeather(@PathParam("iata") String iataCode, @PathParam("pointType") String pointType,
			String datapointJson) {

		Response response = Response.status(Response.Status.BAD_REQUEST).build();
		WeatherCollectorEndpointCallableTask obj = new WeatherCollectorEndpointCallableTask();
		obj.setRestCallCollector(restCollectorEnum.updateWeather);
		obj.setIata(iataCode);
		obj.setPointType(pointType);
		obj.setDatapointJson(datapointJson);

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

	@Override
	public Response getAirports() {

		Response response = Response.status(Response.Status.BAD_REQUEST).build();
		WeatherCollectorEndpointCallableTask obj = new WeatherCollectorEndpointCallableTask();
		obj.setRestCallCollector(restCollectorEnum.getAirports);

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

	@Override
	public Response getAirport(@PathParam("iata") String iata) {

		Response response = Response.status(Response.Status.BAD_REQUEST).build();
		WeatherCollectorEndpointCallableTask obj = new WeatherCollectorEndpointCallableTask();
		obj.setRestCallCollector(restCollectorEnum.getAirport);
		obj.setIata(iata);

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

	@Override
	public Response addAirport(@PathParam("iata") String iata, @PathParam("lat") String latString,
			@PathParam("long") String longString) {

		Response response = Response.status(Response.Status.BAD_REQUEST).build();
		WeatherCollectorEndpointCallableTask obj = new WeatherCollectorEndpointCallableTask();
		obj.setRestCallCollector(restCollectorEnum.addAirport);
		obj.setIata(iata);
		obj.setLatitude(latString);
		obj.setLongitude(longString);

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

	@Override
	public Response deleteAirport(@PathParam("iata") String iata) {

		Response response = Response.status(Response.Status.BAD_REQUEST).build();
		WeatherCollectorEndpointCallableTask obj = new WeatherCollectorEndpointCallableTask();
		obj.setRestCallCollector(restCollectorEnum.deleteAirport);
		obj.setIata(iata);

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

	@Override
	public Response exit() {
		System.exit(0);
		return Response.noContent().build();
	}

}
