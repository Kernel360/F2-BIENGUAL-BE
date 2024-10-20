package com.biengual.userapi.oauth2;

import com.biengual.userapi.message.error.exception.CommonException;
import com.biengual.userapi.oauth2.domain.info.OAuth2UserPrincipal;
import com.biengual.userapi.user.domain.entity.UserEntity;
import com.biengual.userapi.user.service.UserService;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Date;

import static com.biengual.userapi.message.error.code.TokenErrorCode.TOKEN_EXPIRED;
import static com.biengual.userapi.message.error.code.TokenErrorCode.TOKEN_INVALID;

@Service
@RequiredArgsConstructor
public class TokenProvider {
	private final JwtProperties jwtProperties;
	private final UserService userService;

	public static final Duration ACCESS_TOKEN_EXPIRE = Duration.ofDays(1);
	public static final Duration REFRESH_TOKEN_EXPIRE = Duration.ofDays(7);

	public String generateAccessToken(UserEntity user) {
		return generateToken(user, ACCESS_TOKEN_EXPIRE);
	}

	public String generateRefreshToken(UserEntity user) {
		return generateToken(user, REFRESH_TOKEN_EXPIRE);
	}

	private String generateToken(UserEntity user, Duration expiredAt) {
		Date now = new Date();
		return Jwts.builder()
			.setHeaderParam(Header.TYPE, Header.JWT_TYPE)
			.setIssuer(jwtProperties.getIssuer())
			.setIssuedAt(now)
			.setExpiration(new Date(now.getTime() + expiredAt.toMillis()))
			.setSubject(user.getEmail())
			.claim("id", user.getId())
			.claim("role", user.getRole().name())
			.signWith(SignatureAlgorithm.HS256, jwtProperties.getSecret())
			.compact();
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parser().setSigningKey(jwtProperties.getSecret()).parseClaimsJws(token);
			return true;
		} catch (ExpiredJwtException e) {
			throw new CommonException(TOKEN_EXPIRED);
		} catch (Exception e) {
			throw new CommonException(TOKEN_INVALID);
		}
	}

	@Transactional(readOnly = true)
	public Authentication getAuthentication(String token) {
		Claims claims = getClaims(token);

		UserEntity user = userService.getUserById(claims.get("id", Long.class));

		OAuth2UserPrincipal principal = OAuth2UserPrincipal.from(user);

		return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
	}

	private Claims getClaims(String token) {
		return Jwts.parser()
			.setSigningKey(jwtProperties.getSecret())
			.parseClaimsJws(token)
			.getBody();
	}
}
