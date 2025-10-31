# Zentao (禅道) Integration Guide

## Overview

This guide explains how to integrate the DevFlow application with Zentao (禅道) project management system.

## Features

- **Automatic Story Creation**: When a user story is created in DevFlow, it is automatically created in Zentao
- **Story ID Synchronization**: The Zentao story ID is stored back in the DevFlow user story
- **Token Management**: Automatic token management with caching to minimize API calls

## Configuration

### 1. Configure Project with Zentao Information

When creating or editing a project, configure the Project Management System section:

```json
{
  "name": "My Project",
  "description": "Project description",
  "projectManagementSystem": {
    "type": "Zentao",
    "systemId": "1",
    "baseUrl": "http://your-zentao-server.com",
    "accessToken": "username:password"
  }
}
```

### Configuration Parameters

- **type**: Must be set to `"Zentao"`
- **systemId**: The product ID in Zentao where stories will be created
- **baseUrl**: The base URL of your Zentao installation (without trailing slash)
- **accessToken**: Authentication credentials in the format `username:password`

### 2. Access Token Format

The access token should be in the format: `account:password`

Example: `admin:mypassword123`

This will be used to obtain an authentication token from Zentao API.

## How It Works

### Story Creation Flow

1. User creates a user story in DevFlow
2. DevFlow checks if the associated project has a Zentao configuration
3. If configured, DevFlow:
   - Obtains an authentication token from Zentao (cached for 2 hours)
   - Creates a story in Zentao with the user story information
   - Stores the Zentao story ID back in the DevFlow user story

### Story Mapping

DevFlow user story fields are mapped to Zentao story fields as follows:

| DevFlow Field | Zentao Field | Description |
|---------------|--------------|-------------|
| title | title | Story title |
| userStory + optimizedRequirement + technicalNotes | spec | Combined story description |
| acceptanceCriteria | verify | Acceptance criteria |
| priority (HIGH/MEDIUM/LOW) | pri (1/2/3) | Priority mapping |
| tags | keywords | Comma-separated keywords |

### Default Values

When creating stories in Zentao, the following defaults are used:

- **type**: `"story"` (story type)
- **status**: `"draft"` (draft status)
- **stage**: `"wait"` (waiting stage)
- **source**: `"DevFlow"` (source system)
- **sourceNote**: `"Created from DevFlow system"`

## API Reference

### Zentao API Endpoints Used

1. **Token Generation**
   - Endpoint: `POST /api.php/v1/tokens`
   - Documentation: https://www.zentao.net/book/api/1397.html

2. **Story Creation**
   - Endpoint: `POST /api.php/v1/products/{productID}/stories`
   - Documentation: https://www.zentao.net/book/api/694.html

## Error Handling

- If Zentao integration fails, the user story is still saved in DevFlow
- Errors are logged but do not prevent the user story creation
- Token errors will trigger a new token request on the next operation

## Troubleshooting

### Common Issues

1. **Token Authentication Fails**
   - Verify the access token format is correct: `username:password`
   - Check that the username and password are correct in Zentao
   - Ensure the user has permission to create stories in the specified product

2. **Story Creation Fails**
   - Verify the `systemId` (product ID) exists in Zentao
   - Check that the Zentao base URL is correct and accessible
   - Ensure the Zentao API is enabled

3. **Connection Issues**
   - Verify network connectivity to the Zentao server
   - Check if the Zentao server URL is accessible from the DevFlow server
   - Review firewall settings

## Example

### Creating a Project with Zentao Integration

```bash
POST /api/projects
Content-Type: application/json

{
  "name": "E-Commerce Platform",
  "description": "Online shopping platform",
  "projectManagementSystem": {
    "type": "Zentao",
    "systemId": "5",
    "baseUrl": "http://zentao.example.com",
    "accessToken": "john.doe:MySecurePassword123"
  }
}
```

### Creating a User Story

Once the project is configured, creating a user story will automatically create it in Zentao:

```bash
POST /api/user-stories
Content-Type: application/json

{
  "projectId": "project-123",
  "title": "User Login Feature",
  "userStory": "As a user, I want to login to the system...",
  "acceptanceCriteria": "User can login with email and password",
  "priority": "HIGH",
  "tags": ["authentication", "security"]
}
```

The response will include the `storyId` from Zentao:

```json
{
  "id": "story-456",
  "projectId": "project-123",
  "title": "User Login Feature",
  "storyId": "789",
  ...
}
```

## Security Considerations

1. **Credential Storage**: Access tokens (username:password) are stored in the database. Consider encrypting these values in a production environment.

2. **Token Caching**: Tokens are cached in memory for 2 hours. This cache is cleared when the application restarts.

3. **HTTPS**: Always use HTTPS for Zentao connections in production environments.

## Future Enhancements

Potential improvements for the Zentao integration:

- Support for updating existing stories
- Two-way synchronization (updates from Zentao to DevFlow)
- Support for story assignments and status updates
- Support for attachments and comments
- Webhook integration for real-time updates
