package logic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import bean.ImportRow;

/**
 * 学校から届いた学生ファイル（CSV・xlsx）を読んで、ImportRow のリストにする
 * 1行目は見出し行。列の順番は問わず、見出しの名前（氏名・学生番号・クラス・生年月日）で探す
 */
public class ImportFileReader {

	/**
	 * ファイルの最大サイズ（バイト）
	 */
	public static final int MAX_BYTES = 2 * 1024 * 1024;

	/**
	 * 読み込める最大行数
	 */
	public static final int MAX_ROWS = 2000;

	/**
	 * xlsx の中のファイル1つの最大サイズ（zip爆弾の対策）
	 */
	private static final int MAX_ENTRY_BYTES = 20 * 1024 * 1024;

	/**
	 * readメソッド ファイルを読んで学生の行のリストにする
	 *
	 * @param data:byte[] ファイルの中身
	 * @param fileName:String ファイル名（拡張子で形式を決める）
	 * @return 学生の行のリスト（エラーのある行は error に理由が入る）:List<ImportRow>
	 * @throws Exception ファイル全体が読めない場合（形式・見出し・行数など）。メッセージは画面に出せる文章
	 */
	public static List<ImportRow> read(byte[] data, String fileName) throws Exception {
		if (data == null || data.length == 0) {
			throw new Exception("ファイルが空です");
		}
		if (data.length > MAX_BYTES) {
			throw new Exception("ファイルが大きすぎます（2MBまで）");
		}

		String lower = fileName == null ? "" : fileName.toLowerCase();
		List<List<String>> table;
		if (lower.endsWith(".csv") || lower.endsWith(".txt")) {
			table = readCsv(data);
		} else if (lower.endsWith(".xlsx")) {
			table = readXlsx(data);
		} else {
			throw new Exception("CSV または xlsx のファイルを選んでください（古い .xls は xlsx か CSV で保存し直してください）");
		}
		return toRows(table);
	}

	// ------------------------------------------------------------
	// 表（文字列の二次元リスト）を ImportRow にする
	// ------------------------------------------------------------

	/**
	 * 1行目の見出しから列を探し、2行目以降を ImportRow にする
	 * 表の行番号は ImportRow.lineNo に入れる（見出しが1行目）
	 */
	private static List<ImportRow> toRows(List<List<String>> table) throws Exception {
		int headerIndex = -1;
		for (int i = 0; i < table.size(); i++) {
			if (!isBlankRow(table.get(i))) {
				headerIndex = i;
				break;
			}
		}
		if (headerIndex < 0) {
			throw new Exception("ファイルにデータがありません");
		}

		int nameCol = -1;
		int noCol = -1;
		int classCol = -1;
		int birthCol = -1;
		List<String> header = table.get(headerIndex);
		for (int c = 0; c < header.size(); c++) {
			String h = normalizeHeader(header.get(c));
			if (nameCol < 0 && isNameHeader(h)) {
				nameCol = c;
			} else if (noCol < 0 && (h.contains("学生番号") || h.contains("学籍番号") || h.equals("学番"))) {
				noCol = c;
			} else if (classCol < 0 && (h.contains("クラス") || h.equals("組") || h.equals("学級"))) {
				classCol = c;
			} else if (birthCol < 0 && (h.contains("生年月日") || h.contains("誕生日") || h.contains("birth"))) {
				birthCol = c;
			}
		}

		List<String> missing = new ArrayList<>();
		if (nameCol < 0) {
			missing.add("氏名");
		}
		if (noCol < 0) {
			missing.add("学生番号");
		}
		if (classCol < 0) {
			missing.add("クラス");
		}
		if (birthCol < 0) {
			missing.add("生年月日");
		}
		if (!missing.isEmpty()) {
			throw new Exception("1行目の見出しに「" + String.join("」「", missing) + "」の列が見つかりません");
		}

		List<ImportRow> rows = new ArrayList<>();
		for (int i = headerIndex + 1; i < table.size(); i++) {
			List<String> cells = table.get(i);
			if (isBlankRow(cells)) {
				continue;
			}
			if (rows.size() >= MAX_ROWS) {
				throw new Exception("行数が多すぎます（" + MAX_ROWS + "行まで）");
			}
			ImportRow row = new ImportRow();
			row.setLineNo(i + 1);
			row.setName(cell(cells, nameCol));
			row.setStudentNo(normalizeNumber(cell(cells, noCol)));
			row.setClassName(cell(cells, classCol));

			String birthText = cell(cells, birthCol);
			LocalDate birthday = parseBirthday(birthText);
			row.setBirthday(birthday);

			if (row.getName().isEmpty()) {
				row.setError("氏名が空です");
			} else if (row.getStudentNo().isEmpty()) {
				row.setError("学生番号が空です");
			} else if (row.getClassName().isEmpty()) {
				row.setError("クラスが空です");
			} else if (birthText.isEmpty()) {
				row.setError("生年月日が空です");
			} else if (birthday == null) {
				row.setError("生年月日の形式が正しくありません（" + birthText + "）");
			}
			rows.add(row);
		}
		if (rows.isEmpty()) {
			throw new Exception("見出しの下に学生のデータがありません");
		}
		return rows;
	}

