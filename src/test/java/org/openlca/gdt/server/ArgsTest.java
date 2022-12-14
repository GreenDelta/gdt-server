package org.openlca.gdt.server;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;

import org.junit.Test;
import org.openlca.core.services.ServerConfig;
import org.openlca.util.Dirs;

public class ArgsTest {

	@Test
	public void testParseArgs() throws Exception {

		var rootDir = Files.createTempDirectory("olca-tests").toFile();
		var libDir = new File(rootDir, "native");

		var args = new String[] {
				"-data", rootDir.getAbsolutePath(),
				"-db", "testdb",
				"-port", "7777",
				"-native", libDir.getAbsolutePath(),
		};

		var conf = ServerConfig.parse(args);

		assertEquals(rootDir, conf.dataDir().root());
		assertEquals(7777, conf.port());
		assertNotNull(conf.db());
		assertEquals("testdb", conf.db().getName());
		conf.db().close();

		Dirs.delete(rootDir);
	}

}
