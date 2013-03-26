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

package com.stackmob.example.relations;

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
 * with one parameter that creates an object in the car schema
 * and relates it to a parent User object
 */

public class OneStepCreateRelate implements CustomCodeMethod {

  @Override
  public String getMethodName() {
    return "One_Step_Create_Relate";
  }

  @Override
  public List<String> getParams() {
    return Arrays.asList("name");
  }

  @Override
  public ResponseToProcess execute(ProcessedAPIRequest request, SDKServiceProvider serviceProvider) {
    Map<String, List<SMObject>> feedback = new HashMap<String, List<SMObject>>();
    LoggerService logger = serviceProvider.getLoggerService(OneStepCreateRelate.class);


    // These are some example cars that will be created
    Map<String, SMValue> carValues1 = new HashMap<String, SMValue>();
    carValues1.put("make", new SMString("Audi"));
    carValues1.put("model", new SMString("R8"));
    carValues1.put("year", new SMInt(2005L));
    Map<String, SMValue> carValues2 = new HashMap<String, SMValue>();
    carValues2.put("make", new SMString("Audi"));
    carValues2.put("model", new SMString("spyder"));
    carValues2.put("year", new SMInt(2005L));

    SMObject car1 = new SMObject(carValues1);
    SMObject car2 = new SMObject(carValues2);

    List<SMObject> cars = new ArrayList<SMObject>();
    cars.add(car1);
    cars.add(car2);

    DataService ds = serviceProvider.getDataService();

    try {
      /**
       * In the `user` schema we are going to add our list of `cars` to the `garage` (one-to-many) relation
       * found in the `user` specified by the input
       */

      SMString owner = new SMString(request.getParams().get("name"));
      BulkResult result = ds.createRelatedObjects("user", owner, "garage", cars);
      feedback.put(owner.getValue() + " now owns", cars);
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