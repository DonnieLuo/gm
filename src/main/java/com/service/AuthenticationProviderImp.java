package com.service;

import com.Entity.User;
import com.repository.LogRepository;
import com.repository.UserRepository;
import com.service.UserDetailService;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Created by Donnie on 2017/3/1.
 */
@Service
public class AuthenticationProviderImp extends AbstractUserDetailsAuthenticationProvider {

    public AuthenticationProviderImp() {}
    private static final String USER_NOT_FOUND_PASSWORD = "userNotFoundPassword";
    @Autowired
    private UserRepository userRepository;
    @Autowired
    protected UserDetailService userDetailService;
    private Md5PasswordEncoder md5PasswordEncoder =  new Md5PasswordEncoder();

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        logger.debug("additionalAuthenticationChecks");
        if (authentication.getCredentials() == null) {
            logger.debug("Authentication failed: no credentials provided");

            throw new BadCredentialsException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials",
                    "Bad credentials"));
        }
        User user = userRepository.findByUsername(userDetails.getUsername());
        String rawPass = authentication.getCredentials().toString();
        String encodePassword = DigestUtils.md5Hex(rawPass);

        boolean b = encodePassword.equals(userDetails.getPassword());
        if (!b) {
            logger.debug("Authentication failed: password does not match stored value");

            throw new BadCredentialsException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials",
                    "Bad credentials"));
        }
        else logger.debug("----password ok------");
    }

    @Override
    protected void doAfterPropertiesSet() throws Exception {
        Assert.notNull(this.userDetailService, "A UserDetailsService must be set");
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        UserDetails loadedUser;

        try {
            loadedUser = this.userDetailService.loadUserByUsername(username);
//            loadedUser = new UserDetailService().loadUserByUsername(username);
        } catch (UsernameNotFoundException notFound) {
            if(authentication.getCredentials() != null) {
                String presentedPassword = authentication.getCredentials().toString();
                String userNotFoundEncodedPassword = md5PasswordEncoder.encodePassword(USER_NOT_FOUND_PASSWORD, null);
                md5PasswordEncoder.isPasswordValid(userNotFoundEncodedPassword, presentedPassword, null);
            }
            throw notFound;
        } catch (Exception repositoryProblem) {
            logger.error(repositoryProblem.toString());
            throw new InternalAuthenticationServiceException(repositoryProblem.getMessage(), repositoryProblem);
        }

        if (loadedUser == null) {
            throw new InternalAuthenticationServiceException(
                    "UserDetailsService returned null, which is an interface contract violation");
        }
        return loadedUser;
    }

}
