package com.xeridia;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@ApplicationScoped
public class Routes extends RouteBuilder {

    @ConfigProperty(name = "ftp.download.path")
    String downloadPath;

    @Override
    public void configure() {
        from("file://C:/entornos/Blockchain/workspaces/quarkus-camel/src/test/resources/upload?noop=true")
                .log("Uploading files to FTP server")
                .to("sftp://{{ftp.username}}@{{ftp.host}}:{{ftp.port}}/uploads/books?password={{ftp.password}}")
                .log("Files uploaded");

        from("sftp://{{ftp.username}}@{{ftp.host}}:{{ftp.port}}/downloads/books?password={{ftp.password}}")
                .log("Downloading files from FTP server")
                .to("file://" + downloadPath)
                .log("Files downloaded");
    }
}
