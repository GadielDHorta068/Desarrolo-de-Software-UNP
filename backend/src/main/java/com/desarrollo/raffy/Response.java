package com.desarrollo.raffy;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Response {

	public static ResponseEntity<Object> response(HttpStatus status, String message, Object responseObj) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("status", status.value());
		map.put("message", message);		
		map.put("data", responseObj);

		return new ResponseEntity<Object>(map, status);
	}

	// response generico en caso exitoso
	public static ResponseEntity<Object> responseOk(String message, Object responseObj) {
		Map<String, Object> response = new HashMap<String, Object>();
		
		response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.OK.value());
        response.put("data", responseObj);
        response.put("message", message);
        response.put("path", null);
		response.put("error", null);

		return new ResponseEntity<Object>(response, status);
	}

	public static ResponseEntity<Object> ok(Object responseObj) {
		return response(HttpStatus.OK, "OK", responseObj);
	}

	public static ResponseEntity<Object> ok(Object responseObj, String msj) {
		return response(HttpStatus.OK, msj, responseObj);
	}

    public static ResponseEntity<Object> notFound() {
        return response(HttpStatus.NOT_FOUND, "Not found", null);
    }

	public static ResponseEntity<Object> notFound(String msj) {
        return response(HttpStatus.NOT_FOUND, msj, null);
    }

    public static ResponseEntity<Object> error(Object responseObj) {
		return response(HttpStatus.BAD_REQUEST, "Error", responseObj);
	}

	public static ResponseEntity<Object> error(Object responseObj, String msj) {
		return response(HttpStatus.BAD_REQUEST, msj, responseObj);
	}

    public static ResponseEntity<Object> conflict(Object responseObj, String msj) {
        return response(HttpStatus.CONFLICT, msj, responseObj);
    }
    
}