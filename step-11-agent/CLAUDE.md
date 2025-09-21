# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is **step-11-agent** of the Jakarta EE AI Chatbot project, focused on AI agents and book creation functionality. This module is designed to create illustrated children's books using LangChain4j agents and OpenAI integration.

## Key Dependencies

- **LangChain4j 1.4.0**: Core AI framework with OpenAI integration
- **PicoCLI 4.7.5**: Command-line interface framework
- **iText7 8.0.2**: PDF generation for book creation
- **Jackson**: JSON processing for data models
- **OkHttp**: HTTP client for downloading images
- **Lombok**: Code generation for data models

## Build Commands

```bash
# Clean and compile
mvn clean compile

# Build JAR with dependencies
mvn clean package

# Run the application (main class: ca.bazlur.cli.BookCreationCLI)
java -jar target/step-11-agent-*-jar-with-dependencies.jar

# Run from Maven directly
mvn exec:java -Dexec.mainClass="ca.bazlur.cli.BookCreationCLI"
```

## Usage Examples

```bash
# Basic usage with dry run (no API calls for images)
java -jar target/step-11-agent-*-jar-with-dependencies.jar "A brave little mouse" --dry-run

# Full book creation with custom settings
java -jar target/step-11-agent-*-jar-with-dependencies.jar \
  "Learning to Share" \
  --age "4-5" \
  --pages 8 \
  --educational "sharing,kindness" \
  --style "cartoon" \
  --model "gpt-4o-mini" \
  --temperature 0.7 \
  --output "sharing-book.pdf" \
  --verbose

# Using different models
java -jar target/step-11-agent-*-jar-with-dependencies.jar \
  "Space Adventure" \
  --model "gpt-4o" \
  --temperature 0.9 \
  --dry-run
```

## Architecture

### Core Models
- **BookRequest** (`ca.bazlur.model.BookRequest`): Input parameters for book creation including topic, target age, page count, educational goals, and illustration style
- **BookOutline** (`ca.bazlur.model.BookOutline`): High-level book structure with title, main character, setting, and page outlines
- **PageOutline** (`ca.bazlur.model.PageOutline`): Individual page details including text, illustration description, educational and interactive elements

### Main Entry Point
- **Main Class**: `ca.bazlur.cli.BookCreationCLI` (as defined in maven-assembly-plugin configuration)
- **Current Main**: `learning.jakarta.ai.Main` (placeholder/template)

### Agent Architecture
- **BookCreationOrchestrator**: Main AI agent for story creation and plot development
- **IllustrationAgent**: DALL-E 3 integration for image generation and management
- **ContentRefinementAgent**: Age-appropriate content adaptation and text refinement
- **PDFGenerationAgent**: Professional PDF creation with iText7
- **BookCreationWorkflow**: Orchestrates the entire pipeline with progress tracking

### Configuration System
- **BookCreationConfig**: Centralized configuration using Java records
- Supports model selection (gpt-4o-mini, gpt-4o, etc.)
- Configurable temperature and token limits
- CLI options and system properties support

### Design Patterns
- **Java Records** for immutable configuration and data models
- **Builder pattern** for constructing complex requests and responses
- **Agent pattern** for modular AI services
- **Pipeline pattern** with async processing using virtual threads
- **CLI framework** (PicoCLI) for comprehensive command-line interface

## Environment Setup

Ensure you have the **OPENAI_API_KEY** environment variable set:
```bash
export OPENAI_API_KEY=your-api-key-here
```

## Development Notes

- Java 21 target (as specified in pom.xml)
- Uses Maven wrapper (../mvnw) available in parent directory
- Part of larger llm-jakarta project structure
- Integration with PDF generation for book output
- HTTP client setup for downloading generated illustrations