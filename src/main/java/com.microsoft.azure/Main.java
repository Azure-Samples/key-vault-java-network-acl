package com.microsoft.azure;

import com.microsoft.azure.NetworkAclSample;

public class Main {
    public static void main(String[] args) {
        NetworkAclSample samples = new NetworkAclSample();
        samples.createSampleVaultWithNetworkAcl();
    }
}
