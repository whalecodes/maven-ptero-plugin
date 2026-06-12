# AGENTS.md

## What this project is

`maven-ptero-plugin` is a Maven plugin that uploads a built Maven project to a [Pterodactyl](https://pterodactyl.io/) server. It compiles the project, uploads the resulting artifact to a configured directory on a target server, and can optionally restart that server.

It exposes two goals under the `pterodactyl` goal prefix:

- **`upload`** — compile the project and upload the executable to the configured folder on the server.
- **`deploy`** — compile and upload the project, then restart the server.

Consumers import it via their `pom.xml`, configure it there (panel, server, directory), and supply panel credentials (API key + URL) through a `<server>` entry in their `~/.m2/settings.xml`.

For a full description of the plugin, its goals, and how it is configured, see the [README](./README.md). Keep the README as the source of truth for user-facing behavior and update it when that behavior changes.

## Tech Sack

- Java, built with Maven (`<packaging>maven-plugin</packaging>`).
- Maven Plugin API + plugin annotations (`@Mojo`) for defining goals.
- ``JetBrains annotations are available as provided dependencies.

## Guidelines

- Follow good, clean Java standards. Keep the code readable, well-named, and easy to follow.
- Prefer small, focused classes and methods with a single clear responsibility.
- Match the existing style and conventions of the surrounding code.
- Don't over-engineer — keep solutions as simple as the problem allows.
- Never commit secrets. API keys and panel URLs belong in the user's `settings.xml`, never in the repository.
- When you change user-facing behavior (goals, configuration, setup), update the [README](./README.md) to match.
- Avoid writing comments unless asked or the method is doing something non-obvious. 
