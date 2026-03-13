package com.open.spring.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**JwtAuthenticationEntryPoint
 * Implements AuthenticationEntryPoint, an interface in Spring Security. 
 * Defines response when an unauthenticated user tries to access a protected resource.
 * 
 * @Component is a Spring annotation, which means Spring will automatically create an instance of it.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Session expired or not authenticated. Please log in again.\"}");
		response.getWriter().flush();
	}
}