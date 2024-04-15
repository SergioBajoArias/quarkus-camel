package com.xeridia;

import com.jcraft.jsch.*;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

@QuarkusTest
@QuarkusTestResource(FtpTestResource.class)
public class FtpTest {
    @ConfigProperty(name = "ftp.download.path")
    String downloadPath;

    private static Session session;

    @BeforeAll
    public static void beforeAll() throws JSchException {
        Config config = ConfigProvider.getConfig();

        JSch jsch = new JSch();
        jsch.setKnownHosts(config.getValue("user.home", String.class) + "/.ssh/known_hosts");

        session = jsch.getSession(config.getValue("ftp.user", String.class), config.getValue("ftp.host", String.class));
        session.setPort(Integer.parseInt(config.getValue("ftp.port", String.class)));
        session.setPassword(config.getValue("ftp.password", String.class));
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(5000);
    }

    @Test
    public void testFileToFtp() throws JSchException {
        Channel sftp = null;
        try {
            sftp = session.openChannel("sftp");
            sftp.connect(5000);

            ChannelSftp channelSftp = (ChannelSftp) sftp;

            await().atMost(10L, TimeUnit.SECONDS).pollDelay(500, TimeUnit.MILLISECONDS).until(() -> {
                try {
                    return !channelSftp.ls("uploads/books/*.txt").isEmpty();
                } catch (Exception e) {
                    return false;
                }
            });
        } finally {
            if (sftp != null) {
                sftp.disconnect();
            }
        }
    }

    @Test
    public void testFtpToFile() throws JSchException {
        File downloadDir = new File(downloadPath);
        await().atMost(10L, TimeUnit.SECONDS).pollDelay(500, TimeUnit.MILLISECONDS).until(() -> {
            return downloadDir.exists() && downloadDir.list().length > 0;
        });
    }
}
