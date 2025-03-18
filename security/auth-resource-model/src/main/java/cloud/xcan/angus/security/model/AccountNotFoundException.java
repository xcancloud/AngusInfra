package cloud.xcan.angus.security.model;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Thrown if an {@link UserDetailsService} implementation cannot locate a {@link CustomOAuth2User} by
 * its account.
 */
public class AccountNotFoundException extends AuthenticationException {

	/**
	 * Constructs a <code>AccountNotFoundException</code> with the specified message.
	 * @param msg the detail message.
	 */
	public AccountNotFoundException(String msg) {
		super(msg);
	}

	/**
	 * Constructs a {@code AccountNotFoundException} with the specified message and root
	 * cause.
	 * @param msg the detail message.
	 * @param cause root cause
	 */
	public AccountNotFoundException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
