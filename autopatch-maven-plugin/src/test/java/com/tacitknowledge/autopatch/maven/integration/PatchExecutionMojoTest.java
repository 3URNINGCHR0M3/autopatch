package com.tacitknowledge.autopatch.maven.integration;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.Mojo;

import java.io.File;

import com.tacitknowledge.autopatch.maven.PatchExecutionMojo;

public class PatchExecutionMojoTest extends AbstractMojoTestCase {
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Tests the proper run and configuration of the mojo
     *
     * @throws Exception when errors occures
     */
    public void testCompilerTestEnvironment() throws Exception {
        File pom = new File(getBasedir(), "target/test-classes/plugin-config.xml");
        Mojo mojo = lookupMojo("patch", pom);
        mojo.execute();
    }
}