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

import java.net.HttpURLConnection;
import java.util.*;

/**
 * This example will show a user how to write a custom code method
 * with one parameter `year` that queries the `car` schema for all objects
 * that match the condition (ie greater than, less than, order by) when
 * applied to the given year field
 */

public class PaginateResults implements CustomCodeMethod {

  @Override
  public String getMethodName() {
    return "CRUD_Pagination";
  }

  @Override
  public List<String> getParams() {
    return new ArrayList<String>();
  }

  @Override
  public ResponseToProcess execute(ProcessedAPIRequest request, SDKServiceProvider serviceProvider) {
    LoggerService logger = serviceProvider.getLoggerService(CreateObject.class);
    Map<String, List<SMObject>> feedback = new HashMap<String, List<SMObject>>();

    // Make a new ResultFilter that starts at 0 and ends at 9 to paginate at every 10 results
    ResultFilters filters = new ResultFilters(0, 9, null, null);
    DataService ds = serviceProvider.getDataService();
    List<SMObject> results;

    try {
      results = ds.readObjects("car", new ArrayList<SMCondition>(), 0, filters);
      if (results != null && results.size() > 0) {
        feedback.put("results", results);
      } else {
        HashMap<String, String> errMap = new HashMap<String, String>();
        errMap.put("error", "no match found");
        errMap.put("detail", "no matches for that ID");
        return new ResponseToProcess(HttpURLConnection.HTTP_NOT_FOUND, errMap); // http 500 - internal server error
      }
    } catch (InvalidSchemaException ise) {
      logger.error(ise.getMessage(), ise);
    } catch (DatastoreException dse) {
      logger.error(dse.getMessage(), dse);
    }

    return new ResponseToProcess(HttpURLConnection.HTTP_OK, feedback);
  }

}
