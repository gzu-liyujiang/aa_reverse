package jadx.core.utils;

import jadx.api.JadxPreferences;

public class StringUtils {
    
	public String unescapeString(String str) {
		int len = str.length();
		if (len == 0) {
			return "\"\"";
		}
		StringBuilder res = new StringBuilder();
		for (int i = 0; i < len; i++) {
			int c = str.charAt(i) & 0xFFFF;
			processChar(c, res);
		}
		return '"' + res.toString() + '"';
	}

	public String unescapeChar(char ch) {
		if (ch == '\'') {
			return "'\\\''";
		}
		StringBuilder res = new StringBuilder();
		processChar(ch, res);
		return '\'' + res.toString() + '\'';
	}

	private void processChar(int c, StringBuilder res) {
		switch (c) {
			case '\n': res.append("\\n"); break;
			case '\r': res.append("\\r"); break;
			case '\t': res.append("\\t"); break;
			case '\b': res.append("\\b"); break;
			case '\f': res.append("\\f"); break;
			case '\'': res.append('\''); break;
			case '"': res.append("\\\""); break;
			case '\\': res.append("\\\\"); break;

			default:
				if (c < 32 || c >= 127&&JadxPreferences.Option_GetZ(JadxPreferences.OPT_ESCAPE_UNICODE)) {
					res.append("\\u").append(String.format("%04x", c));
				} else {
					res.append((char) c);
				}
				break;
		}
	}

	public static String escape(String str) {
		int len = str.length();
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			char c = str.charAt(i);
			switch (c) {
				case '.':
				case '/':
				case ';':
				case '$':
				case ' ':
				case ',':
				case '<':
					sb.append('_');
					break;

				case '[':
					sb.append('A');
					break;

				case ']':
				case '>':
				case '?':
				case '*':
					break;

				default:
					sb.append(c);
					break;
			}
		}
		return sb.toString();
	}
}
