---
services: key-vault
platforms: java
author: tiffanyachen
---

# Vnet/IP ACL sample for Azure Key Vault using the Azure Java SDK

This sample repo demonstrates how to create an Azure Key Vault with access limited to specific IP ranges and Azure Virtual Networks.

## Running the samples
1. If not installed, install [Java](https://www.java.com/en/download/help/download_options.xml).

2. Clone the repository.
```
git clone https://github.com/Azure-Samples/key-vault-java-authentication.git
```
3. Create an Azure service principal, using
[Azure CLI](http://azure.microsoft.com/documentation/articles/resource-group-authenticate-service-principal-cli/),
[PowerShell](http://azure.microsoft.com/documentation/articles/resource-group-authenticate-service-principal/)
or [Azure Portal](http://azure.microsoft.com/documentation/articles/resource-group-create-service-principal-portal/).
Note that if you wish to authenticate with the certificate authenticator the certificate should be saved locally.

4. Export these environment variables into your current shell or IDE.
```
    AZURE_TENANT_ID={your tenant id}
    AZURE_CLIENT_ID={your service principal AppID}
    AZURE_OBJECT_ID={your service principal Object ID}
    AZURE_CLIENT_SECRET={your application key}
```

AZURE_TENANT_ID, AZURE_CLIENT_ID, and AZURE_CLIENT_SECRET must be set for general Azure authentication.

5. Run main.java for a sample run through. This project uses maven so you can do so either through an IDE or on the command line.

## More information

* [What is Key Vault?](https://docs.microsoft.com/en-us/azure/key-vault/key-vault-whatis)
* [Get started with Azure Key Vault](https://docs.microsoft.com/en-us/azure/key-vault/key-vault-get-started)
* [Azure Key Vault General Documentation](https://docs.microsoft.com/en-us/azure/key-vault/)
* [Azure Key Vault REST API Reference](https://docs.microsoft.com/en-us/rest/api/keyvault/)
* [Azure SDK for Java Documentation](https://docs.microsoft.com/en-us/java/api/overview/azure/keyvault)
* [Azure Active Directory Documenation](https://docs.microsoft.com/en-us/azure/active-directory/)
