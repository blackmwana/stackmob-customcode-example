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

import com.stackmob.core.DatastoreException;
import com.stackmob.core.InvalidSchemaException;
import com.stackmob.core.customcode.CustomCodeMethod;
import com.stackmob.core.rest.ProcessedAPIRequest;
import com.stackmob.core.rest.ResponseToProcess;
import com.stackmob.sdkapi.*;

import java.net.HttpURLConnection;
import java.util.*;

public class ReadObject implements CustomCodeMethod {

  @Override
  public String getMethodName() {
    return "read_object";
  }

  @Override
  public List<String> getParams() {
    return Arrays.asList("searchTerm");
  }

  @Override
  public ResponseToProcess execute(ProcessedAPIRequest request, SDKServiceProvider serviceProvider) {
    LoggerService logger = serviceProvider.getLoggerService(CreateObject.class);

    Map<String, SMObject> map = new HashMap<String, SMObject>();

    DataService ds = serviceProvider.getDataService();
    List<SMCondition> query = new ArrayList<SMCondition>();
    List<SMObject> results;

    try {
      // I want to find any car that matches the make parameter
      query.add(new SMEquals("make", new SMString(request.getParams().get("searchTerm"))));
      // Gather all objects in schema matching searchTerm parameter
      results = ds.readObjects("car", query);
      if (results != null && results.size() > 0) {
        for( SMObject smo : results) {
          map.put("car", smo);
        }
      } else {
        HashMap<String, String> errMap = new HashMap<String, String>();
        errMap.put("error", "no match found");
        errMap.put("detail", "no matches for the search term passed");
        return new ResponseToProcess(HttpURLConnection.HTTP_NOT_FOUND, errMap); // http 500 - internal server error
      }
    } catch (InvalidSchemaException ise) {
      logger.error(ise.getMessage(), ise);
    } catch (DatastoreException dse) {
      logger.error(dse.getMessage(), dse);
    }

    return new ResponseToProcess(HttpURLConnection.HTTP_OK, map);
  }

}
