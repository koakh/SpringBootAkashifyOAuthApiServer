- [TUTORIAL : Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)

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

Invalid CSRF Token 'null' was found on the request parameter '_csrf' or header 'X-CSRF-TOKEN'
http://stackoverflow.com/questions/21128058/invalid-csrf-token-null-was-found-on-the-request-parameter-csrf-or-header

### ToDo

- [ ] Google+
- [ ] OAuth KeyCloak
- [ ] Store users and logins in Neo4j
- [ ] Neo4J Api
- [ ] Remove angular and Work With Aurelia
- [ ] Android Connect 

### Install Developer Tools

- [LiveReload](http://livereload.com/)
- [Live realod Chrome Extension](https://chrome.google.com/webstore/detail/livereload/jnihajbhpnppcggbcgedagnkighmdlei)