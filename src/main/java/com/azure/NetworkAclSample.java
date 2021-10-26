package com.azure;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.CredentialUnavailableException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.keyvault.models.IpRule;
import com.azure.resourcemanager.keyvault.models.NetworkRuleAction;
import com.azure.resourcemanager.keyvault.models.NetworkRuleBypassOptions;
import com.azure.resourcemanager.keyvault.models.NetworkRuleSet;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.keyvault.models.VirtualNetworkRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NetworkAclSample {

    protected static AzureResourceManager azure;

    protected static final Region VAULT_REGION = Region.US_WEST;
    protected static final Integer RESOURCE_NAME_MAX_LENGTH = 24;
    protected static final String AZURE_CLIENT_ID = System.getProperty("AZURE_CLIENT_ID");
    protected static final String AZURE_OBJECT_ID = System.getProperty("AZURE_OBJECT_ID");
    protected static final String AZURE_CLIENT_SECRET = System.getProperty("AZURE_CLIENT_SECRET");
    protected static final String AZURE_TENANT_ID = System.getProperty("AZURE_TENANT_ID");
    protected static final String AZURE_SUBSCRIPTION_ID = System.getProperty("AZURE_SUBSCRIPTION_ID");
    protected static final String DEFAULT_IP_ADDRESS = "0.0.0.0/0";
    protected static final String DEFAULT_VAULT_NAME_PREFIX = "vault-sample";
    protected static final String RESOURCE_GROUP_NAME = azure.resourceGroups()
            .manager()
            .internalContext()
            .randomResourceName("rg-sample", RESOURCE_NAME_MAX_LENGTH);
    protected static final String FIRST_VAULT_NAME = azure.resourceGroups()
            .manager()
            .internalContext()
            .randomResourceName(DEFAULT_VAULT_NAME_PREFIX, RESOURCE_NAME_MAX_LENGTH);
    protected static final String SECOND_VAULT_NAME = azure.resourceGroups()
            .manager()
            .internalContext()
            .randomResourceName(DEFAULT_VAULT_NAME_PREFIX, RESOURCE_NAME_MAX_LENGTH);

    static {
        // Authenticates to Azure with Client_ID and CLIENT_SECRET
        authenticateToAzure();
    }

    public void createSampleVaultWithNetworkAcl() {

        // Create sample resource group.
        azure.resourceGroups()
                .define(RESOURCE_GROUP_NAME)
                .withRegion(VAULT_REGION)
                .create();
        System.out.println("Created a resource group with name: " + RESOURCE_GROUP_NAME);

        // If you have a virtual network, set it here.
        List<VirtualNetworkRule> rules = new ArrayList<>();

        // Setting the network rules
        NetworkRuleSet networkRuleSet = new NetworkRuleSet()
                .withBypass(NetworkRuleBypassOptions.AZURE_SERVICES)// Allows bypass of network ACLs from Azure services. Accepted values: 'AzureServices' or 'None'.
                .withDefaultAction(NetworkRuleAction.DENY) // Action to take if access attempt does not match any rule. Accepted values: 'Allow' or 'Deny'.
                // IP rules
                .withIpRules(new ArrayList<IpRule>(
                        Arrays.asList(new IpRule().withValue(DEFAULT_IP_ADDRESS))
                )) // Allow access from all IPv4 addresses.
                // Virtual network rules (Allows access to Azure Virtual Networks by their Azure Resource ID).
                .withVirtualNetworkRules(rules);

        // Create a new sample vault with
        Vault firstVault = azure.vaults()
                .define(FIRST_VAULT_NAME)
                .withRegion(Region.US_WEST)
                .withExistingResourceGroup(RESOURCE_GROUP_NAME)
                .defineAccessPolicy()
                .forObjectId(AZURE_OBJECT_ID)
                .allowSecretAllPermissions()
                .attach()
                .withDeploymentDisabled()
                .withBypass(NetworkRuleBypassOptions.AZURE_SERVICES)
                .withDefaultAction(NetworkRuleAction.DENY)
                .withAccessFromIpAddress(DEFAULT_IP_ADDRESS)
                .create();

        Vault secondVault = azure.vaults()
                .define(SECOND_VAULT_NAME)
                .withRegion(Region.US_WEST)
                .withExistingResourceGroup(RESOURCE_GROUP_NAME)
                .defineAccessPolicy()
                .forObjectId(AZURE_OBJECT_ID)
                .allowSecretAllPermissions()
                .attach()
                .withDeploymentDisabled()
                .withAccessFromAzureServices() //Essentially sets Bypass to AZURE_SERVICES
                .withAccessFromSelectedNetworks() //Sets default Action to Deny
                .withAccessFromIpAddress(DEFAULT_IP_ADDRESS)
                .create();


        System.out.println("Created vault: " + firstVault.vaultUri());
        System.out.println("Created vault: " + secondVault.vaultUri());


    }

    private static void authenticateToAzure() {
        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        // Authentication for general Azure service.
        ClientSecretCredential credentials = new ClientSecretCredentialBuilder()
                .clientId(AZURE_CLIENT_ID)
                .tenantId(AZURE_TENANT_ID)
                .clientSecret(AZURE_CLIENT_SECRET)
                .build();
        try {
            azure = AzureResourceManager.configure()
                    .withLogLevel(HttpLogDetailLevel.BASIC)
                    .authenticate(credentials, profile)
                    .withSubscription(AZURE_SUBSCRIPTION_ID);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CredentialUnavailableException(
                    "Error authenticating to Azure - check your credentials in your environment.");
        }
    }
}