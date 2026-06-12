package codes.whale.maven.ptero;

import codes.whale.maven.ptero.errors.PterodactylException;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Credentials for a Pterodactyl panel, resolved from a {@code <server>} entry in the
 * user's {@code settings.xml}: the API key comes from {@code <password>} and the panel
 * base URL from {@code <configuration><url>}.
 */
public record PteroPanel(@NotNull String url, @NotNull String apiKey) {

    public static @NotNull PteroPanel fromSettings(@NotNull Settings settings, @NotNull String panelId)
            throws PterodactylException {
        Server server = settings.getServer(panelId);
        if (server == null)
            throw new PterodactylException("Panel '" + panelId + "' is not defined in settings.xml — add a <server> with this id under <servers>.");

        String apiKey = server.getPassword();
        if (apiKey == null || apiKey.isBlank())
            throw new PterodactylException("Panel '" + panelId + "' is missing <password> (the API key) in settings.xml.");

        String url = configuredUrl(server);
        if (url == null || url.isBlank())
            throw new PterodactylException("Panel '" + panelId + "' is missing <configuration><url> in settings.xml.");

        return new PteroPanel(url, apiKey);
    }

    private static @Nullable String configuredUrl(@NotNull Server server) {
        if (!(server.getConfiguration() instanceof Xpp3Dom configuration))
            return null;

        Xpp3Dom url = configuration.getChild("url");
        return url == null ? null : url.getValue();
    }

}
