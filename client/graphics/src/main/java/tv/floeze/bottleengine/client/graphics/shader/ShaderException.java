package tv.floeze.bottleengine.client.graphics.shader;

public class ShaderException extends RuntimeException {

	private static final long serialVersionUID = -7343583764228779327L;

	public ShaderException(String message, String log) {
		super(message + "\n" + log);
	}

}
