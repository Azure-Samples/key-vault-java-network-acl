package com.microsoft.azure;

import com.microsoft.aad.adal4j.AuthenticationException;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.keyvault.*;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.credentials.UserTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.rest.LogLevel;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.authentication.KeyVaultCredentials;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NetworkAclSample {

    protected static Azure azure;

    protected static final Region VAULT_REGION = Region.US_WEST;
    protected static final String AZURE_CLIENT_ID = System.getProperty("AZURE_CLIENT_ID");
    protected static final String AZURE_OBJECT_ID = System.getProperty("AZURE_OBJECT_ID");
    protected static final String AZURE_CLIENT_SECRET = System.getProperty("AZURE_CLIENT_SECRET");
    protected static final String AZURE_TENANT_ID = System.getProperty("AZURE_TENANT_ID");


    static {
        //Authenticates to Azure with Client_ID and CLIENT_SECRET
        authenticateToAzure();
    }

    public void createSampleVaultWithNetworkAcl() {
        String rgName = SdkContext.randomResourceName("rg-sample", 24);
        String vaultName = SdkContext.randomResourceName("vault-sample", 24);

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
                .withIpRules(new ArrayList<>(Arrays.asList(new IPRule().withValue("0.0.0.0/0")))) // Allow access from all IPv4 addresses
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

        Vault vault2 = azure.vaults().define(SdkContext.randomResourceName("vault-sample", 20))
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
        // Authentication for general Azure service
        ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(AZURE_CLIENT_ID,
                AZURE_TENANT_ID, AZURE_CLIENT_SECRET, AzureEnvironment.AZURE);

        try {
            azure = Azure.configure().withLogLevel(LogLevel.BASIC).authenticate(credentials).withDefaultSubscription();
        } catch (Exception e) {
            e.printStackTrace();
            throw new AuthenticationException(
                    "Error authenticating to Azure - check your credentials in your environment.");
        }
    }

}
