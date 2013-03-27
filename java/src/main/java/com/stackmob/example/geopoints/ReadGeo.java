/**
 * Copyright 2012-2013 StackMob
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stackmob.example.geopoints;

import com.stackmob.core.DatastoreException;
import com.stackmob.core.InvalidSchemaException;
import com.stackmob.core.customcode.CustomCodeMethod;
import com.stackmob.core.rest.ProcessedAPIRequest;
import com.stackmob.core.rest.ResponseToProcess;
import com.stackmob.sdkapi.*;

import java.net.HttpURLConnection;
import java.util.*;

/**
 * This example will show a user how to write a custom code method
 * that will read all geopoint values from the `user` schema and return
 * any results that fall both within a "Near" radius and within
 * a square, 2D area defined by 'WithinBox'.
 */

public class ReadGeo implements CustomCodeMethod {

  @Override
  public String getMethodName() {
    return "Read_Geopoint";
  }

  @Override
  public List<String> getParams() {
    return new ArrayList<String>();
  }

  @Override
  public ResponseToProcess execute(ProcessedAPIRequest request, SDKServiceProvider serviceProvider) {
    LoggerService logger = serviceProvider.getLoggerService(ReadGeo.class);
    Map<String, List<SMObject>> feedback = new HashMap<String, List<SMObject>>();

    SMNear near = new SMNear(           // Near-condition results will always be sorted by distance
            "position",                 // name of GeoField in schema
            new SMDouble(37.77207),     // latitude
            new SMDouble(-122.40621),   // longitude
            new SMDouble(.0025));       // radius - (62.25 mi) can be null

    SMWithinBox withinBox = new SMWithinBox(  // Whereas withinbox results can be sorted
            "position",
            new SMDouble(37.8),
            new SMDouble(-122.47),      // Top Left coords
            new SMDouble(37.7),
            new SMDouble(-122.3));      // Bottom Right coords

    DataService ds = serviceProvider.getDataService();
    List<SMCondition> query = new ArrayList<SMCondition>();
    query.add(near);
    query.add(withinBox);
    List<SMObject> results;

    try {
      results = ds.readObjects("user", query);
      if (results != null && results.size() > 0) {
        feedback.put("Locations found", results);
      } else {
        HashMap<String, String> errMap = new HashMap<String, String>();
        errMap.put("error", "no match found");
        errMap.put("detail", "no matches for conditions set");
        return new ResponseToProcess(HttpURLConnection.HTTP_NOT_FOUND, errMap); // http 500 - internal server error
      }
    } catch (DatastoreException dse) {
      logger.error(dse.getMessage(), dse);
    } catch (InvalidSchemaException ise) {
      logger.error(ise.getMessage(), ise);
    }

    return new ResponseToProcess(HttpURLConnection.HTTP_OK, feedback);
  }

}
