package tv.floeze.bottleengine.common.io;

import java.util.Scanner;

public final class ResourceLoader {

	private ResourceLoader() {
	}

	public static String loadTextFromResources(String path) {
		try (Scanner s = new Scanner(ResourceLoader.class.getClassLoader().getResourceAsStream(path))
				.useDelimiter("\\A")) {
			return s.next();
		}
	}

}
