# Docker Deployment for LLM Jakarta Projects

This folder contains Docker configuration files for all the LLM Jakarta projects. Each project has its own Docker Compose file named `docker-compose-step-XX.yml` that can be used to run that specific project. The main `docker-compose.yml` file in this folder can be used to run all projects together.

## Prerequisites

- Docker and Docker Compose installed on your machine
- OpenAI API key (set as an environment variable or in a `.env` file)

## Project Structure

- `docker-compose-step-00.yml`: Basic chatbot implementation (step-00-chatbot-first-step)
- `docker-compose-step-01.yml`: Prompt engineering examples (step-01-prompts)
- `docker-compose-step-02.yml`: Chat with memory capabilities (step-02-chat-memory)
- `docker-compose-step-03.yml`: Tool usage examples (step-03-tools)
- `docker-compose-step-04.yml`: In-memory RAG implementation (step-04-inmemory-rag)
- `docker-compose-step-05.yml`: Simple RAG implementation (step-05-easy-rag)
- `docker-compose-step-06.yml`: Advanced RAG implementation (step-06-advanced-rag)
- `docker-compose-step-07.yml`: Multi-model implementation (step-07-multi-model)
- `docker-compose-step-08.yml`: Advanced tools and RAG implementation (step-08-advanced-tools-and-rag)
- `docker-compose-step-09.yml`: Multi-component project with REST backend and server (step-09-mcp)

## Running Individual Projects

To run a specific project, use the corresponding Docker Compose file:

```bash
docker-compose -f docker-compose-step-XX.yml up -d
```

Each project will be available on a specific port:
- step-00: http://localhost:8800
- step-01: http://localhost:8801
- step-02: http://localhost:8802
- step-03: http://localhost:8803
- step-04: http://localhost:8804
- step-05: http://localhost:8805
- step-06: http://localhost:8806
- step-07: http://localhost:8807
- step-08: http://localhost:8808
- step-09-rest-backend: http://localhost:8809
- step-09-server: http://localhost:8810

## Running All Projects

To run all projects together, run the following command from the `deploy` folder:

```bash
docker-compose up -d
```

This will start all projects, as well as a PostgreSQL database and Adminer for database management.

## Database

The PostgreSQL database is available at:
- Host: localhost
- Port: 5435
- Username: llmjakarta
- Password: llmjakarta
- Database: llmjakarta

Adminer (database management tool) is available at http://localhost:9000

## Environment Variables

You can set the following environment variables:

```bash
export OPENAI_API_KEY=your-api-key
```

Or create a `.env` file in the `deploy` folder with the following content:

```
OPENAI_API_KEY=your-api-key
```

## Stopping Projects

To stop a specific project:

```bash
docker-compose -f docker-compose-step-XX.yml down
```

To stop all projects:

```bash
cd deploy
docker-compose down
```

To stop all projects and remove volumes:

```bash
cd deploy
docker-compose down -v
```
