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

package com.stackmob.example.crud;

import com.stackmob.core.InvalidSchemaException;
import com.stackmob.core.DatastoreException;
import com.stackmob.core.customcode.CustomCodeMethod;
import com.stackmob.core.rest.ProcessedAPIRequest;
import com.stackmob.core.rest.ResponseToProcess;
import com.stackmob.sdkapi.SDKServiceProvider;
import com.stackmob.sdkapi.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.HttpURLConnection;
import java.util.*;

public class UpdateObject implements CustomCodeMethod {

  @Override
  public String getMethodName() {
    return "update_object";
  }

  @Override
  public List<String> getParams() {
    return Arrays.asList("make","year");
  }

  @Override
  public ResponseToProcess execute(ProcessedAPIRequest request, SDKServiceProvider serviceProvider) {
    String make = "";
    String year = "";

    LoggerService logger = serviceProvider.getLoggerService(CreateObject.class);
    //Log the JSON object passed to the StackMob Logs
    logger.debug(request.getBody());

    JSONParser parser = new JSONParser();
    try {
      Object obj = parser.parse(request.getBody());
      JSONObject jsonObject = (JSONObject) obj;
      make = (String) jsonObject.get("make");
      year = (String) jsonObject.get("year");
    } catch (ParseException pe) {
      logger.error(pe.getMessage(), pe);
    }

    DataService ds = serviceProvider.getDataService();
    Map<String, SMValue> map = new HashMap<String, SMValue>();
    map.put("updated year", new SMInt(Long.parseLong(year)));

    List<SMUpdate> update = new ArrayList<SMUpdate>();
    List<SMCondition> query = new ArrayList<SMCondition>();
    // Create the changes in the form of an Update that you'd like to apply to the object
    update.add(new SMSet("year", new SMInt(Long.parseLong(year))));
    // In this case I want to make changes to year by overriding existing values with user input
    List<SMObject> results;
    try {
      // I want to make changes to any car that matches the make parameter
      query.add(new SMEquals("make", new SMString(make)));
      // Gather list of all cars matching the make parameter
      results = ds.readObjects("car", query);
      for( SMObject smo : results) { // For each car, fetch car_ ID
        String car_id = (String) smo.getValue().get("car_id").getValue();
        // Access each object by car_ID (primary key) and update object
        ds.updateObject("car", new SMString(car_id), update);
      }
    } catch (InvalidSchemaException ise) {
      HashMap<String, String> errMap = new HashMap<String, String>();
      errMap.put("error", "invalid_schema");
      errMap.put("detail", ise.toString());
      return new ResponseToProcess(HttpURLConnection.HTTP_INTERNAL_ERROR, errMap); // http 500 - internal server error

    } catch (DatastoreException dse) {
      HashMap<String, String> errMap = new HashMap<String, String>();
      errMap.put("error", "datastore_exception");
      errMap.put("detail", dse.toString());
      return new ResponseToProcess(HttpURLConnection.HTTP_INTERNAL_ERROR, errMap); // http 500 - internal server error
    }

    return new ResponseToProcess(HttpURLConnection.HTTP_OK, map);
  }

}