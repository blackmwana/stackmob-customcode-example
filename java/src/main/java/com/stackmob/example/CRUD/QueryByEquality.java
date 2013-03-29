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

public class QueryByEquality implements CustomCodeMethod {

  @Override
  public String getMethodName() {
    return "CRUD_Query_By_Equality";
  }

  @Override
  public List<String> getParams() {
    return Arrays.asList("year");
  }

  @Override
  public ResponseToProcess execute(ProcessedAPIRequest request, SDKServiceProvider serviceProvider) {

    Map<String, List<SMObject>> feedback = new HashMap<String, List<SMObject>>();
    List<SMCondition> query = new ArrayList<SMCondition>();

    // We are going to primarily sort by year (ascending) and then by createddate in reverse-chrono
    List<SMOrdering> orderings = Arrays.asList(
            new SMOrdering("year", OrderingDirection.ASCENDING),
            new SMOrdering("createddate", OrderingDirection.DESCENDING));
    ResultFilters filters = new ResultFilters(0, -1, orderings, null);

    DataService ds = serviceProvider.getDataService();
    List<SMObject> results;

    try {
      SMInt year = new SMInt(Long.parseLong(request.getParams().get("year")));

      // We only want years greater than or equal to the user input
      query.add(new SMGreaterOrEqual("year", year));
      results = ds.readObjects("car", query, 0, filters);

      if (results != null && results.size() > 0) {
        feedback.put("results", results);
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

    return new ResponseToProcess(HttpURLConnection.HTTP_OK, feedback);
  }

}