# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a sandbox repository for experimenting with Claude Code.

## Project Structure

- `backend/` - Spring Boot API application (Java 21, Maven)

## Backend API

- **Framework**: Spring Boot 3.4.1
- **Java Version**: 21
- **Package**: `com.sandbox.api`
- **Port**: 8080

### Build Commands

```bash
# Build
cd backend && ./mvnw clean package

# Run
cd backend && ./mvnw spring-boot:run

# Test
cd backend && ./mvnw test
```

### API Endpoints

- `GET /api/hello` - Hello World endpoint

## Development Environment

- **IDE**: IntelliJ IDEA
- **Java**: JDK 21

## Repository

- **Remote**: https://github.com/nagashima-toru/sandbox-claude-code.git
- **Main branch**: master