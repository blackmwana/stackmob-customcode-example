/**
 * Copyright 2013 StackMob
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

import com.stackmob.core.DatastoreException;
import com.stackmob.example.Util;
import com.stackmob.core.customcode.CustomCodeMethod;
import com.stackmob.core.rest.ProcessedAPIRequest;
import com.stackmob.core.rest.ResponseToProcess;
import com.stackmob.sdkapi.SDKServiceProvider;

import com.stackmob.core.ServiceNotActivatedException;
import com.stackmob.sdkapi.PushService;
import com.stackmob.sdkapi.PushService.TokenAndType;
import com.stackmob.sdkapi.PushService.TokenType;
import com.stackmob.sdkapi.LoggerService;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a custom code example to show how to register a specific mobile device
 * to receive push notifications. Please note that different services have different
 * requirements for tokens. iOS requires the token to be a 64 char hex string, whereas
 * Android does not have such requirements.
 */

public class SMPushRegisterDevice implements CustomCodeMethod {

  @Override
  public String getMethodName() {
    return "PUSH_register_device";
  }

  @Override
  public List<String> getParams() {
    return Arrays.asList("device_token","user_name","token_type");
  }

  @Override
  public ResponseToProcess execute(ProcessedAPIRequest request, SDKServiceProvider serviceProvider) {
    int responseCode = 0;
    String responseBody = "";
    TokenType deviceTokenType;

    LoggerService logger = serviceProvider.getLoggerService(SMPushRegisterDevice.class);  //Log to the StackMob Custom code console
    logger.debug("Start register device token");

    String deviceToken = request.getParams().get("device_token");  // DEVICE TOKEN should be YOUR mobile device token
    String tokenType = request.getParams().get("token_type");  // TOKEN TYPE should be YOUR device type (ios / gcm)
    logger.debug("Device token: " + deviceToken + ", " + "Token Type: " + tokenType);

    if (Util.strNullCheck(deviceToken)) {
      HashMap<String, String> errParams = new HashMap<String, String>();
      errParams.put("error", "the device token passed was null or empty.");
      return new ResponseToProcess(HttpURLConnection.HTTP_BAD_REQUEST, errParams); // http 400 - bad request
    }

    if (Util.strNullCheck(tokenType)) {
      HashMap<String, String> errParams = new HashMap<String, String>();
      errParams.put("error", "the token type passed was null or empty.");
      return new ResponseToProcess(HttpURLConnection.HTTP_BAD_REQUEST, errParams); // http 400 - bad request
    }

    // Check if the input token type was one of the 3 supported services
    if (tokenType.equals("ios")) {
      deviceTokenType = TokenType.iOS;
    } else if  (tokenType.equals("gcm")) {
      deviceTokenType = TokenType.AndroidGCM;
    } else if  (tokenType.equals("c2dm")) {
      deviceTokenType = TokenType.Android;
    } else {
      HashMap<String, String> errParams = new HashMap<String, String>();
      errParams.put("error", "the token type passed was not valid, must be ios, c2dm or gcm");
      return new ResponseToProcess(HttpURLConnection.HTTP_BAD_REQUEST, errParams); // http 400 - bad request
    }

    TokenAndType token = new TokenAndType(deviceToken, deviceTokenType); // token type can be iOS or GCM
    String username = request.getParams().get("user_name"); // (OPTIONAL) USERNAME to register a token to a specific username

    try {
      PushService service = serviceProvider.getPushService();
      service.registerTokenForUser(username, token);
      responseCode = HttpURLConnection.HTTP_OK;
      responseBody = "token saved";
    } catch (ServiceNotActivatedException e) {
      logger.error("error service not active" + e.toString());
      responseCode = HttpURLConnection.HTTP_INTERNAL_ERROR; // error 500
      responseBody = e.toString();
    } catch (DatastoreException e) {
      logger.error("DataStore Exception: " + e.toString(), e);
      responseCode = HttpURLConnection.HTTP_INTERNAL_ERROR;
      responseBody = e.toString();
    }

    logger.debug("End register device token code");

    Map<String, Object> map = new HashMap<String, Object>();
    map.put("response_body", responseBody);

    return new ResponseToProcess(responseCode, map);
  }
}