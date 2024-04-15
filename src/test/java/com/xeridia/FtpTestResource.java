package com.xeridia;

import java.io.File;
import java.util.Map;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.apache.camel.util.CollectionHelper;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

public class FtpTestResource implements QuarkusTestResourceLifecycleManager {

    private static final int FTP_PORT = 2222;
    private static final String SSH_IMAGE = "quay.io/jamesnetherton/sftp-server:0.6.0";

    private static final String FTP_USER = "user";
    private static final String FTP_PASSWORD = "password";

    private GenericContainer container;

    @Override
    public Map<String, String> start() {
        try {

            File downloadDir = new File("C:/entornos/Blockchain/workspaces/quarkus-camel/target/downloads");
            downloadDir.delete();

            container = new GenericContainer(SSH_IMAGE)
                    .withExposedPorts(FTP_PORT)
                    .withEnv("PASSWORD_ACCESS", "true")
                    .withEnv("FTP_USER", FTP_USER)
                    .withEnv("FTP_PASSWORD", FTP_PASSWORD)
                    .withCopyFileToContainer(
                            MountableFile.forClasspathResource("/download/file_to_download.txt"),
                            "/deployments/downloads/books/file_to_download.txt"
                    )
                    .waitingFor(Wait.forListeningPort());

            container.start();

            return CollectionHelper.mapOf(
                    "ftp.host", container.getHost(),
                    "ftp.port", container.getMappedPort(FTP_PORT).toString(),
                    "ftp.user", FTP_USER,
                    "ftp.password", FTP_PASSWORD,
                    "timer.delay", "100");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        if (container != null) {
            container.stop();
        }
    }
}