package com.inria.spirals.mgonzale.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.inria.spirals.mgonzale.services.DiscoveryService;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 2016/12/4
 */
@RestController
public class GrpcClientController {

    @Autowired
    private DiscoveryService dService;

@RequestMapping(method = RequestMethod.GET, value = "/start", produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, String> initFail() {
    Map<String, String> message = new HashMap<>();
      dService.testFail();
    message.put("status", "BEGIN TEST");
    return message;
  }
}
