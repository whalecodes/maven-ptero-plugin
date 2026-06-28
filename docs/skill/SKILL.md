---
name: use-maven-pterodactyl-plugin
description: Compile a Maven project and upload or deploy the built artifact to a Pterodactyl game-server panel. Use when the user wants to push, upload, or deploy a built plugin/jar to their Pterodactyl server through Maven.
---

# maven-ptero-plugin

A Maven plugin (goal prefix `pterodactyl`) that ships a compiled project to a Pterodactyl
server. It packages the project, uploads the resulting jar to a configured directory on a
target server, and can optionally restart the server so the new build goes live.

## How it works

Two goals, both of which package the project automatically (no separate `mvn package` needed):

| Goal                 | What it does                                                   |
|----------------------|---------------------------------------------------------------|
| `pterodactyl:upload` | Package the project and upload the jar to the server directory. |
| `pterodactyl:deploy` | `upload`, then restart the server.                            |

Use `upload` when the user just wants the new jar on the server. Use `deploy` when they want
it live — anything like "restart", "make it live", "deploy", or "redeploy".

## Before running

Check that the project is configured. If anything below is missing, point the user to **Setup**
rather than guessing values — never invent a panel URL, API key, server id, or directory.

1. `pom.xml` declares the plugin with `<panel>`, `<server>`, and `<directory>`, plus the
   `<pluginRepository>` that resolves it.
2. `~/.m2/settings.xml` has a `<server>` whose `<id>` matches the plugin's `<panel>` value,
   supplying the API key and panel URL.

## Setup

**1. Plugin repository** in `pom.xml`:

```xml
<pluginRepositories>
  <pluginRepository>
    <id>minevilla-repo</id>
    <url>https://repo.minevilla.net/releases</url>
  </pluginRepository>
</pluginRepositories>
```

**2. Plugin configuration** in `pom.xml`:

```xml
<plugin>
  <groupId>codes.whale</groupId>
  <artifactId>maven-ptero-plugin</artifactId>
  <version>1.0.0</version>
  <configuration>
    <panel>my-panel</panel>
    <server>server-identifier</server>
    <directory>/plugins</directory>
  </configuration>
</plugin>
```

**3. Credentials** in `~/.m2/settings.xml` (never in the repo):

```xml
<server>
  <id>my-panel</id>
  <password>your-api-key</password>
  <configuration>
    <url>https://panel.example.com</url>
  </configuration>
</server>
```

`panel` must match the settings `<server>` `<id>`. `server` is the Pterodactyl server
identifier; `directory` is the upload target on that server (e.g. `/plugins`).

## Running

```bash
mvn pterodactyl:upload   # package + upload
mvn pterodactyl:deploy   # package + upload + restart
```

Success is logged as `Upload complete.` and, for `deploy`, `Restart signal sent.`

## Troubleshooting

- **"Artifact not found … run mvn package first"** — the build produced no jar, or the
  `finalName` differs from what was expected. Confirm the project packages cleanly.
- **"Panel '…' is not defined / missing `<password>` / missing `<configuration><url>`"** —
  the `settings.xml` entry is missing or incomplete. See Setup step 3.
- **HTTP errors on upload/restart** — usually a bad API key, wrong server identifier, or a
  directory that doesn't exist on the server.

For full user-facing details, the [README](../../README.md) is the source of truth.
