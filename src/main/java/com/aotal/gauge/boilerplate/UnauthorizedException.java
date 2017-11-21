package com.aotal.gauge.boilerplate;

import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * The joy of spring - all to throw a 403 error.
 * 
 * @author abraae
 *
 */
@ResponseStatus(value = org.springframework.http.HttpStatus.FORBIDDEN)
public class UnauthorizedException extends RuntimeException {

	public UnauthorizedException() {
		// TODO Auto-generated constructor stub
	}

	public UnauthorizedException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public UnauthorizedException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public UnauthorizedException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public UnauthorizedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
