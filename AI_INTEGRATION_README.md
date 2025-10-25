# AI-Powered User Story Optimization

This module provides AI-powered user story optimization and test case generation using Dify AI Agent API.

## Features

- **User Story Optimization**: Automatically optimize user story descriptions using AI
- **Test Case Generation**: Generate comprehensive test cases based on user stories
- **Dify Integration**: Seamless integration with Dify AI Agent API

## Setup Instructions

### 1. Backend Configuration

Add the following configuration to your `application.properties` file:

```properties
# Dify AI API Configuration
dify.api.base-url=${DIFY_API_BASE_URL:https://api.dify.ai/v1}
dify.api.key=${DIFY_API_KEY:}
dify.api.user-story-optimization.workflow-id=${DIFY_USER_STORY_OPTIMIZATION_WORKFLOW_ID:}
dify.api.test-case-generation.workflow-id=${DIFY_TEST_CASE_GENERATION_WORKFLOW_ID:}
```

### 2. Environment Variables

Set the following environment variables or update your application properties:

- `DIFI_API_KEY`: Your Dify API key
- `DIFY_API_BASE_URL`: Dify API base URL (default: https://api.dify.ai/v1)
- `DIFY_USER_STORY_OPTIMIZATION_WORKFLOW_ID`: Workflow ID for user story optimization (optional)
- `DIFY_TEST_CASE_GENERATION_WORKFLOW_ID`: Workflow ID for test case generation (optional)

### 3. Dify Workflow Setup

1. Log in to your Dify dashboard
2. Create workflows for:
   - User Story Optimization
   - Test Case Generation
3. Configure the workflows with appropriate prompts and models
4. Get the workflow IDs and set them in your configuration

### 4. API Endpoints

The following REST endpoints are available:

#### Optimize User Story
```http
POST /api/ai/optimize-user-story
Content-Type: application/json

{
  "description": "User story description to optimize",
  "projectContext": "Optional project context",
  "additionalRequirements": "Optional additional requirements"
}
```

#### Generate Test Cases
```http
POST /api/ai/generate-test-cases
Content-Type: application/json

{
  "description": "Original user story description",
  "optimizedDescription": "Optional optimized description",
  "projectContext": "Optional project context"
}
```

#### Health Check
```http
GET /api/ai/health
```

## Frontend Integration

The frontend automatically calls these APIs when users click:
- "Optimize By AI" button in the User Story Creation page
- "Generate by AI" button in the Test Cases tab

## Error Handling

The system includes comprehensive error handling:
- API validation errors
- Dify service errors
- Network connectivity issues
- Invalid responses

## Security

- All endpoints require authentication
- Role-based access control (USER, ADMIN, OPERATOR roles)
- API key security for Dify integration

## Sample Dify Workflow Prompts

### User Story Optimization Prompt
```
You are an expert Agile coach and product manager. Optimize the following user story:

Description: {{user_story_description}}
Project Context: {{project_context}}
Additional Requirements: {{additional_requirements}}

Please provide:
1. Optimized User Story in proper format (As a... I want... So that...)
2. Clear Acceptance Criteria
3. Definition of Done

Format your response with clear sections using markdown headers.
```

### Test Case Generation Prompt
```
You are a QA engineer and testing expert. Generate comprehensive test cases for the following user story:

User Story: {{user_story_description}}
Project Context: {{project_context}}

Please generate test cases covering:
1. Happy path scenarios
2. Edge cases
3. Error handling
4. Performance considerations
5. Security aspects

Format each test case with Given-When-Then structure.
```

## Troubleshooting

### Common Issues

1. **API Key Issues**
   - Verify your Dify API key is correct
   - Check if the API key has proper permissions

2. **Workflow Configuration**
   - Ensure workflow IDs are correct
   - Verify workflows are published and active

3. **Network Issues**
   - Check firewall settings
   - Verify internet connectivity to Dify servers

4. **Response Parsing**
   - Check Dify workflow output format
   - Ensure responses contain expected structure

### Logs

Check application logs for detailed error information:
```bash
tail -f logs/application.log | grep -i "dify\|ai"
```

## Development

### Testing

Run tests with:
```bash
mvn test
```

### Local Development

For local development, you can use mock responses by setting:
```properties
dify.api.base-url=http://localhost:8080/mock
```

## Contributing

1. Follow the existing code structure
2. Add comprehensive error handling
3. Include unit tests for new features
4. Update documentation for API changes