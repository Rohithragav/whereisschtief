package org.schtief.whereisschtief;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jdo.PersistenceManager;

public class LocationManager 
{

	@SuppressWarnings("unchecked")
	public static List<Location> getLocations(PersistenceManager pm, String name, Calendar fromDate, Calendar toDate)
	{
		StringBuffer jdoql	=	new StringBuffer("SELECT FROM ");
		jdoql.append(Location.class.getName());
		if(null!=fromDate || null!= toDate)
		{			
			jdoql.append(" WHERE");
			if(null!=fromDate)
			{
				jdoql.append(" time >= ");
				jdoql.append(fromDate.getTimeInMillis());
			}
			if(null!=toDate)
			{
				jdoql.append(" && time <= ");
				jdoql.append(toDate.getTimeInMillis());
			}
		}
		//TODO name
		jdoql.append(" ORDER BY time ASC");

//		String jdoql = "SELECT FROM " + Location.class.getName()+" WHERE time >= "+fromDate.getTimeInMillis()+" && time <= "+toDate.getTimeInMillis()+" ORDER BY time DESC";
		System.out.println("HALLO "+jdoql);
		return (List<Location>) pm.newQuery(jdoql.toString()).execute();
	}

	public static List<Location> getClusteredLocations(PersistenceManager pm, String name, Calendar fromDate, Calendar toDate, double thresholdRadius, int thresholdMinutes)
	{
		List<Location> locations	=	getLocations(pm, name, fromDate, toDate);
		List<Location> result		=	new ArrayList<Location>();

		Cluster cluster	=	null;
		for (Location location : locations) 
		{
//			if(null==lastLocation)
//			{
//				lastLocation = location;
//				continue;
//			}
			//wenn Cluster noch nicht da erstellen mit Location
			if(null==cluster)
			{
				cluster	=	new Cluster(location);
				continue;
			}
			//wenn abstand zum Cluster < thresholdRadius
			if(cluster.distance(location)<=thresholdRadius)
			{
				System.out.println("Cluster Distance ok ADD : "+cluster.distance(location));
				//zum cluster hinzuf�gen
				cluster.add(location);
				//continue
			}
			else //wenn abstand zum Cluster > thresholdRadius
			{
				System.out.println("Cluster Distance not ok : "+cluster.distance(location));

				//calc LocationT-startT
				//if dt > thresholdMinutes
				if((location.getTime()-cluster.getTime())/(1000*60)>=thresholdMinutes)
				{
					System.out.println("Cluster sum time OK "+((location.getTime()-cluster.getTime())/(1000*60)));
					//set last time
					cluster.setLastTime(location.getTime());
					//calc center add to List
					result.add(cluster);
				}
				else //if dt < thresholdMinutes
				{
					System.out.println("Cluster sum time not OK "+((location.getTime()-cluster.getTime())/(1000*60)));

					//add whole cluster Members to List
					result.addAll(cluster.getMembers());
				}
				//new cluster add Location
				cluster	=	new Cluster(location);
				
			}
		}	
		//cluster kann noch voll sein.
		//calc LocationT-startT
		//if dt > thresholdMinutes
		if((locations.get(locations.size()-1).getTime()-cluster.getTime())/(1000*60)>=thresholdMinutes)
		{
			System.out.println("Last Cluster sum time OK "+((locations.get(locations.size()-1).getTime()-cluster.getTime())/(1000*60)));
			//calc center add to List
			result.add(cluster);
		}
		else //if dt < thresholdMinutes
		{
			System.out.println("Last Cluster sum time not OK "+((locations.get(locations.size()-1).getTime()-cluster.getTime())/(1000*60)));

			//add whole cluster Members to List
			result.addAll(cluster.getMembers());
		}
		return result;
	}

	
}