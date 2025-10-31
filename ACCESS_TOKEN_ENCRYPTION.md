# Access Token Encryption Guide

## Overview

This project implements automatic encryption for sensitive access tokens stored in MongoDB. Both `GitRepository` and `ProjectManagementSystem` access tokens are encrypted when stored and automatically decrypted when retrieved.

## Implementation Details

### 1. Encryption Utility

The `CryptoUtil` class (`com.lz.devflow.util.CryptoUtil`) provides AES encryption functionality:

- **Algorithm**: AES-256 (ECB mode with PKCS5 padding)
- **Encoding**: Base64 for encrypted strings
- **Key Length**: 32 bytes (256 bits)

### 2. Automatic Encryption/Decryption

The `Project` entity class automatically handles encryption:

```java
// In GitRepository inner class
public void setAccessToken(String accessToken) {
    // Automatically encrypts when setting
    this.accessToken = CryptoUtil.encrypt(accessToken);
}

public String getAccessToken() {
    // Automatically decrypts when getting
    return CryptoUtil.decrypt(accessToken);
}

// Same implementation in ProjectManagementSystem inner class
```

### 3. Configuration

Add the encryption key to your `application.properties`:

```properties
# Crypto Configuration for AccessToken Encryption
# IMPORTANT: Change this key in production! Must be 32 characters for AES-256
crypto.secret.key=${CRYPTO_SECRET_KEY:DevFlowSecretKey1234567890123456}
```

### 4. Environment Variable Setup

For production, set the encryption key via environment variable:

```bash
# Linux/Mac
export CRYPTO_SECRET_KEY="YourSecureRandomKey1234567890123"

# Windows
set CRYPTO_SECRET_KEY=YourSecureRandomKey1234567890123
```

## Usage

### Storing Access Token

When creating or updating a project, simply set the access token as plain text:

```java
Project.GitRepository gitRepo = new Project.GitRepository();
gitRepo.setAccessToken("ghp_your_github_token_here");
// Token is automatically encrypted before saving to MongoDB

Project.ProjectManagementSystem pms = new Project.ProjectManagementSystem();
pms.setAccessToken("username:password");
// Token is automatically encrypted before saving to MongoDB
```

### Retrieving Access Token

When reading the access token, it's automatically decrypted:

```java
Project project = projectRepository.findById(projectId).orElseThrow();
String token = project.getGitRepository().getAccessToken();
// Token is automatically decrypted - you get the plain text

String pmsToken = project.getProjectManagementSystem().getAccessToken();
// Token is automatically decrypted
```

## Storage Format

In MongoDB, the access tokens are stored in encrypted Base64 format:

```json
{
  "_id": "...",
  "name": "My Project",
  "gitRepository": {
    "type": "GITHUB",
    "baseUrl": "https://api.github.com",
    "accessToken": "dGhpc0lzRW5jcnlwdGVkVG9rZW4="  // Encrypted Base64
  },
  "projectManagementSystem": {
    "type": "Zentao",
    "baseUrl": "https://zentao.example.com",
    "accessToken": "YW5vdGhlckVuY3J5cHRlZFRva2Vu"  // Encrypted Base64
  }
}
```

## Security Best Practices

1. **Change the Default Key**: The default encryption key is for development only. Always use a strong, random key in production.

2. **Key Management**: 
   - Store the encryption key securely (e.g., AWS Secrets Manager, Azure Key Vault)
   - Never commit the production key to version control
   - Rotate keys periodically

3. **Key Requirements**:
   - Must be at least 16 characters (32 recommended for AES-256)
   - Use random, unpredictable characters
   - Different keys for different environments

4. **Generate Strong Keys**:
   ```bash
   # Using OpenSSL (32 characters)
   openssl rand -base64 32 | cut -c1-32
   
   # Using Python
   python -c "import secrets; print(secrets.token_urlsafe(32)[:32])"
   ```

## Data Migration

If you have existing projects with unencrypted tokens in the database:

1. The first time you retrieve and save an existing project, tokens will be encrypted
2. Or, you can create a migration script to encrypt all existing tokens:

```java
// Migration example
List<Project> projects = projectRepository.findAll();
for (Project project : projects) {
    if (project.getGitRepository() != null) {
        // Re-saving will trigger encryption
        String token = project.getGitRepository().getAccessToken();
        project.getGitRepository().setAccessToken(token);
    }
    if (project.getProjectManagementSystem() != null) {
        String token = project.getProjectManagementSystem().getAccessToken();
        project.getProjectManagementSystem().setAccessToken(token);
    }
    projectRepository.save(project);
}
```

## Troubleshooting

### Error: "Error decrypting data"

- Check if the `CRYPTO_SECRET_KEY` matches the key used to encrypt the data
- Verify the encrypted data is valid Base64
- Ensure the key length is correct (32 characters for AES-256)

### Null or Empty Tokens

- The encryption/decryption methods handle `null` and empty strings gracefully
- They return the input unchanged if null or empty

## Impact on Existing Code

No changes are required in service layers or controllers:

- `ProjectServiceImpl`: Works transparently with encrypted tokens
- `UserStoryServiceImpl`: Receives decrypted tokens automatically
- `ZentaoService`: Receives decrypted tokens for API calls
- All other services continue to work without modification

The encryption is completely transparent to the application logic.
