package org.schtief.whereisschtief;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONWriter;

public class Cluster extends Location {
	private List<Location> members;
	private Long lastTime	=	null;
	
	public void setLastTime(Long lastTime) {
		this.lastTime = lastTime;
	}

	public Cluster(Location location) {
		super();
		members	=	new ArrayList<Location>();
		members.add(location);
	}

	public void add(Location location) {
		members.add(location);
	}

	public Collection<? extends Location> getMembers() {
		return members;
	}

	@Override
	public Double distance(Location location) {
		double latSum	=	0;
		double lonSum	=	0;
		
		for (Location loc : members) {
			latSum+=loc.getDoubleLatitude();
			lonSum+=loc.getDoubleLongitude();
		}
		latSum	=	latSum/members.size();
		lonSum	=	lonSum/members.size();
		
		return distance(latSum, lonSum,
				location.getDoubleLatitude(), location.getDoubleLongitude())*1000;		
	}

	@Override
	public void toJSON(JSONWriter writer) throws JSONException {
		writer.key("id");
		writer.value(members.get(0).getId());
		writer.key("date");
		
		Calendar cal=Calendar.getInstance();
		cal.setTimeInMillis(members.get(0).getTime());
		cal.add(Calendar.HOUR, 2);
		
		long duration;
		Calendar endCal	=	null;
		if(null!=lastTime)
		{
			duration =	lastTime-members.get(0).getTime();
			endCal =	Calendar.getInstance();
			endCal.setTimeInMillis(lastTime);
			endCal.add(Calendar.HOUR, 2);
		}
		else
		{
			duration =	members.get(members.size()-1).getTime()-members.get(0).getTime();
		}
		
		duration	=	(duration/60000);
		String durationS;
		if(duration>60)
			durationS	=	(duration/60)+"h "+(duration%60)+"mins";
		else
			durationS	=	duration+"mins";

		writer.value(cal.getTime().toGMTString()+"<br/>"+(null==lastTime ?"till now":endCal.getTime().toGMTString()+"<br/>")+"="+durationS);
		
		double latSum	=	0;
		double lonSum	=	0;
		
		for (Location loc : members) {
			latSum+=loc.getDoubleLatitude();
			lonSum+=loc.getDoubleLongitude();
		}
		latSum	=	latSum/members.size();
		lonSum	=	lonSum/members.size();
		
		writer.key("latitude");
		writer.value(Double.toString(latSum));
		
		writer.key("longitude");
		writer.value(Double.toString(lonSum));

	}

	@Override
	public Long getTime() {

		return members.get(0).getTime();
	}
	
	
}
