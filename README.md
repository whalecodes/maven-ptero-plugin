# maven-ptero-plugin

A Maven plugin that ships your compiled project to a [Pterodactyl](https://pterodactyl.io/) server:
1. compiles the project 
2. uploads the artifact to a configured directory on the server
3. optionally restarts it so the new build goes live 

The plugin is most useful for a testing workflow where you are frequently editing code, rebuilding, and uploading to your server.
You can use the `deploy` goal to run this entire loop in one click. 

## Goals

The plugin is bound to the goal prefix `pterodactyl` and exposes two goals:

| Goal                 | What it does                                                                                    |
|----------------------|-------------------------------------------------------------------------------------------------|
| `pterodactyl:upload` | compiles project & uploads the built artifact to the configured directory on the target server. |
| `pterodactyl:deploy` | performs `upload`, then restarts the server.                                                    |

You run them like any other Maven goal:

```bash
mvn pterodactyl:upload
mvn pterodactyl:deploy
```

In IntelliJ IDEA, these also appear in the Maven tab under Plugins > pterodactyl.

## Setup

There is a 3-step configuration process to resolve the plugin and securely authorize it to interact with your panel.

### 1. Add the plugin repository to `pom.xml`

The plugin is hosted on the MineVilla repository. Add it under `<pluginRepositories>`:

```xml
<pluginRepositories>
  <pluginRepository>
    <id>minevilla-repo</id>
    <url>https://repo.minevilla.net/releases</url>
  </pluginRepository>
</pluginRepositories>
```

### 2. Configure the plugin in `pom.xml`

```xml
<plugin>
  <groupId>codes.whale</groupId>
  <artifactId>maven-ptero-plugin</artifactId>
  <version>1.0.0</version>
  <configuration>
    <panel>my-panel</panel>
    <server>server-identifier</server>
    <directory>/myFolder</directory>
  </configuration>
</plugin>
```

| Field | Description |
| ----- | ----------- |
| `panel` | Matches the `<id>` of a `<server>` in your `settings.xml`. Supplies the panel URL and API key. |
| `server` | The identifier of the Pterodactyl server to upload to. |
| `directory` | The directory on that server where the artifact should be placed (e.g. `/plugins`). |

### 3. Add credentials to `~/.m2/settings.xml`

```xml
<settings>
  <servers>
    <server>
      <id>my-panel</id>
      <password>your-api-key</password>
      <configuration>
        <url>https://panel.example.com</url>
      </configuration>
    </server>
  </servers>
</settings>
```

| Field | Description |
| ----- | ----------- |
| `id` | Must match the `panel` value in the plugin configuration. |
| `password` | Your Pterodactyl panel API key. |
| `configuration/url` | The base URL of your Pterodactyl panel. |