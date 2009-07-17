package org.schtief.whereisschtief;

import java.io.IOException;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.schtief.whereisschtief.LatitudeJSONParser.LatitudeJSONParserException;

@SuppressWarnings("serial")
public class UpdateServlet extends HttpServlet {

	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		//loop over all users
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try{
		List<User> users	=	UserManager.listUsers(pm);
		for (User user : users) 
		{
			//get location for each user
    		
            try {
    			Location location	=	LatitudeJSONParser.getPosition(/*"http://www.google.com/latitude/apps/badge/api?user=-2071822615655660005&type=json"*/user.getJsonUrl());
        		//check if there is a location change
        		String jdoql	=	"select from "+Location.class.getName()+" order by time desc range 0,1";
        		List<Location> lastLocations = (List<Location>) pm.newQuery(jdoql).execute();
        		if(null!=lastLocations && lastLocations.size()==1)
        		{
        			resp.getWriter().println("LastLocation " +lastLocations.get(0).toString());
        			if(! lastLocations.get(0).getLatitude().equals(location.getLatitude()) || 
        				! lastLocations.get(0).getLongitude().equals(location.getLongitude()))
        			{
                    	pm.makePersistent(location);
                    	resp.getWriter().println("added");
        			}
        			else
                    	resp.getWriter().println("ignored");
        		}
        		else
        		{
        			pm.makePersistent(location);
                	resp.getWriter().println("first");
        		}
        		

            } catch (LatitudeJSONParserException e) {
            	e.printStackTrace();
			}
		}    
		}finally {
             pm.close();
         }
	}
}