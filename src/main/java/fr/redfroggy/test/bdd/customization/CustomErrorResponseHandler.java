package fr.redfroggy.test.bdd.customization;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.DefaultResponseErrorHandler;

/*
This Implementation of ResponseErrorHandler allows us
to get the status code and body even if the request is not in the 2xx range
*/
public class CustomErrorResponseHandler extends DefaultResponseErrorHandler {

  @Override
  protected boolean hasError(HttpStatus statusCode) {
    return false;
  }
}
