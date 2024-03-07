package com.ivotasevski.keycloakspi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.protocol.oidc.mappers.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ExternalRolesProtocolMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper,
        OIDCIDTokenMapper, UserInfoTokenMapper {

    private static String URL_CONFIG_PROP = "external.roles.api.url";

    private static final Logger log = Logger.getLogger(ExternalRolesProtocolMapper.class.getName());

    public static final String PROVIDER_ID = "external-roles-protocol-mapper";

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        configProperties.add(new ProviderConfigProperty(
                URL_CONFIG_PROP,
                "External Role API URL",
                "The URL of the API that provides the external roles",
                ProviderConfigProperty.STRING_TYPE,
                null));
        OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, ExternalRolesProtocolMapper.class);

    }

    private ObjectMapper objectMapper;

    public ExternalRolesProtocolMapper() {
        super();
        objectMapper = new ObjectMapper();
    }

    @Override
    public String getDisplayCategory() {
        return "Token Mapper";
    }

    @Override
    public String getDisplayType() {
        return "External Roles Token Mapper";
    }

    @Override
    public String getHelpText() {
        return "Adds custom claim with external roles in the token";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel,
                            UserSessionModel userSession, KeycloakSession keycloakSession,
                            ClientSessionContext clientSessionCtx) {


        // Setting the claim as multivalued, so it can accept a list of roles
        mappingModel.getConfig().put(ProtocolMapperUtils.MULTIVALUED, Boolean.TRUE.toString());

        // In case you need to identify the user by another property, retrieve it from the token.
        String sub = token.getSubject();

        // If the url contains a placeholder part {USER_ID} replace it. For example: /api/users/{USER_ID}/roles
        String finalUrl = mappingModel.getConfig().get(URL_CONFIG_PROP).replaceAll("\\{USER_ID\\}", sub);

        List<String> externalRoles = retrieveRolesFromExternalApi(finalUrl);
        log.finest(String.format("Token for subject '%s' has been extended with the following external roles: %s", sub, externalRoles.toString()));
        OIDCAttributeMapperHelper.mapClaim(token, mappingModel, externalRoles);
    }

    private List<String> retrieveRolesFromExternalApi(String url) {

        log.finest(String.format("Invoking '%s' to retrieve external roles.", url));

        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet request = new HttpGet(url);
            HttpResponse response = httpClient.execute(request);

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RuntimeException(
                        String.format("Not able to retrieve external roles from external API. URL: '%s', StatusCode: '%s'",
                                url,
                                response.getStatusLine().getStatusCode()));
            }

            String responseBody = EntityUtils.toString(response.getEntity());
            log.finest(String.format("Url: '%s', Response: %s", url, responseBody));
            return objectMapper.readValue(responseBody, List.class);

        } catch (Exception e) {
            throw new RuntimeException(String.format("Unable to retrieve roles from external API. URL: %s", url), e);
        }
    }
}