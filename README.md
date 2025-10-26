# 🌐 HTTP Request DSL – Type-safe Kotlin DSL for HTTP Requests

[![version](https://img.shields.io/badge/version-1.0.15-yellow.svg)](https://semver.org)
[![Awesome Kotlin Badge](https://kotlin.link/awesome-kotlin.svg)](https://github.com/KotlinBy/awesome-kotlin)
[![Build](https://github.com/rkociniewski/http-dsl/actions/workflows/main.yml/badge.svg)](https://github.com/rkociniewski/rosario/actions/workflows/main.yml)
[![CodeQL](https://github.com/rkociniewski/http-dsl/actions/workflows/codeql.yml/badge.svg)](https://github.com/rkociniewski/rosario/actions/workflows/codeql.yml)
[![Dependabot Status](https://img.shields.io/badge/Dependabot-enabled-success?logo=dependabot)](https://github.com/rkociniewski/http-dsl/network/updates)
[![codecov](https://codecov.io/gh/rkociniewski/http-dsl/branch/main/graph/badge.svg)](https://codecov.io/gh/rkociniewski/rosario)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-blueviolet?logo=kotlin)](https://kotlinlang.org/)
[![Gradle](https://img.shields.io/badge/Gradle-9.1.0-blue?logo=gradle)](https://gradle.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-greem.svg)](https://opensource.org/licenses/MIT)

**HTTP Request DSL** is a minimalist, type-safe Kotlin DSL library for building HTTP requests. It provides a clean and concise syntax for defining HTTP requests with full compile-time validation.

## ✨ Features

* 🔧 **Type-safe DSL** for building HTTP requests
* 🎯 Support for all major HTTP methods (GET, POST, PUT, DELETE, PATCH)
* 📦 Multiple request body types: JSON, Text, Raw (bytes)
* ⏱️ Timeout configuration (connect, read, write)
* 🔐 Authentication templates with Bearer tokens
* 🔄 Interceptor system for middleware
* ✅ Request validation pipelines
* 🏗️ URL builder with segments and query parameters
* 🔀 Batch request builder for multiple requests
* 🛡️ Comprehensive validation and error handling

## 📦 Installation

### Requirements

* Kotlin 2.2.21 or later
* Java 21 or later
* Gradle 9.1.0 or later

### Gradle Setup

```kotlin
dependencies {
    implementation("rk.powermilk:http-request-dsl:1.0.13")
}
```

## 🚀 Quick Start

### Basic GET Request

```kotlin
val request = httpRequest {
    url("https://api.example.com/users")
    method(HttpMethod.GET)
}
```

### POST with JSON Body

```kotlin
val request = httpRequest {
    url("https://api.example.com/users")
    method(HttpMethod.POST)

    headers {
        "Content-Type" to "application/json"
        "Authorization" to "Bearer token123"
    }

    body {
        json {
            "name" to "John"
            "age" to 30
            "emails" to listOf("john@example.com")
        }
    }

    timeout {
        connect = 5000
        read = 10000
    }
}
```

### Nested JSON Structures

```kotlin
val request = httpRequest {
    url("https://api.example.com/users")
    method(HttpMethod.POST)

    body {
        json {
            "name" to "John"
            nested("address") {
                "street" to "Main St"
                "city" to "New York"
                "zip" to "10001"
            }
            "emails" to listOf("john@example.com")
        }
    }
}
```

## 🔧 Advanced Features

### Authenticated Requests

```kotlin
val request = RequestTemplates.authenticated(
    baseUrl = "https://api.example.com",
    token = "secret-token"
) {
    path("/users/123")
    method(HttpMethod.GET)

    headers {
        "X-Custom-Header" to "value"
    }
}
```

### URL Builder

```kotlin
val request = httpRequestWithUrl(
    baseUrl = "https://api.example.com",
    urlBlock = {
        segment("users")
        segment("123")
        query("include", "posts")
        query("format", "json")
    }
) {
    method(HttpMethod.GET)
}
// Result: https://api.example.com/users/123?include=posts&format=json
```

### Request Interceptors

```kotlin
val pipeline = RequestPipeline(
    listOf(
        RetryInterceptor(maxRetries = 3),
        LoggingInterceptor()
    )
)

val originalRequest = httpRequest {
    url("https://api.example.com/users")
    method(HttpMethod.GET)
}

val processedRequest = pipeline.execute(originalRequest)
```

### Request Validation

```kotlin
val request = httpRequest {
    url("https://api.example.com/users")
    method(HttpMethod.POST)
    body {
        json { "name" to "John" }
    }
}

val result = request.validate()
if (result.isSuccess) {
    // Request is valid
} else {
    // Handle validation error
    println(result.exceptionOrNull()?.message)
}
```

### Request Modification

```kotlin
val original = httpRequest {
    url("https://api.example.com/users")
    method(HttpMethod.GET)
}

val modified = original.modify {
    method(HttpMethod.POST)
    headers {
        "Authorization" to "Bearer token"
    }
    body {
        json { "name" to "Jane" }
    }
}
```

### Batch Requests

```kotlin
val requests = batchRequests {
    request {
        url("https://api.example.com/users/1")
        method(HttpMethod.GET)
    }

    request {
        url("https://api.example.com/users/2")
        method(HttpMethod.GET)
    }

    request {
        url("https://api.example.com/posts")
        method(HttpMethod.POST)
        body {
            json { "title" to "New Post" }
        }
    }
}
```

### Conditional Building

```kotlin
val includeAuth = true
val hasBody = false

val request = httpRequest {
    url("https://api.example.com/users")

    conditionalHeaders(includeAuth) {
        "Authorization" to "Bearer token"
    }

    conditionalBody(hasBody) {
        json { "data" to "value" }
    }
}
```

### Default Configuration

```kotlin
val config = RequestConfig(
    defaultTimeout = 10000,
    defaultHeaders = mapOf(
        "User-Agent" to "MyApp/1.0",
        "Accept" to "application/json"
    )
)

val builder = ConfigurableRequestBuilder(config)
val request = builder.build {
    url("https://api.example.com/users")
    method(HttpMethod.GET)
}
```

## 🗂 Project Structure

```
📦rk.powermilk.request
 ┣ 📁constant      # Error message constants
 ┣ 📁dsl           # DSL builders and annotations
 ┣ 📁enums         # HttpMethod enum
 ┣ 📁model         # Data models (HttpRequest, RequestBody, etc.)
 ┗ 📜Main.kt       # Example usage
```

## 📚 Request Body Types

### JSON Body

```kotlin
body {
    json {
        "key" to "value"
        "number" to 42
        "list" to listOf(1, 2, 3)
        "nullable" to null
    }
}
```

### Text Body

```kotlin
body {
    text("Plain text content")
}
```

### Raw Body (bytes)

```kotlin
body {
    raw("binary data".toByteArray())
}
```

## ⏱️ Timeouts

```kotlin
timeout {
    connect = 5000  // ms
    read = 10000    // ms
    write = 7000    // ms
}
```

## ✅ Validation

The library provides comprehensive validation:

* URL must be non-empty and non-blank
* Headers cannot have empty keys or values
* JSON body cannot have empty keys
* Timeouts must be positive
* Body can only be set once
* POST/PUT methods should have a body (in validation)
* URL must use HTTPS (in validation)

## 🛠️ Development

### Running Tests

```bash
# Unit tests
./gradlew test

# Code coverage report
./gradlew coverage

# Static analysis
./gradlew detekt
```

### Code Quality Tools

* **detekt** - Static code analysis for Kotlin
* **jacoco** - Code coverage (75%+ required)
* **dokka** - Documentation generation

## 📊 Project Status

**Development Status**: Active
**Code Coverage**: 75%+ required
**Kotlin Version**: 2.2.21
**Java Version**: 21

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Ensure all tests pass
4. Submit a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🏗️ Built With

* [Kotlin](https://kotlinlang.org/) - Programming language
* [Gradle](https://gradle.org/) - Build system
* [JUnit 5](https://junit.org/junit5/) - Testing framework
* [MockK](https://mockk.io/) - Mocking library
* [detekt](https://detekt.dev/) - Static code analysis

## 📋 Versioning

This project uses [Semantic Versioning](http://semver.org/).

Version format: `MAJOR.MINOR.PATCH`

- **MAJOR**: Breaking changes
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes

## 👨‍💻 Author

* **Rafał Kociniewski** - [rkociniewski](https://github.com/rkociniewski)

## 🎯 Use Cases

### REST API Client

```kotlin
val client = RequestTemplates.authenticated(
    baseUrl = "https://api.myservice.com",
    token = authToken
) {
    path("/v1/users")
    method(HttpMethod.GET)
}
```

### File Upload

```kotlin
val uploadRequest = httpRequest {
    url("https://api.example.com/upload")
    method(HttpMethod.POST)

    headers {
        "Content-Type" to "application/octet-stream"
    }

    body {
        raw(fileBytes)
    }
}
```

### GraphQL Query

```kotlin
val graphqlRequest = httpRequest {
    url("https://api.example.com/graphql")
    method(HttpMethod.POST)

    headers {
        "Content-Type" to "application/json"
    }

    body {
        json {
            "query" to """
                query {
                    user(id: 123) {
                        name
                        email
                    }
                }
            """.trimIndent()
        }
    }
}
```

## 🔍 Error Examples

```kotlin
// ❌ Empty URL
httpRequest {
    url("") // IllegalArgumentException: URL must be non-empty
}

// ❌ Empty headers
httpRequest {
    url("https://api.example.com")
    headers {
        "" to "value" // IllegalArgumentException: Header name cannot be empty
    }
}

// ❌ Body set twice
httpRequest {
    url("https://api.example.com")
    body {
        text("First")
        text("Second") // IllegalStateException: Body can only be set once
    }
}

// ❌ Negative timeout
httpRequest {
    url("https://api.example.com")
    timeout {
        connect = -1000 // IllegalArgumentException: Connect timeout must be positive
    }
}
```

## 📞 Support

* **Issues**: [GitHub Issues](https://github.com/rkociniewski/http-request-dsl/issues)
* **Discussions**: [GitHub Discussions](https://github.com/rkociniewski/http-request-dsl/discussions)

---

Made with ❤️ and 🙏 by [Rafał Kociniewski](https://github.com/rkociniewski)
