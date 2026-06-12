package codes.whale.maven.ptero.mojos;

import codes.whale.maven.ptero.AbstractPteroMojo;
import codes.whale.maven.ptero.PteroClient;
import codes.whale.maven.ptero.errors.PterodactylException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Compiles and uploads the project, then restarts the server so the new build goes live.
 */
@Mojo(name = "deploy")
@Execute(phase = LifecyclePhase.PACKAGE)
public class DeployMojo extends AbstractPteroMojo {

    @Override
    protected void afterUpload(PteroClient client, String server)
            throws MojoExecutionException {
        getLog().info("Restarting server " + server + "...");
        try {
            client.restartServer(server);
        } catch (PterodactylException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        getLog().info("Restart signal sent.");
    }

}
