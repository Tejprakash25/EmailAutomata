package com.emailautomata.feature.identity;

import com.emailautomata.core.error.BusinessException;
import com.emailautomata.core.error.DuplicateResourceException;
import com.emailautomata.core.error.ErrorCode;
import com.emailautomata.core.security.AuthenticatedUser;
import com.emailautomata.core.security.JwtService;
import com.emailautomata.feature.identity.dto.AuthResponse;
import com.emailautomata.feature.identity.dto.LoginRequest;
import com.emailautomata.feature.identity.dto.RegisterRequest;
import com.emailautomata.feature.identity.dto.UserProfileResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registration and authentication.
 *
 * <p>Owns the transaction boundary and every credential rule. Controllers here
 * do no work beyond binding and delegating.</p>
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Creates an account and returns a session immediately, so registration
     * does not dead-end at a login form.
     *
     * <p>The existence check exists for a good error message; the unique index
     * is the actual guarantee. Two concurrent requests can both pass the check,
     * so the constraint violation is caught and translated to the same
     * conflict.</p>
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().toLowerCase().trim();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("User", "email", email);
        }

        User user = User.register(
                email,
                passwordEncoder.encode(request.password()),
                request.displayName()
        );

        try {
            user = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            log.debug("Concurrent registration lost the race for {}", email);
            throw new DuplicateResourceException("User", "email", email);
        }

        log.info("Registered account {}", user.getId());
        return issueSession(user);
    }

    /**
     * Verifies credentials.
     *
     * <p>Every failure — unknown address, wrong password, suspended account —
     * returns the identical error. Distinguishing them would let an attacker
     * confirm which addresses hold accounts.</p>
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository
                .findByEmailIgnoreCase(request.email().trim())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        log.info("Authenticated account {}", user.getId());
        return issueSession(user);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse currentProfile(AuthenticatedUser principal) {
        // A valid token for a since-deleted account is treated as unauthenticated.
        User user = userRepository.findById(principal.id())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHENTICATED));

        return UserProfileResponse.from(user);
    }

    private AuthResponse issueSession(User user) {
        String token = jwtService.issue(user.getId(), user.getEmail());
        return AuthResponse.of(token, jwtService.expirySeconds(), UserProfileResponse.from(user));
    }
}