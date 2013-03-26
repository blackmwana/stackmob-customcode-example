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

public class RelateToParent implements CustomCodeMethod {

  @Override
  public String getMethodName() {
    return "Relate_Existing_To_Parent";
  }

  @Override
  public List<String> getParams() {
    return Arrays.asList("car_ID", "user_name");
  }

  @Override
  public ResponseToProcess execute(ProcessedAPIRequest request, SDKServiceProvider serviceProvider) {
    Map<String, SMObject> feedback = new HashMap<String, SMObject>();
    LoggerService logger = serviceProvider.getLoggerService(RelateToParent.class);

    DataService ds = serviceProvider.getDataService();
    List<SMValue> valuesToAppend = new ArrayList<SMValue>();
    SMObject result;

    try {
      /**
       * In the `user` schema we are going to add the car specified by ID to the `garage` (one-to-many) relation
       * specified by the input `user_name`
       */
      SMString owner = new SMString(request.getParams().get("user_name"));
      SMString carID = new SMString(request.getParams().get("car_ID"));

      valuesToAppend.add(carID);

      result = ds.addRelatedObjects("user", owner, "garage", valuesToAppend);
      feedback.put("added " + carID.getValue() + "to", result);
    } catch (InvalidSchemaException ise) {
      logger.error(ise.getMessage(), ise);
    } catch (DatastoreException dse) {
      logger.error(dse.getMessage(), dse);
    }

    return new ResponseToProcess(HttpURLConnection.HTTP_OK, feedback);
  }

}
