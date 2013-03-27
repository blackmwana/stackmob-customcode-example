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

package com.stackmob.example.util;

import com.stackmob.core.DatastoreException;
import com.stackmob.core.InvalidSchemaException;
import com.stackmob.core.customcode.CustomCodeMethod;
import com.stackmob.core.rest.ProcessedAPIRequest;
import com.stackmob.core.rest.ResponseToProcess;
import com.stackmob.sdkapi.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.HttpURLConnection;
import java.util.*;

/**
 * This example will show a user how to write a custom code method
 * that will read all geopoint values from the `user` schema and return
 * any results that fall both within a "Near" radius and within
 * a square, 2D area defined by 'WithinBox'.
 */

public class WriteGeo implements CustomCodeMethod {

  @Override
  public String getMethodName() {
    return "Write_Geopoint";
  }

  @Override
  public List<String> getParams() {
    return Arrays.asList("user_name", "Latitude", "Longitude");
  }

  @Override
  public ResponseToProcess execute(ProcessedAPIRequest request, SDKServiceProvider serviceProvider) {
    LoggerService logger = serviceProvider.getLoggerService(WriteGeo.class);
    Map<String, SMObject> feedback = new HashMap<String, SMObject>();

    String user      = "";
    String latitude  = "";
    String longitude = "";

    JSONParser parser = new JSONParser();
    try {
      Object obj = parser.parse(request.getBody());
      JSONObject jsonObject = (JSONObject) obj;
      user      = (String) jsonObject.get("user_name");
      latitude  = (String) jsonObject.get("Latitude");
      longitude = (String) jsonObject.get("Longitude");
    } catch (ParseException pe) {
      logger.error(pe.getMessage(), pe);
    }

    DataService ds = serviceProvider.getDataService();
    List<SMUpdate> update = new ArrayList<SMUpdate>();
    Map<String, SMValue> geoPoint = new HashMap<String, SMValue>();
    SMObject result;

    try {
      geoPoint.put("lat", new SMDouble(Double.parseDouble(latitude)));
      geoPoint.put("lon", new SMDouble(Double.parseDouble(longitude)));
      update.add(new SMSet("position", new SMObject(geoPoint)));
    } catch (NumberFormatException nfe) {
      logger.error(nfe.getMessage(), nfe);
    }

    try {
      result = ds.updateObject("user", new SMString(user), update);
      feedback.put("Updated object", result);
    } catch (DatastoreException dse) {
      logger.error(dse.getMessage(), dse);
    } catch (InvalidSchemaException ise) {
      logger.error(ise.getMessage(), ise);
    }

    return new ResponseToProcess(HttpURLConnection.HTTP_OK, feedback);
  }

}