	private static boolean isNameHeader(String h) {
		if (h.contains("カナ") || h.contains("ｶﾅ") || h.contains("フリガナ") || h.contains("ふりがな") || h.contains("読み")
				|| h.contains("ローマ")) {
			return false;
		}
		return h.contains("氏名") || h.equals("名前") || h.equals("学生名") || h.equals("name");
	}

	/**
	 * 見出しの空白（全角も）を消して小文字にそろえる
	 */
	private static String normalizeHeader(String s) {
		return s == null ? "" : s.replace("　", "").replace(" ", "").trim().toLowerCase();
	}

	private static String cell(List<String> cells, int col) {
		if (col >= cells.size() || cells.get(col) == null) {
			return "";
		}
		return cells.get(col).replace("　", " ").trim();
	}

	private static boolean isBlankRow(List<String> cells) {
		for (String s : cells) {
			if (s != null && !s.trim().isEmpty()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 数字だけのセルが「2502027.0」のようになっていたら小数点以下を消す
	 */
	private static String normalizeNumber(String s) {
		if (s.matches("\\d+\\.0+")) {
			return s.substring(0, s.indexOf('.'));
		}
		return s;
	}

	/**
	 * parseBirthdayメソッド いろいろな書き方の生年月日を LocalDate にする
	 * 対応：2006-10-04 / 2006/10/4 / 2006.10.4 / 2006年10月4日 / 20061004 / Excelの日付の数字（シリアル値）
	 *
	 * @return 日付 読めない・ありえない日付の場合は null
	 */
	public static LocalDate parseBirthday(String text) {
		if (text == null) {
			return null;
		}
		String s = text.trim();
		if (s.isEmpty()) {
			return null;
		}
		try {
			LocalDate date = null;
			// 2006-10-04 2006/10/4 2006.10.4 2006年10月4日
			java.util.regex.Matcher m = java.util.regex.Pattern
					.compile("^(\\d{4})[-/.年](\\d{1,2})[-/.月](\\d{1,2})日?$").matcher(s);
			if (m.matches()) {
				date = LocalDate.of(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)),
						Integer.parseInt(m.group(3)));
			} else if (s.matches("\\d{8}")) {
				// 20061004
				date = LocalDate.of(Integer.parseInt(s.substring(0, 4)), Integer.parseInt(s.substring(4, 6)),
						Integer.parseInt(s.substring(6, 8)));
			} else if (s.matches("\\d{4,6}(\\.0+)?")) {
				// Excelの日付の数字（1900年1月1日が1）
				int serial = Integer.parseInt(s.replaceAll("\\.0+$", ""));
				date = LocalDate.of(1899, 12, 30).plusDays(serial);
			}
			if (date == null || date.getYear() < 1900 || date.isAfter(LocalDate.now())) {
				return null;
			}
			return date;
		} catch (Exception e) {
			// 13月・32日など、存在しない日付
			return null;
		}
	}

	// ------------------------------------------------------------
	// CSV
	// ------------------------------------------------------------

	/**
	 * CSVを読む。文字コードはまず UTF-8 を試し、だめなら Shift_JIS（Windows-31J）で読む
	 * （ExcelからCSVで保存すると Shift_JIS になることが多い）
	 */
	private static List<List<String>> readCsv(byte[] data) throws Exception {
		String text;
		try {
			text = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
					.onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(data)).toString();
		} catch (CharacterCodingException e) {
			text = new String(data, Charset.forName("windows-31j"));
		}
		// 先頭のBOMを消す
		if (!text.isEmpty() && text.charAt(0) == '﻿') {
			text = text.substring(1);
		}

		// 区切りは、見出し行にタブが多ければタブ、そうでなければカンマ
		int firstLineEnd = text.indexOf('\n');
		String firstLine = firstLineEnd < 0 ? text : text.substring(0, firstLineEnd);
		char delimiter = count(firstLine, '\t') > count(firstLine, ',') ? '\t' : ',';

		List<List<String>> table = new ArrayList<>();
		List<String> row = new ArrayList<>();
		StringBuilder field = new StringBuilder();
		boolean inQuotes = false;
		boolean hasContent = false;
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (inQuotes) {
				if (ch == '"') {
					if (i + 1 < text.length() && text.charAt(i + 1) == '"') {
						field.append('"');
						i++;
					} else {
						inQuotes = false;
					}
				} else {
					field.append(ch);
				}
			} else if (ch == '"') {
				inQuotes = true;
				hasContent = true;
			} else if (ch == delimiter) {
				row.add(field.toString());
				field.setLength(0);
				hasContent = true;
			} else if (ch == '\r' || ch == '\n') {
				if (ch == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') {
					i++;
				}
				row.add(field.toString());
				field.setLength(0);
				table.add(row);
				row = new ArrayList<>();
				hasContent = false;
			} else {
				field.append(ch);
				hasContent = true;
			}
		}
		if (hasContent || field.length() > 0 || !row.isEmpty()) {
			row.add(field.toString());
			table.add(row);
		}
		return table;
	}

	private static int count(String s, char ch) {
		int n = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == ch) {
				n++;
			}
		}
		return n;
	}

	// ------------------------------------------------------------
	// xlsx（zipの中のXMLを直接読む。外部ライブラリは使わない）
	// ------------------------------------------------------------

	private static List<List<String>> readXlsx(byte[] data) throws Exception {
		Map<String, byte[]> files = new HashMap<>();
		try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(data))) {
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				String name = entry.getName();
				if (name.equals("xl/workbook.xml") || name.equals("xl/_rels/workbook.xml.rels")
						|| name.equals("xl/sharedStrings.xml") || name.startsWith("xl/worksheets/")) {
					files.put(name, readLimited(zip));
				}
			}
		} catch (java.util.zip.ZipException e) {
			throw new Exception("xlsx ファイルを読めません（壊れているか、xlsx ではありません）");
		}
		if (!files.containsKey("xl/workbook.xml")) {
			throw new Exception("xlsx ファイルを読めません（壊れているか、xlsx ではありません）");
		}

		// 共有文字列（セルの文字は、ここの番号で入っている）
		List<String> shared = new ArrayList<>();
		if (files.containsKey("xl/sharedStrings.xml")) {
			Document doc = parseXml(files.get("xl/sharedStrings.xml"));
			NodeList items = doc.getElementsByTagNameNS("*", "si");
			for (int i = 0; i < items.getLength(); i++) {
				shared.add(allText((Element) items.item(i)));
			}
		}

		// 最初のシートのファイル名を探す
		String sheetPath = firstSheetPath(files);
		if (sheetPath == null || !files.containsKey(sheetPath)) {
			throw new Exception("xlsx ファイルにシートが見つかりません");
		}

		Document sheet = parseXml(files.get(sheetPath));
		NodeList rowNodes = sheet.getElementsByTagNameNS("*", "row");
		List<List<String>> table = new ArrayList<>();
		for (int i = 0; i < rowNodes.getLength(); i++) {
			Element rowElement = (Element) rowNodes.item(i);
			// 行番号（空行を飛ばしている場合のため、番号どおりの位置に入れる）
			int rowNumber = parseIntOr(rowElement.getAttribute("r"), table.size() + 1);
			while (table.size() < rowNumber - 1) {
				table.add(new ArrayList<>());
			}
			List<String> cells = new ArrayList<>();
			NodeList cellNodes = rowElement.getElementsByTagNameNS("*", "c");
			for (int j = 0; j < cellNodes.getLength(); j++) {
				Element c = (Element) cellNodes.item(j);
				int col = columnIndex(c.getAttribute("r"), cells.size());
				while (cells.size() < col) {
					cells.add("");
				}
				cells.add(cellText(c, shared));
			}
			table.add(cells);
		}
		return table;
	}

	private static String cellText(Element c, List<String> shared) {
		String type = c.getAttribute("t");
		if ("inlineStr".equals(type)) {
			return allText(c);
		}
		NodeList values = c.getElementsByTagNameNS("*", "v");
		if (values.getLength() == 0) {
			return "";
		}
		String v = values.item(0).getTextContent().trim();
		if ("s".equals(type)) {
			int index = parseIntOr(v, -1);
			return (index >= 0 && index < shared.size()) ? shared.get(index) : "";
		}
		return v;
	}

	/**
	 * workbook.xml と rels から、最初のシートのファイルの場所を探す
	 */
	private static String firstSheetPath(Map<String, byte[]> files) throws Exception {
		Document workbook = parseXml(files.get("xl/workbook.xml"));
		NodeList sheets = workbook.getElementsByTagNameNS("*", "sheet");
		if (sheets.getLength() > 0 && files.containsKey("xl/_rels/workbook.xml.rels")) {
			Element first = (Element) sheets.item(0);
			String relId = first.getAttributeNS("http://schemas.openxmlformats.org/officeDocument/2006/relationships",
					"id");
			Document rels = parseXml(files.get("xl/_rels/workbook.xml.rels"));
			NodeList list = rels.getElementsByTagNameNS("*", "Relationship");
			for (int i = 0; i < list.getLength(); i++) {
				Element rel = (Element) list.item(i);
				if (rel.getAttribute("Id").equals(relId)) {
					String target = rel.getAttribute("Target");
					if (target.startsWith("/")) {
						return target.substring(1);
					}
					return "xl/" + target;
				}
			}
		}
		return "xl/worksheets/sheet1.xml";
	}

	/**
	 * セル番号（B3 など）の列を 0始まりの番号にする。読めないときは fallback
	 */
	private static int columnIndex(String ref, int fallback) {
		if (ref == null || ref.isEmpty()) {
			return fallback;
		}
		int n = 0;
		int i = 0;
		while (i < ref.length() && Character.isLetter(ref.charAt(i))) {
			n = n * 26 + (Character.toUpperCase(ref.charAt(i)) - 'A' + 1);
			i++;
		}
		return i == 0 ? fallback : n - 1;
	}

	private static int parseIntOr(String s, int fallback) {
		try {
			return Integer.parseInt(s.trim());
		} catch (Exception e) {
			return fallback;
		}
	}

	/**
	 * 要素の中の文字（t タグ）をすべてつなげる（ふりがな rPh は除く）
	 */
	private static String allText(Element element) {
		StringBuilder sb = new StringBuilder();
		collectText(element, sb);
		return sb.toString();
	}

	private static void collectText(Node node, StringBuilder sb) {
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			String name = child.getLocalName();
			if ("rPh".equals(name) || "phoneticPr".equals(name)) {
				continue;
			}
			if ("t".equals(name)) {
				sb.append(child.getTextContent());
			} else {
				collectText(child, sb);
			}
		}
	}

	private static byte[] readLimited(InputStream in) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[8192];
		int n;
		int total = 0;
		while ((n = in.read(buffer)) > 0) {
			total += n;
			if (total > MAX_ENTRY_BYTES) {
				throw new Exception("xlsx ファイルが大きすぎます");
			}
			out.write(buffer, 0, n);
		}
		return out.toByteArray();
	}

	/**
	 * 外部の定義を読まない設定で XML を読む（XXE の対策）
	 */
	private static Document parseXml(byte[] data) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		factory.setXIncludeAware(false);
		factory.setExpandEntityReferences(false);
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(new ByteArrayInputStream(data));
	}
}
