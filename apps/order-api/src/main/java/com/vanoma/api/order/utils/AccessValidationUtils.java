package com.vanoma.api.order.utils;

import com.vanoma.api.utils.exceptions.UnauthorizedAccessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccessValidationUtils {

    public static boolean isStaffOrService(String authHeader) {
        // Assuming JWT token signature validation happens on API Gateway.
        // Otherwise, Order API would have to be aware of secret key (beyond its scope).
        if (authHeader == null) {
            return false; // Orders via Delivery Links.
        }
        if (!isValidAuthHeaderFormat(authHeader)) {
            throw new UnauthorizedAccessException("global.invalidAuthHeader");
        }
        try {
            String jwtToken = authHeader.split("\\s")[1];
            int lastPartStart = jwtToken.lastIndexOf(".");
            String jwtTokenNoSignature = jwtToken.substring(0, lastPartStart + 1);
            // TODO: We can actually validate signature (since services share the secret key) and also throw if the
            //  token has expired. For now though, that's probably not a priority.
            Claims claims = getClaimsFromToken(jwtTokenNoSignature);
            Map<String, Object> rawClaims = new HashMap<>(claims);
            List<String> roles = (List<String>) rawClaims.get("roles");
            return roles.stream().anyMatch(role -> role.toLowerCase().contains("staff") || role.equalsIgnoreCase("service"));

        } catch (Exception e) {
            throw new UnauthorizedAccessException("global.invalidAuthHeader");
        }
    }

    private static Claims getClaimsFromToken(String jwtTokenNoSignature) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .parseClaimsJwt(jwtTokenNoSignature).getBody();
        } catch (ExpiredJwtException e) {
            claims = e.getClaims();
        }
        return claims;
    }

    private static boolean isValidAuthHeaderFormat(String authHeader) {
        String[] parts = authHeader.split("\\s");
        if (parts.length != 2) return false;
        String jwt = parts[1];
        String[] jwtParts = jwt.split("\\.");
        return jwtParts.length == 3;
    }
}
