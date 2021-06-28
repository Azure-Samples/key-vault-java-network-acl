package com.microsoft.azure;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.keyvault.models.IpRule;
import com.azure.resourcemanager.keyvault.models.NetworkRuleAction;
import com.azure.resourcemanager.keyvault.models.NetworkRuleBypassOptions;
import com.azure.resourcemanager.keyvault.models.NetworkRuleSet;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.keyvault.models.VirtualNetworkRule;
import com.microsoft.aad.adal4j.AuthenticationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NetworkAclSample {

    protected static AzureResourceManager azure;

    protected static final Region VAULT_REGION = Region.US_WEST;
    protected static final String AZURE_CLIENT_ID = System.getProperty("AZURE_CLIENT_ID");
    protected static final String AZURE_OBJECT_ID = System.getProperty("AZURE_OBJECT_ID");
    protected static final String AZURE_CLIENT_SECRET = System.getProperty("AZURE_CLIENT_SECRET");
    protected static final String AZURE_TENANT_ID = System.getProperty("AZURE_TENANT_ID");
    protected static final String AZURE_SUBSCRIPTION_ID = System.getProperty("AZURE_SUBSCRIPTION_ID");


    static {
        //Authenticates to Azure with Client_ID and CLIENT_SECRET
        authenticateToAzure();
    }

    public void createSampleVaultWithNetworkAcl() {
        String rgName = azure.resourceGroups().manager().internalContext().randomResourceName("rg-sample", 24);
        String vaultName = azure.resourceGroups().manager().internalContext().randomResourceName("vault-sample", 24);

        //Create sample resource group.
        azure.resourceGroups().define(rgName)
                .withRegion(VAULT_REGION)
                .create();
        System.out.println("Created a resource group with name " + rgName);

        //If you have a virtual network, set it here.
        String vnetId = "";
        List<VirtualNetworkRule> rules = new ArrayList<>();
//        rules.add(new VirtualNetworkRule().withId(vnetId));

        //Setting the network rules
        NetworkRuleSet networkRuleSet = new NetworkRuleSet()
                .withBypass(NetworkRuleBypassOptions.AZURE_SERVICES)// Allows bypass of network ACLs from Azure services. Valid: 'AzureServices' or 'None'
                .withDefaultAction(NetworkRuleAction.DENY) // Action to take if access attempt does not match any rule. 'Allow' or 'Deny'
                //IP rules
                .withIpRules(new ArrayList<IpRule>(Arrays.asList(new IpRule().withValue("0.0.0.0/0")))) // Allow access from all IPv4 addresses
                //Virtual network rules (Allows access to Azure Virtual Networks by their Azure Resource ID).
                .withVirtualNetworkRules(rules);

        //Create a new sample vault with
        Vault vault = azure.vaults().define(vaultName)
                .withRegion(Region.US_WEST)
                .withExistingResourceGroup(rgName)
                .defineAccessPolicy()
                .forObjectId(AZURE_OBJECT_ID)
                .allowSecretAllPermissions()
                .attach()
                .withDeploymentDisabled()
                .withBypass(NetworkRuleBypassOptions.AZURE_SERVICES)
                .withDefaultAction(NetworkRuleAction.DENY)
                .withAccessFromIpAddress("0.0.0.0/0")
                .create();

        Vault vault2 = azure.vaults()
                .define(azure.resourceGroups().manager().internalContext().randomResourceName("vault-sample", 20))
                .withRegion(Region.US_WEST)
                .withExistingResourceGroup(rgName)
                .defineAccessPolicy()
                .forObjectId(AZURE_OBJECT_ID)
                .allowSecretAllPermissions()
                .attach()
                .withDeploymentDisabled()
                .withAccessFromAzureServices() //essentially sets Bypass to AZURE_SERVICES
                .withAccessFromSelectedNetworks() //Sets default Action to Deny
                .withAccessFromIpAddress("0.0.0.0/0")
                .create();


        System.out.println("Created vault " + vault.vaultUri());
        System.out.println("Created vault " + vault2.vaultUri());


    }

    private static void authenticateToAzure() {
        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        // Authentication for general Azure service
        ClientSecretCredential credentials = new ClientSecretCredentialBuilder()
                .clientId(AZURE_CLIENT_ID).tenantId(AZURE_TENANT_ID)
                .clientSecret(AZURE_CLIENT_SECRET)
                .build();
        try {
            azure = AzureResourceManager.configure()
                    .withLogLevel(HttpLogDetailLevel.BASIC)
                    .authenticate(credentials, profile)
                    .withSubscription(AZURE_SUBSCRIPTION_ID);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AuthenticationException(
                    "Error authenticating to Azure - check your credentials in your environment.");
        }
    }
}