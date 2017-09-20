package com.crossover.trial.weather;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * A collected point, including some information about the range of collected
 * values
 *
 * @author code test administrator
 */
public class DataPoint {

	private double mean = 0.0;

	private int first = 0;

	private int second = 0;

	private int third = 0;

	private int count = 0;

	/** private constructor, use the builder to create this object */
	private DataPoint() {
	}

	protected DataPoint(int first, int second, int mean, int third, int count) {
		this.setMean(mean);
		this.setFirst(first);
		this.setSecond(second);
		this.setThird(third);
		this.setCount(count);
	}

	/** the mean of the observations */
	public double getMean() {
		return mean;
	}

	protected void setMean(double mean) {
		this.mean = mean;
	}

	/** 1st quartile -- useful as a lower bound */
	public int getFirst() {
		return first;
	}

	protected void setFirst(int first) {
		this.first = first;
	}

	/** 2nd quartile -- median value */
	public int getSecond() {
		return second;
	}

	protected void setSecond(int second) {
		this.second = second;
	}

	/** 3rd quartile value -- less noisy upper value */
	public int getThird() {
		return third;
	}

	protected void setThird(int third) {
		this.third = third;
	}

	/** the total number of measurements */
	public int getCount() {
		return count;
	}

	protected void setCount(int count) {
		this.count = count;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
	}

	public boolean equals(Object that) {
		return this.toString().equals(that.toString());
	}

	static public class Builder {

		private int mean;
		private int first;
		private int second;
		private int third;
		private int count;

		public Builder() {
		}

		public Builder withMean(int mean) {
			this.mean = mean;
			return this;
		}

		public Builder withFirst(int first) {
			this.first = first;
			return this;
		}

		public Builder withSecond(int second) {
			this.second = second;
			return this;
		}

		public Builder withThird(int third) {
			this.third = third;
			return this;
		}

		public Builder withCount(int count) {
			this.count = count;
			return this;
		}

		// mean, first, second, thrid and count keys
		public DataPoint build() {
			return new DataPoint(this.first, this.second, this.mean, this.third, this.count);
		}
	}
}
