# JWT REST client container example

A minimal container that uses the temporary credentials Kura provides when **identity integration** is enabled on a
container instance. It:

1. reads the JWT token pair from the file referenced by `KURA_JWT_FILE`;
2. periodically calls `GET {KURA_REST_BASE_URL}/inventory/v1/images` with the access token;
3. exchanges the refresh token for a new pair with `POST {KURA_REST_BASE_URL}/token/jwt/v1/refresh` before the access token expires, keeping the new pair in memory;
4. exits with status `1` when Kura rejects its tokens (HTTP 401), for example because the refresh token expired or the temporary identity was deleted.

The client only uses the Python standard library.

## Prerequisites

### Build and deploy the container

On the device, or on a machine pushing to a registry reachable by the device:

```bash
docker build -t kura-jwt-rest-client:latest .
```

Then configure a container instance with `Image name` `kura-jwt-rest-client` and `Image tag` `latest`. Kura does not pull an image that is already available locally.

### Configure Kura and run

- **JWT Issuing Service** (`org.eclipse.kura.core.token.jwt.issuer.JwtIssuingService`) configured with a keystore containing the signing key. Refer to the [official Kura documentation](https://eclipse-kura.github.io/kura/latest) for more details.
- **REST Service**: JWT authentication enabled and a Token Verification Service that trusts the issuer used by the container instance. Refer to the [official Kura documentation](https://eclipse-kura.github.io/kura/latest) for more details.
- **Container instance**:
    - `Image name` set to `kura-jwt-rest-client`;
    - `Image tag` set to `latest`
    - `Enable Identity Integration` set to `true`;
    - `Container Permissions` containing `rest.inventory`;
    - `JWT Access Token Duration (Seconds)` set to 30 seconds
    - `JWT Refresh Token Duration (Seconds)` set to 60 seconds
    - `Token Issuing Service Target` selecting the issuer above.

### Verify it works

From the Kura logs and the kura Audit logs you should see the client performing calls to `GET {KURA_REST_BASE_URL}/inventory/v1/images` every 10 seconds. Before the refresh tokens expires, a call to the `POST {KURA_REST_BASE_URL}/token/jwt/v1/refresh` is made to obtain a new token pair and the loop continues.

From the Kura audit log (`tail -F /var/log/kura-audit.log`), after about 60 seconds you should see that a token refresh is requested:

```
<134>1 2026-10-07T15:58:11.672+02:00 raspberry EclipseKura 8727 - [RequestContext@28392 category="AuditLogger" exception="" priority="INFO" thread="qtp1055114709-115"] {rest.method=POST, rest.path=token/jwt/v1/refresh, entrypoint=RestService, identity=container_kura_jwt_rest_client_017969ce, ip=172.17.0.2} Rest - Success - JWT token pair refreshed
<134>1 2026-10-07T15:58:11.672+02:00 raspberry EclipseKura 8727 - [RequestContext@28392 category="AuditLogger" exception="" priority="INFO" thread="qtp1055114709-115"] {rest.method=POST, rest.path=token/jwt/v1/refresh, entrypoint=RestService, identity=container_kura_jwt_rest_client_017969ce, ip=172.17.0.2} Rest - Success - Rest request succeeded
```

From the container log (`docker logs kura-jwt-rest-client`), you should see the retrieved framework properties, like:
```
2026-10-07 14:14:03 Images:
{
  "images": [
    {
      "name": "kura-jwt-rest-client",
      "type": "CONTAINER_IMAGE",
      "version": "latest"
    },
    {
      "name": "nginx",
      "type": "CONTAINER_IMAGE",
      "version": "latest"
    }
  ]
}
```

And periodic token refreshes:

```
2026-10-07 13:33:57 Token pair refreshed
```

## Environment

From the `client.py` the following variables are set by Kura and are available inside of the container:

| Variable | Description |
|---|---|
| `KURA_IDENTITY_NAME` | Name of the temporary identity the tokens are issued for |
| `KURA_JWT_FILE` | Read-only file with the initial token pair, same JSON format as the refresh endpoint response |
| `KURA_REST_BASE_URL` | Base URL of the Kura REST APIs, e.g. `https://172.17.0.1:443/services` |
