package com.ivotasevski.keycloakspi;

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

    private static final Logger log = Logger.getLogger(ExternalRolesProtocolMapper.class.getName());

    public static final String PROVIDER_ID = "external-roles-protocol-mapper";

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        configProperties.add(new ProviderConfigProperty(
                "ExternalRolesAPIUrl",
                "External Role API URL",
                "The URL of the API that provides the external roles",
                ProviderConfigProperty.STRING_TYPE,
                null));
        OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, ExternalRolesProtocolMapper.class);

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

        log.info("Setting the claim as multivalued.");
        mappingModel.getConfig().put(ProtocolMapperUtils.MULTIVALUED, Boolean.TRUE.toString());

        // TODO: Retrieve these from external source
        List<String> externalRoles = List.of("Role1", "Role2", "Role3");
        log.info("The following roles have been added to token: " + externalRoles);
        OIDCAttributeMapperHelper.mapClaim(token, mappingModel, externalRoles);
    }
}