package tv.floeze.bottleengine.common.io;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

public final class ResourceLoader {

	private ResourceLoader() {
	}

	public static String loadTextFromResources(String path) {
		InputStream stream = ResourceLoader.class.getClassLoader().getResourceAsStream(path);
		if (stream == null)
			throw new RuntimeException(new FileNotFoundException("Could not find " + path + " on classpath"));
		try (Scanner s = new Scanner(stream).useDelimiter("\\A")) {
			if (!s.hasNext())
				return "";
			return s.next();
		}
	}

}
