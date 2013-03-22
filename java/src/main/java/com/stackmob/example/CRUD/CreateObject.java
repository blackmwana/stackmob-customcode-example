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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This example will show a user how to write a custom code method
 * with three parameters that creates an object in their schema.
 *
 */

public class CreateObject implements CustomCodeMethod {

  @Override
  public String getMethodName() {
    return "CRUD_Create";
  }

  @Override
  public List<String> getParams() {
    return Arrays.asList("model","make","year");
  }

  @Override
  public ResponseToProcess execute(ProcessedAPIRequest request, SDKServiceProvider serviceProvider) {
    String model = "";
    String make = "";
    String year = "";

    LoggerService logger = serviceProvider.getLoggerService(CreateObject.class);
    //Log the JSON object passed to the StackMob Logs
    logger.debug(request.getBody());

    JSONParser parser = new JSONParser();
    try {
      Object obj = parser.parse(request.getBody());
      JSONObject jsonObject = (JSONObject) obj;

      // Fetch the values passed in by the user from the body of JSON
      model = (String) jsonObject.get("model");
      make = (String) jsonObject.get("make");
      year = (String) jsonObject.get("year");
    } catch (ParseException pe) {
      logger.error(pe.getMessage(), pe);
    }

    // I'll be using this map to print messages to console as feedback to the operation
    Map<String, SMValue> feedback = new HashMap<String, SMValue>();

    feedback.put("model", new SMString(model));
    feedback.put("make", new SMString(make));
    feedback.put("year", new SMInt(Long.parseLong(year)));

    DataService ds = serviceProvider.getDataService();
    try {
      ds.createObject("car", new SMObject(feedback));
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

    return new ResponseToProcess(HttpURLConnection.HTTP_OK, feedback);
  }

}