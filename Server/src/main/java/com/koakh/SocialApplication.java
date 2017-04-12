package com.koakh;

import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.CompositeFilter;

@SpringBootApplication
@EnableOAuth2Client
@RestController
@EnableAuthorizationServer
@EnableAutoConfiguration
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class SocialApplication extends WebSecurityConfigurerAdapter {

  @Value("${security.oauth2.client.client-id}")
  private String acmeClientId;
  @Value("${facebook.client.clientId}")
  private String facebookClientId;
  @Value("${github.client.clientId}")
  private String gitHubClientId;
  @Value("${google.client.clientId}")
  private String googleClientId;

  private OAuth2ClientContext oauth2ClientContext;

  @Autowired
  public SocialApplication(OAuth2ClientContext oauth2ClientContext) {
    this.oauth2ClientContext = oauth2ClientContext;
  }

  public static void main(String[] args) {
    SpringApplication.run(SocialApplication.class, args);
  }

  @RequestMapping({"/user", "/me"})
  public Map<String, String> user(Principal principal) {
    // Mapper
    Map<String, String> map = new LinkedHashMap<>();
    map.put("name", principal.getName());
    // ClientId
    String clientId = ((OAuth2Authentication) principal).getOAuth2Request().getClientId();
    // OAuth2AuthenticationDetails
    OAuth2AuthenticationDetails oAuth2Details = ((OAuth2AuthenticationDetails) ((OAuth2Authentication) principal).getDetails());
    String remoteAddress = oAuth2Details.getRemoteAddress();
    String sessionId = oAuth2Details.getSessionId();
    String tokenType = oAuth2Details.getTokenType();
    String tokenValue = oAuth2Details.getTokenValue();
    // UserAuthenticationDetails
    Object authenticationDetails = null;
    Object[] roles = null;
    Object[] scopes = null;
    if (((OAuth2Authentication) principal).getUserAuthentication() != null) {
      authenticationDetails = ((OAuth2Authentication) principal).getUserAuthentication().getDetails();
      roles = ((OAuth2Authentication) principal).getUserAuthentication().getAuthorities().toArray();
      scopes = ((OAuth2Authentication) principal).getOAuth2Request().getScope().toArray();
    }
    // Helpers
    String userId = null;
    String dbKey = null;

    // github and facebook clients
    if (authenticationDetails instanceof LinkedHashMap) {
      LinkedHashMap userDetails = ((LinkedHashMap) ((OAuth2Authentication) principal).getUserAuthentication().getDetails());
      boolean authenticated = ((OAuth2Authentication) principal).getUserAuthentication().isAuthenticated();

      // Shared OAuth Properties
      map.put("client", clientId);
      map.put("user", principal.getName());
      map.put("authenticated", (authenticated) ? "true" : "false");

      // Acme Client
      if (clientId.equals(acmeClientId)) {
        map.put("grant_type", userDetails.get("grant_type").toString());
        map.put("username", userDetails.get("username").toString());
      }
      // Shared for all other clients
      else {
        map.put("fullName", userDetails.get("name").toString());
      }

      // Social Clients
      if (clientId.equals(gitHubClientId)) {
        userId = userDetails.get("id").toString();
        map.put("id", userId);
        map.put("login", userDetails.get("login").toString());
        map.put("email", userDetails.get("email").toString());
        map.put("avatar", userDetails.get("avatar_url").toString());
        map.put("url", userDetails.get("url").toString());
      } else if (clientId.equals(facebookClientId)) {
        userId = userDetails.get("id").toString();
        map.put("id", userId);
        map.put("login", null);
        map.put("email", userDetails.get("email").toString());
        map.put("avatar", ((LinkedHashMap) ((LinkedHashMap) userDetails.get("picture")).get("data")).get("url").toString());
        map.put("url", (userDetails.get("link") != null) ? userDetails.get("link").toString() : null);
      } else if (clientId.equals(googleClientId)) {
        userId = userDetails.get("sub").toString();
        map.put("id", userId);
        map.put("login", userDetails.get("email").toString());
        map.put("email", userDetails.get("email").toString());
        map.put("avatar", userDetails.get("picture").toString());
        map.put("url", userDetails.get("profile").toString());
      }

      // Assign dbKey
      dbKey = String.format("%s.%s", clientId, userId);
    }

    return map;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // @formatter:off
    http
        // All requests are protected by default
        .antMatcher("/**")
        // The home page and login endpoints are explicitly excluded
        .authorizeRequests().antMatchers("/", "/login**", "/webjars/**").permitAll()
        // All other endpoints require an authenticated user
        .anyRequest().authenticated()
        .and().exceptionHandling()
        // Auth Server : Unauthenticated users are re-directed to the home page
        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/"))
        .and().logout().logoutSuccessUrl("/").permitAll()
        // Angular csrf
        .and().csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        // OAuth2ClientContext Filter
        .and().addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class);
    // @formatter:on
  }

  @Configuration
  @EnableResourceServer
  protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
    @Override
    public void configure(HttpSecurity http) throws Exception {
      http.antMatcher("/me")
          .authorizeRequests().anyRequest().authenticated();
    }
  }

  @Bean
  // Handling the Redirects Filter
  // We autowire the already available filter, and register it with a sufficiently low order that it comes before the main Spring Security filter.
  // In this way we can use it to handle redirects signaled by exceptions in authentication requests
  public FilterRegistrationBean oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
    FilterRegistrationBean registration = new FilterRegistrationBean();
    registration.setFilter(filter);
    registration.setOrder(-100);
    return registration;
  }

  @Bean
  @ConfigurationProperties("github")
  public ClientResources github() {
    return new ClientResources();
  }

  @Bean
  @ConfigurationProperties("facebook")
  public ClientResources facebook() {
    return new ClientResources();
  }

  @Bean
  @ConfigurationProperties("google")
  public ClientResources google() {
    return new ClientResources();
  }

  // Configure Filters
  private Filter ssoFilter() {
    CompositeFilter filter = new CompositeFilter();
    List<Filter> filters = new ArrayList<>();
    filters.add(ssoFilter(facebook(), "/login/facebook"));
    filters.add(ssoFilter(github(), "/login/github"));
    filters.add(ssoFilter(google(), "/login/google"));
    filter.setFilters(filters);
    return filter;
  }

  // Shared method to Configure Filters
  private Filter ssoFilter(ClientResources client, String path) {
    OAuth2ClientAuthenticationProcessingFilter filter = new OAuth2ClientAuthenticationProcessingFilter(path);
    OAuth2RestTemplate template = new OAuth2RestTemplate(client.getClient(), oauth2ClientContext);
    filter.setRestTemplate(template);
    UserInfoTokenServices tokenServices = new UserInfoTokenServices(
        client.getResource().getUserInfoUri(),
        client.getClient().getClientId()
    );
    tokenServices.setRestTemplate(template);
    filter.setTokenServices(tokenServices);
    return filter;
  }
}
