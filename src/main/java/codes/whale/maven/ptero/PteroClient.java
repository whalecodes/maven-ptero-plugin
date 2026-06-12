package codes.whale.maven.ptero;

import codes.whale.maven.ptero.errors.PterodactylException;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A minimal client for the parts of the Pterodactyl client API this plugin needs.
 */
public final class PteroClient {

    private static final Pattern UPLOAD_URL_PATTERN =
            Pattern.compile("\"attributes\"\\s*:\\s*\\{[\\s\\S]*?\"url\"\\s*:\\s*\"(.*?)\"");

    /*
     * Wings mounts the server root here; the files API expects paths relative to it.
     */
    private static final String CONTAINER_ROOT = "/home/container";

    private final String baseUrl;
    private final String apiKey;
    private final HttpClient http;

    public PteroClient(@NotNull PteroPanel panel) {
        this.baseUrl = panel.url().replaceAll("/+$", "");
        this.apiKey = panel.apiKey();
        this.http = HttpClient.newHttpClient();
    }

    public void uploadFile(@NotNull String server, @NotNull String directory, @NotNull Path file) throws PterodactylException {
        String normalizedDirectory = normalizeDirectory(directory);
        String uploadUrl = requestUploadUrl(server);
        postFile(uploadUrl, normalizedDirectory, file);
    }

    public void restartServer(@NotNull String server) throws PterodactylException {
        HttpRequest request = HttpRequest.newBuilder(clientApiUri(server, "/power"))
                .header("Authorization", "Bearer " + this.apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"signal\":\"restart\"}"))
                .build();

        HttpResponse<String> response = send(request, "restart server");
        if (response.statusCode() != 204)
            throw new PterodactylException("Failed to restart server: HTTP " + response.statusCode() + " — " + response.body());
    }

    /**
     * Asks the panel for a one-time signed URL to upload files to the server.
     */
    private @NotNull String requestUploadUrl(@NotNull String server) throws PterodactylException {
        HttpRequest request = HttpRequest.newBuilder(clientApiUri(server, "/files/upload"))
                .header("Authorization", "Bearer " + this.apiKey)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = send(request, "request upload URL");
        if (response.statusCode() != 200)
            throw new PterodactylException("Failed to get upload URL: HTTP " + response.statusCode() + " — " + response.body());

        Matcher matcher = UPLOAD_URL_PATTERN.matcher(response.body());
        if (!matcher.find())
            throw new PterodactylException("Could not parse upload URL from response: " + response.body());

        return matcher.group(1).replace("\\/", "/");
    }

    private void postFile(@NotNull String uploadUrl, @NotNull String directory, @NotNull Path file) throws PterodactylException {
        String boundary = "----MavenPteroPlugin" + System.currentTimeMillis();
        byte[] body;
        try {
            body = buildMultipartBody(boundary, file.getFileName().toString(), Files.readAllBytes(file));
        } catch (IOException e) {
            throw new PterodactylException("Could not read " + file + ": " + e.getMessage(), e);
        }

        String separator = uploadUrl.contains("?") ? "&" : "?";
        URI uri = URI.create(uploadUrl + separator + "directory=" + encodePath(directory));

        HttpRequest request = HttpRequest.newBuilder(uri)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpResponse<String> response = send(request, "upload file");
        if (response.statusCode() < 200 || response.statusCode() >= 300)
            throw new PterodactylException("Upload failed: HTTP " + response.statusCode() + " — " + response.body());
    }

    private @NotNull HttpResponse<String> send(@NotNull HttpRequest request, @NotNull String action) throws PterodactylException {
        try {
            return this.http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new PterodactylException("Failed to " + action + ": " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PterodactylException("Interrupted while trying to " + action + ".", e);
        }
    }

    private @NotNull URI clientApiUri(@NotNull String server, @NotNull String path) {
        return URI.create(this.baseUrl + "/api/client/servers/" + server + path);
    }

    private static byte[] buildMultipartBody(@NotNull String boundary, @NotNull String filename, byte[] fileBytes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String crlf = "\r\n";

        out.write(("--" + boundary + crlf).getBytes(UTF_8));
        out.write(("Content-Disposition: form-data; name=\"files\"; filename=\"" + filename + "\"" + crlf).getBytes(UTF_8));
        out.write(("Content-Type: application/java-archive" + crlf).getBytes(UTF_8));
        out.write(crlf.getBytes(UTF_8));
        out.write(fileBytes);
        out.write(crlf.getBytes(UTF_8));
        out.write(("--" + boundary + "--" + crlf).getBytes(UTF_8));

        return out.toByteArray();
    }

    /**
     * Trims the container root prefix and collapses the path to an absolute, slash-separated form.
     */
    private static @NotNull String normalizeDirectory(@NotNull String directory) {
        String normalized = directory.trim().replace('\\', '/');

        if (normalized.startsWith(CONTAINER_ROOT))
            normalized = normalized.substring(CONTAINER_ROOT.length());

        normalized = normalized.replaceAll("/+", "/");
        if (normalized.isBlank() || normalized.equals("/"))
            return "/";

        return normalized.startsWith("/") ? normalized : "/" + normalized;
    }

    private static @NotNull String encodePath(@NotNull String path) {
        return URLEncoder.encode(path, UTF_8)
                .replace("+", "%20")
                .replace("%2F", "/");
    }

}
