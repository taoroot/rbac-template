package com.github.taoroot.common.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AuthJwtDecoder implements JwtDecoder {

    private final Map<String, String> map;

    public AuthJwtDecoder(HashMap<String, String> secretMap) {
        map = secretMap;
    }

    public void addSecret(String iss, String secret) {
        map.put(iss, secret);
    }

    public String getSecret(String iss) {
        return map.get(iss);
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            JWT parsedJwt = JWTParser.parse(token);
            JWSObject parse = JWSObject.parse(token);

            Map<String, Object> headers = new LinkedHashMap<>(parsedJwt.getHeader().toJSONObject());
            Map<String, Object> claims = parsedJwt.getJWTClaimsSet().getClaims();

            String iss = (String) claims.get("iss");

            boolean verify = parse.verify(new MACVerifier(getSecret(iss)));
            if (!verify) {
                throw new JwtException("TOKEN不合法");
            }

            Date exp = (Date) claims.get("exp");

            if (exp == null) {
                throw new JwtException("TOKEN不合法");
            }

            if (exp.before(new Date())) {
                throw new JwtException("token过期");
            }

            HashMap<String, Object> claims1 = new HashMap<>(claims);
            claims1.put("exp", exp.toInstant());

            return Jwt.withTokenValue(token)
                    .headers(h -> h.putAll(headers))
                    .claims(c -> c.putAll(claims1))
                    .build();
        } catch (JOSEException | ParseException e) {
            throw new JwtException(e.getMessage(), e.getCause());
        }
    }
}
