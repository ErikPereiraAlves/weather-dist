package com.crossover.trial.weather;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Basic airport information.
 *
 * @author code test administrator
 */
public class AirportData {

	/** the airport primary key code */
	private int pk = 0;

	/** the airport name string code */
	private String name = "";

	/** the city string code */
	private String city = "";

	/** the country string code */
	private String country = "";

	/** the three letter IATA code */
	private String iata = "";

	/** the four etter ICAO code */
	private String icao = "";

	/** latitude value in degrees */
	private double latitude = 0.0;

	/** longitude value in degrees */
	private double longitude = 0.0;

	/** altitude value in feet */
	private double altitude = 0.0;

	/** timezone value in decimals */
	private float timezone = 0;

	/**
	 * the one letter DST code - E (Europe), A (US/Canada), S (South America), O
	 * (Australia), Z (New Zealand), N (None) or U (Unknown)
	 */
	private String dst = "U";

	public AirportData() {
	}

	public AirportData(String iata, double latitude, double longitude) {
		super();
		this.iata = iata;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public AirportData(int pk, String name, String city, String country, String iata, String icao, double latitude,
			double longitude, double altitude, float timezone, String dst) {
		super();
		this.pk = pk;
		this.name = name;
		this.city = city;
		this.country = country;
		this.iata = iata;
		this.icao = icao;
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
		this.timezone = timezone;
		this.dst = dst;
	}

	public int getPk() {
		return pk;
	}

	public void setPk(int pk) {
		this.pk = pk;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getIcao() {
		return icao;
	}

	public void setIcao(String icao) {
		this.icao = icao;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	public float getTimezone() {
		return timezone;
	}

	public void setTimezone(float timezone) {
		this.timezone = timezone;
	}

	public String getDst() {
		return dst;
	}

	public void setDst(String dst) {
		this.dst = dst;
	}

	public String getIata() {
		return iata;
	}

	public void setIata(String iata) {
		this.iata = iata;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

	public String writeToFile(int pk) {

		// 1,"General Edward Lawrence Logan Intl","Boston","United
		// States","BOS","KBOS",42.364347,-71.005181,19,-5,"A"
		String line = pk + ",\"" + name + "\",\"" + city + "\",\"" + country + "\",\"" + iata + "\",\"" + icao + "\",\""
				+ latitude + "\",\"" + longitude + "\",\"" + altitude + "\",\"" + timezone + "\",\"" + dst + "\"";

		return line;
	}

	public boolean equals(Object other) {
		if (other instanceof AirportData) {
			return ((AirportData) other).getIata().equals(this.getIata());
		}

		return false;
	}
}
