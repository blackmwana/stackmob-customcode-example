package com.stackmob.example;

import java.net.HttpURLConnection;
import java.util.HashMap;
import com.stackmob.core.rest.ResponseToProcess;

/**
 * Created with IntelliJ IDEA.
 * User: sid
 * Date: 3/12/13
 * Time: 12:09 PM
 * To change this template use File | Settings | File Templates.
 */

public class Util {

  static public ResponseToProcess strCheck(String str, String msg) {
    boolean bool = true;

    if (str == null || str.isEmpty() ) {
      bool = false;
    }

    HashMap<String, String> errParams = new HashMap<String, String>();
    errParams.put("error", "the " +  msg + " passed was null or empty.");
    return new ResponseToProcess(HttpURLConnection.HTTP_BAD_REQUEST, errParams); // http 400 - bad request
  }
}
