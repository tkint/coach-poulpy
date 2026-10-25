---
status: accepted
date: 2026-10-24
---

# One Kotlin Multiplatform core for every platform

Android, iOS, the server and the website must run workouts identically. Domain types, the workout engine, workout type modules and stats live in shared Kotlin modules; Compose and SwiftUI only render and talk to hardware, and the server reuses the same modules on the JVM for validation. The website runs the engine compiled to JavaScript (decided 24 October 2026) rather than a TypeScript rewrite: a Kotlin/JS build step costs less than writing and testing the engine twice.

## Consequences

- Modules the website needs use the `coachpoulpy.kmp-web-library` convention and must stay free of JVM-only or mobile-only dependencies.
- iOS consumes everything through the `shared` umbrella framework.
