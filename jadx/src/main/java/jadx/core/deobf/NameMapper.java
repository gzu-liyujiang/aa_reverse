package jadx.core.deobf;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class NameMapper {

	private static final Pattern VALID_JAVA_IDENTIFIER = Pattern.compile(
			"\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*");

	private static final Pattern VALID_JAVA_FULL_IDENTIFIER = Pattern.compile(
			"(" + VALID_JAVA_IDENTIFIER + "\\.)*" + VALID_JAVA_IDENTIFIER);

	private static final Set<String> RESERVED_NAMES = new HashSet<String>(
			Arrays.asList(new String[]{
					"abstract",
					"assert",
					"boolean",
					"break",
					"byte",
					"case",
					"catch",
					"char",
					"class",
					"const",
					"continue",
					"default",
					"do",
					"double",
					"else",
					"enum",
					"extends",
					"false",
					"final",
					"finally",
					"float",
					"for",
					"goto",
					"if",
					"implements",
					"import",
					"instanceof",
					"int",
					"interface",
					"long",
					"native",
					"new",
					"null",
					"package",
					"private",
					"protected",
					"public",
					"return",
					"short",
					"static",
					"strictfp",
					"super",
					"switch",
					"synchronized",
					"this",
					"throw",
					"throws",
					"transient",
					"true",
					"try",
					"void",
					"volatile",
					"while",
			})
	);

	public static boolean isReserved(String str) {
		return RESERVED_NAMES.contains(str);
	}

    /*
	public static boolean isValidIdentifier(String str) {
		return VALID_JAVA_IDENTIFIER.matcher(str).matches() && isAllCharsPrintable(str);
	}*/
}
