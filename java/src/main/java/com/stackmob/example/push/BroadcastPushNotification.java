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

package com.stackmob.example.push;

import com.stackmob.core.PushServiceException;
import com.stackmob.core.ServiceNotActivatedException;
import com.stackmob.core.customcode.CustomCodeMethod;
import com.stackmob.core.rest.ProcessedAPIRequest;
import com.stackmob.core.rest.ResponseToProcess;
import com.stackmob.sdkapi.*;
import com.stackmob.sdkapi.PushService;

import java.net.HttpURLConnection;
import java.util.*;

/**
 * This example will show a user how to write a custom code method
 * that will broadcast a push notification to all devices registered
 * to receive push notifications.
 */

public class BroadcastPushNotification implements CustomCodeMethod {

  @Override
  public String getMethodName() {
    return "PUSH_Broadcast_Notification";
  }

  @Override
  public List<String> getParams() {
    return new ArrayList<String>();
  }

  @Override
  public ResponseToProcess execute(ProcessedAPIRequest request, SDKServiceProvider serviceProvider) {
    LoggerService logger = serviceProvider.getLoggerService(BroadcastPushNotification.class);

    Map<String, String> payload = new HashMap<String, String>();

    try {
      PushService ps = serviceProvider.getPushService();
      // Add data to your push payload
      payload.put("key1", "value1");
      payload.put("sound", "someSound.mp3");
      payload.put("alert", "Push Alert!");

      ps.broadcastPush(payload);
      logger.debug("Broadcasted Push notification");

    } catch (ServiceNotActivatedException e){
      logger.error(e.getMessage(), e);
    } catch (PushServiceException e){
      logger.error(e.getMessage(), e);
    }

    return new ResponseToProcess(HttpURLConnection.HTTP_OK, payload);
  }

}
