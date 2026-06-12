package codes.whale.maven.ptero;

import codes.whale.maven.ptero.errors.PterodactylException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;

import java.io.File;

/**
 * Base for the plugin's goals: resolves the panel credentials, uploads the built
 * artifact to the configured server, then lets subclasses run follow-up steps.
 */
public abstract class AbstractPteroMojo extends AbstractMojo {

    /**
     * Identifier of the {@code <server>} entry in settings.xml that holds the panel URL and API key.
     */
    @Parameter(property = "ptero.panel", required = true)
    private String panel;

    /**
     * Identifier of the target server on the panel.
     */
    @Parameter(property = "ptero.server", required = true)
    private String server;

    /**
     * Directory on the server to upload the artifact into, e.g. {@code /plugins}.
     */
    @Parameter(property = "ptero.directory", required = true)
    private String directory;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${settings}", readonly = true, required = true)
    private Settings settings;

    @Override
    public final void execute() throws MojoExecutionException {
        File artifact = builtJar();
        if (!artifact.exists())
            throw new MojoExecutionException("Artifact not found at " + artifact.getAbsolutePath() + " — run mvn package first.");

        PteroClient client;
        try {
            client = new PteroClient(PteroPanel.fromSettings(settings, panel));
        } catch (PterodactylException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        getLog().info("Uploading " + artifact.getName() + " → " + panel + ":" + server + ":" + directory);
        try {
            client.uploadFile(server, directory, artifact.toPath());
        } catch (PterodactylException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        getLog().info("Upload complete.");

        afterUpload(client, server);
    }

    private File builtJar() {
        return new File(project.getBuild().getDirectory(), project.getBuild().getFinalName() + ".jar");
    }

    protected abstract void afterUpload(PteroClient client, String server) throws MojoExecutionException;

}
