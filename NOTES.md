- [TUTORIAL : Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
- [Spring boot - Google Authentication Sample](https://github.com/SoatGroup/spring-boot-google-auth)

- [OAuth 2 Developers Guide](https://projects.spring.io/spring-security-oauth/docs/oauth2.html)

- [SSO with OAuth2: Angular JS and Spring Security Part](https://spring.io/blog/2015/02/03/sso-with-oauth2-angular-js-and-spring-security-part-v)

---
### How to Links

- [Field Dependency Injection Considered Harmful](http://vojtechruzicka.com/field-dependency-injection-considered-harmful/)

---
### Server

cd Server
gradlew bootRun
http://localhost:8080

---
### Client

cd Client
gradlew bootRun
http://localhost:9999/client


```
$ curl acme:acmesecret@localhost:8080/oauth/token -d grant_type=client_credentials
{"access_token":"370592fd-b9f8-452d-816a-4fd5c6b4b8a6","token_type":"bearer","expires_in":43199,"scope":"read write"}
```

Use spring boot log random password ex

```
Using default security password: 17ee8936-28d4-47f7-b0fe-edc015442a13
```

```
security.oauth2.client.clientId = acme
security.oauth2.client.secret = ****
```

```
$ curl acme:acmesecret@localhost:8080/oauth/token -d grant_type=password -d username=user -d password=17ee8936-28d4-47f7-b0fe-edc015442a13
{"access_token":"aa49e025-c4fe-4892-86af-15af2e6b72a2","token_type":"bearer","refresh_token":"97a9f978-7aad-4af7-9329-78ff2ce9962d","expires_in":43199,"scope":"read write"}
```

```
$ request with access_token
curl http://localhost:8084/me -H "Authorization: Bearer aa49e025-c4fe-4892-86af-15af2e6b72a2"
```

Get with Access Token 
http://akashify.com:8084/me?access_token=696ab9db-4bf5-4dff-9a14-583eb69eda6c
acme:acmesecret@localhost:8080/user?access_token=0ff33a63-e0fa-4c25-92ce-fd651891be51


### ToDo

- [+] Google+
- [ ] OAuth KeyCloak
- [ ] Store users and logins in Neo4j
- [ ] Neo4J Api
- [ ] Remove angular and Work With Aurelia
- [ ] Android Connect 

### Install Developer Tools

- [LiveReload](http://livereload.com/)
- [Live realod Chrome Extension](https://chrome.google.com/webstore/detail/livereload/jnihajbhpnppcggbcgedagnkighmdlei)


http://stackoverflow.com/questions/32076503/using-postman-to-access-oauth-2-0-google-apis

Auth URL = https://accounts.google.com/o/oauth2/v2/auth
Access Token URL = https://www.googleapis.com/oauth2/v4/token

https://console.developers.google.com/apis/credentials?highlightClient=417427748635-h05mh84q1vkk6pav7cqhrrjnhe4ugkes.apps.googleusercontent.com&project=encouraging-mix-162621

Cliente OAuth
Este é seu ID do cliente
417427748635-h05mh84q1vkk6pav7cqhrrjnhe4ugkes.apps.googleusercontent.com
Esta é sua chave secreta do cliente
sHnnQaAYSF9XmTQK2BdKD9pf

https://developers.google.com/oauthplayground/?code=4/xWixvfAhKNcDMShhrLn2HBvbX5dDtSUe7MSz6QOWbGY#











A simple OAuth protected REST service built with Spring Boot and Spring Security OAuth
This is a simple REST service that provides a single RESTful endpoint protected by OAuth 2. The REST service is based on the Building a RESTful Web Service getting started guide. This project incorporates the new Java-based configuration support, now available in Spring Security OAuth 2.0. Please log any issues or feature requests to the Spring Security OAuth project.
https://github.com/royclarkson/spring-rest-service-oauth





Spring Boot Security OAuth2 with Form Login
http://stackoverflow.com/questions/31494282/spring-boot-security-oauth2-with-form-login


