/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.syncleus.dann.data;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

/**
 *
 * @see https://github.com/FasterXML/jackson-module-jaxb-annotations
 * https://jersey.java.net/documentation/1.18/json.html
 */
public class JSONTest {

    public void testJAXB() {
        final JaxbAnnotationModule module = new JaxbAnnotationModule();
        // configure as necessary

        //objectMapper.registerModule(module);
    }

    public void testLowLevelJSONConstruct() {
        final JSONObject myObject = new JSONObject();
        try {
            myObject.put("a", "b");
        } catch (final JSONException ex) {

        }

    }
}
