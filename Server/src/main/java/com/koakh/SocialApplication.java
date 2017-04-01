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
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
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
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class SocialApplication extends WebSecurityConfigurerAdapter {

  @Autowired
  private OAuth2ClientContext oauth2ClientContext;

  @Value("${facebook.client.clientId}")
  private String facebookClientId;
  @Value("${github.client.clientId}")
  private String gitHubClientId;

  public static void main(String[] args) {
    SpringApplication.run(SocialApplication.class, args);
  }

  //@RequestMapping({"/user", "/me"})
  //public Map<String, String> user(Principal principal) {
  //  Map<String, String> map = new LinkedHashMap<>();
  //  map.put("name", principal.getName());
  //  return map;
  //}

  //class org.springframework.security.oauth2.provider.OAuth2Authentication

  //java.lang.ClassCastException: org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails
  // cannot be cast to java.util.LinkedHashMap

  //acme
  //((OAuth2Authentication) principal).getUserAuthentication().getDetails().getClass() = OAuth2AuthenticationDetails

  @RequestMapping({"/user", "/me"})
  public Map<String, String> user(Principal principal) {
    Map<String, String> map = new LinkedHashMap<>();
    map.put("name", principal.getName());
    String clientId = ((OAuth2Authentication) principal).getOAuth2Request().getClientId();
    String clientName = null;

    Object getUserAuthenticationDetails = ((OAuth2Authentication) principal).getUserAuthentication().getDetails();

    // github and facebook clients
    if (getUserAuthenticationDetails instanceof LinkedHashMap) {
      LinkedHashMap userDetails = ((LinkedHashMap) ((OAuth2Authentication) principal).getUserAuthentication().getDetails());
      String dbKey = dbKey = String.format("%s.%s", clientId, userDetails.get("id").toString());

      // Shared OAuth Properties
      map.put("user", principal.getName());
      map.put("id", userDetails.get("id").toString());
      map.put("fullName", userDetails.get("name").toString());

      if (clientId.equals(gitHubClientId)) {
        clientName = "github";
        map.put("login", userDetails.get("login").toString());
        map.put("email", userDetails.get("email").toString());
        map.put("avatar", userDetails.get("avatar_url").toString());
        map.put("url", userDetails.get("url").toString());
      } else if (clientId.equals(facebookClientId)) {
        clientName = "facebook";
        map.put("login", null);
        map.put("email", null);
        map.put("avatar", null);
        map.put("url", userDetails.get("link").toString());
      }
    }
    else {
      clientName = "acme";
    }

    map.put("client", clientName);
    return map;
  }

  @RequestMapping("/unauthenticated")
  public String unauthenticated() {
    return "redirect:/?error=true";
  }

  //@Override
  //protected void configure(HttpSecurity http) throws Exception {
  //  // @formatter:off
  //  http.antMatcher("/**").authorizeRequests().antMatchers("/", "/login**", "/webjars/**").permitAll().anyRequest()
  //      .authenticated().and().exceptionHandling()
  //      .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/")).and().logout()
  //      .logoutSuccessUrl("/").permitAll().and().csrf()
  //      .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).and()
  //      .addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class);
  //  // @formatter:on
  //}

  @Override
  protected void configure(HttpSecurity http) throws Exception {
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
  }

  @Configuration
  @EnableResourceServer
  protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
    @Override
    public void configure(HttpSecurity http) throws Exception {
      // @formatter:off
      http.antMatcher("/me")
          .authorizeRequests().anyRequest().authenticated();
      // @formatter:on
    }
  }

  @Bean
  // Handling the Redirects Filter
  // We autowire the already available filter, and register it with a sufficiently low order that it comes before the main Spring Security filter.
  // In this way we can use it to handle redirects signaled by expceptions in authentication requests
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

  // Configure Filters
  private Filter ssoFilter() {
    CompositeFilter filter = new CompositeFilter();
    List<Filter> filters = new ArrayList<>();
    filters.add(ssoFilter(facebook(), "/login/facebook"));
    filters.add(ssoFilter(github(), "/login/github"));
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

  @Bean
  public AuthoritiesExtractor authoritiesExtractor(OAuth2RestOperations template) {
    return map -> {
      String url = (String) map.get("organizations_url");
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> orgs = template.getForObject(url, List.class);
      if (orgs.stream()
          .anyMatch(org -> "spring-projects".equals(org.get("login")))) {
        return AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER");
      }
      throw new BadCredentialsException("Not in Spring Projects origanization");
    };
  }
}
