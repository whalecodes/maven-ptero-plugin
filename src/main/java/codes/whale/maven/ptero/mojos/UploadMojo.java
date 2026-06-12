package codes.whale.maven.ptero.mojos;

import codes.whale.maven.ptero.AbstractPteroMojo;
import codes.whale.maven.ptero.PteroClient;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Compiles the project and uploads the built artifact to the configured server directory.
 */
@Mojo(name = "upload")
@Execute(phase = LifecyclePhase.PACKAGE)
public class UploadMojo extends AbstractPteroMojo {

    @Override
    protected void afterUpload(PteroClient client, String server)
            throws MojoExecutionException {
        // noop - abstract mojo does everything already
    }

}
