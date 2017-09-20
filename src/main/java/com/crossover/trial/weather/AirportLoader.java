package com.crossover.trial.weather;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import static com.crossover.trial.weather.AirportLoader.airports;
import static com.crossover.trial.weather.AirportLoader.getAirports;
import static com.crossover.trial.weather.RestWeatherQueryEndpoint.atmosphericInformation;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Logger;

/**
 * A simple airport loader which reads a file from disk and sends entries to the
 * webservice
 *
 * TODO: Implement the Airport Loader. I did!
 * 
 * @author code test administrator
 */
public class AirportLoader {

	public final static Logger LOGGER = Logger.getLogger(AirportLoader.class.getName());

	/** end point for read queries */
	private WebTarget query;

	/** end point to supply updates */
	private WebTarget collect;

	private String host = "http://localhost:8080/";

	private static final String DEFAULT_SEPARATOR = ",";

	private static final int DEFAULT_FILE_COLUMN_SIZE = 11;

	private static File airportDataFile;

	/** all known airports */
	protected static List<AirportData> airports = new ArrayList<>();

	/** if collection of airports in memory are up to date */
	private static boolean upToDateFlag = true;

	/** Get the collection of available airports */
	public static List<AirportData> getAirports() {

		if (false == isUpToDateFlag() || null == airports || 0 == airports.size()) {
			try {
				synchronized (AirportLoader.class) {// only one thread needs to
													// do this, for benefit of
													// all.
					if (null == airportDataFile) {
						airportDataFile = getAirportsDatFile();
					}
					upload(airportDataFile);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return airports;
	}

	public AirportLoader() {
		Client client = ClientBuilder.newClient();
		query = client.target(host + "query");
		collect = client.target(host + "collect");
	}

	public static void upload(File file) throws IOException {

		airportDataFile = file;

		// auxiliary map to prevent repeated values being added to the arraylist
		// of airports.
		Map<String, AirportData> map = new LinkedHashMap<String, AirportData>();

		if (isUpToDateFlag() && null != airports && airports.size() > 0) {
			// if true, collection is in memory and up to date, no need to
			// re-scan file for
			// now.

			return;
		}

		// get the latest airports in memory
		String[] l;
		Scanner scanner = new Scanner(airportDataFile);
		airports.clear();// reset it
		while (scanner.hasNext()) {
			l = scanner.nextLine().replaceAll("\"", "").split(DEFAULT_SEPARATOR);
			if (null != l && l.length == DEFAULT_FILE_COLUMN_SIZE) {
				map.put(l[5],
						new AirportData(Integer.parseInt(l[0]), l[1], l[2], l[3], l[4], l[5], Double.parseDouble(l[6]),
								Double.parseDouble(l[7]), Double.parseDouble(l[8]), Float.parseFloat(l[9]), l[10]));

			}

		}

		airports.addAll(map.values());
		atmosphericInformation = new LinkedList<>(Collections.nCopies(airports.size(), new AtmosphericInformation()));

		// collection of airports in memory are up to date now..
		setUpToDateFlag(true);

		scanner.close();

	}

	/*
	public static void main(String args[]) throws IOException {
		
		if (args.length > 0) {
			airportDataFile = new File(args[0]);
			
		}
		else{
			System.out.println("Did you add airports.dat path as java argument when running this program?");
		}

	}
	*/

	public static boolean isUpToDateFlag() {
		return upToDateFlag;
	}

	public static void setUpToDateFlag(boolean upToDateFlag) {
		AirportLoader.upToDateFlag = upToDateFlag;
	}

	// just in case tester didn't pass the airport.dat file as argument.
	// disregard this method.
	public static File getAirportsDatFile() {
		File dir = new File(new AirportLoader().getClass().getResource(".").getPath());
		String path = dir.toString();

		if (null != path && path.contains("test-classes")) {
			path = path.replace("/target/test-classes/com/crossover/trial/weather", "");
		} else {
			path = path.replace("/target/classes/com/crossover/trial/weather", "");
		}
		path = path + "/src/main/resources/airports.dat";

		airportDataFile = new File(path);

		return airportDataFile;

	}

	public static AirportData getAirportByIata(String iataCode) {

		if (null == airports || airports.size() == 0) {
			getAirports();
		}

		for (AirportData data : airports) {
			if (data.getIata().equalsIgnoreCase(iataCode)) {
				return data;
			}

		}
		return null;

	}

	// if it were a real database i wouldnt have to worry about DML concurrency.
	// but since it's a file..
	public synchronized static void updateAirportDataFile() {

		Path path = airportDataFile.toPath();

		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
			for (int i = 0; i < airports.size(); i++) {

				writer.write(airports.get(i).writeToFile(i + 1));
				if (i < airports.size() - 1) {
					writer.write(System.getProperty("line.separator"));
				}

			}

		} catch (IOException e) {
			LOGGER.warning("IOException when trying to write to airports.dat");
		}
		// flat "out date" the current List found in memory. So that next client
		// refreshes it for everyone.
		setUpToDateFlag(false);
	}
}
