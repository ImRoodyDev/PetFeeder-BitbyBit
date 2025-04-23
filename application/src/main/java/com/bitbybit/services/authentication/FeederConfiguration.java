package com.bitbybit.services.authentication;

import com.bitbybit.components.ScheduleComponent;
import org.json.JSONObject;

import java.util.ArrayList;

public class FeederConfiguration {
	public final String feederId;
	public int portionSize;
	public ArrayList<ScheduleComponent> feedTimestamps = new ArrayList<>();

	public FeederConfiguration(String feederId, int portionSize) {
		this.feederId = feederId;
		this.portionSize = portionSize;
	}

	public void addTimestampsComponent(ArrayList<ScheduleComponent> timestamps) {
		this.feedTimestamps.clear();
		this.feedTimestamps.addAll(timestamps);
	}

	public void addTimestamp(String id) throws Exception {
		int position = (feedTimestamps.size());
		feedTimestamps.add(new ScheduleComponent(id, position, "null:null"));
	}

	public void removeTimestamp(int position) {
		feedTimestamps.remove(position);
	}
}
