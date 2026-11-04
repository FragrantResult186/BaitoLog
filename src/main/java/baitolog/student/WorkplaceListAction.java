package baitolog.student;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import bean.ShiftTemplate;
import bean.User;
import bean.Workplace;
import dao.ShiftTemplateDao;
import dao.WorkplaceDao;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import tool.Action;

/**
 * 勤務先一覧・登録画面のアクション（学生用）
 * GET ：勤務先の一覧を表示する（id を付けると、その勤務先の編集欄を開く。new を付けると、空の入力欄を開く）
 * POST：action=save なら保存、action=delete なら削除する
 */
public class WorkplaceListAction extends Action {

	/** 表示色の候補（色コード、色の名前） */
	private static final String[][] COLORS = {
			{ "#E53935", "赤" }, { "#1E88E5", "青" }, { "#43A047", "緑" }, { "#FB8C00", "オレンジ" },
			{ "#8E24AA", "紫" }, { "#00ACC1", "水色" }, { "#D81B60", "ピンク" }, { "#6D4C41", "茶" } };

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse res) throws Exception {

		//ローカル変数の宣言 1
		String root = req.getContextPath() + "/baitolog";
		String errorMessage = "";
		boolean isNew = false;
		Workplace selected = null;
		List<ShiftTemplate> templates = new ArrayList<>();
		WorkplaceDao workplaceDao = new WorkplaceDao();
		ShiftTemplateDao templateDao = new ShiftTemplateDao();

		//リクエストパラメータ―の取得 2
		HttpSession session = req.getSession(false);
		User user = null;
		if (session != null) {
			user = (User) session.getAttribute("user");
		}
		if (user == null || !user.isAuthenticated() || !user.isStudent()) {
			// 学生としてログインしていない場合は、ログイン画面へ
			res.sendRedirect(root + "/Login.action");
			return;
		}
		int studentId = user.getId();
		boolean isPost = "POST".equals(req.getMethod());
		String action = req.getParameter("action");
		int id = toInt(req.getParameter("id"));

		//DBからデータ取得 3
		List<Workplace> workplaces = workplaceDao.findByStudent(studentId);

		//ビジネスロジック 4
		boolean isSaved = false;
		boolean isDeleted = false;
		if (isPost && "save".equals(action)) {
			// ---- 保存（イベント4）----
			selected = new Workplace();
			selected.setId(id);
			selected.setStudentId(studentId);
			selected.setName(trim(req.getParameter("workplace_name")));
			selected.setBranchName(trim(req.getParameter("branch_name")));
			selected.setColor(trim(req.getParameter("color")));
			isNew = (id == 0);
			errorMessage = readTemplates(req, templates);
			if (errorMessage.isEmpty()) {
				if (selected.getName().isEmpty()) {
					errorMessage = "必須項目の記入漏れ";
				} else if (selected.getName().length() > 50 || selected.getBranchName().length() > 50) {
					errorMessage = "勤務先名・店舗名は50文字以内で入力してください";
				}
			}
			if (errorMessage.isEmpty() && id != 0 && workplaceDao.findById(id, studentId) == null) {
				// 他の学生の勤務先、または存在しない勤務先
				res.sendRedirect(root + "/student/WorkplaceList.action");
				return;
			}
			if (errorMessage.isEmpty()) {
				// 表示色が「自動割当」（または候補にない値）のときは、使っていない色を割り当てる
				selected.setColor(decideColor(selected.getColor(), workplaces, id));
				//DBへデータ保存 5
				int savedId = workplaceDao.save(selected, templates);
				res.sendRedirect(root + "/student/WorkplaceList.action?id=" + savedId + "&saved=1");
				return;
			}
		} else if (isPost && "delete".equals(action)) {
			// ---- 削除（イベント5）----
			selected = workplaceDao.findById(id, studentId);
			if (selected == null) {
				res.sendRedirect(root + "/student/WorkplaceList.action");
				return;
			}
			if (workplaceDao.hasShift(id)) {
				errorMessage = "シフトが登録済みの勤務先は削除できません";
				templates = templateDao.findByWorkplace(id);
			} else {
				//DBへデータ保存 5
				workplaceDao.delete(id, studentId);
				res.sendRedirect(root + "/student/WorkplaceList.action?deleted=1");
				return;
			}
		} else if (req.getParameter("new") != null) {
			// ---- 「＋ 追加」（イベント2）：空の入力欄を開く ----
			isNew = true;
			selected = new Workplace();
		} else if (id != 0) {
			// ---- 一覧の行を選ぶ（イベント1）：その勤務先の編集欄を開く ----
			selected = workplaceDao.findById(id, studentId);
			if (selected != null) {
				templates = templateDao.findByWorkplace(id);
			}
		}

		//レスポンス値をセット 6
		req.setAttribute("workplaces", workplaces);
		req.setAttribute("selected", selected);
		req.setAttribute("isNew", isNew);
		req.setAttribute("templates", templates);
		req.setAttribute("colors", COLORS);
		req.setAttribute("errorMessage", errorMessage);
		req.setAttribute("isSaved", "1".equals(req.getParameter("saved")));
		req.setAttribute("isDeleted", "1".equals(req.getParameter("deleted")));

		//JSPへフォワード 7
		req.getRequestDispatcher("workplace_list.jsp").forward(req, res);
	}

	/**
	 * テンプレートの入力欄（template_name / tpl_start / tpl_end / tpl_break の同じ順番）を読み取る
	 * すべて空の行は無視する。入力に問題があればエラーメッセージを返す（問題なければ空文字）
	 */
	private String readTemplates(HttpServletRequest req, List<ShiftTemplate> templates) {
		String[] names = req.getParameterValues("template_name");
		String[] starts = req.getParameterValues("tpl_start");
		String[] ends = req.getParameterValues("tpl_end");
		String[] breaks = req.getParameterValues("tpl_break");
		String errorMessage = "";
		if (names == null || starts == null || ends == null || breaks == null) {
			return errorMessage;
		}
		int count = Math.min(Math.min(names.length, starts.length), Math.min(ends.length, breaks.length));
		for (int i = 0; i < count; i++) {
			String name = trim(names[i]);
			String start = trim(starts[i]);
			String end = trim(ends[i]);
			String breakText = trim(breaks[i]);
			if (name.isEmpty() && start.isEmpty() && end.isEmpty() && (breakText.isEmpty() || "0".equals(breakText))) {
				continue;
			}
			ShiftTemplate template = new ShiftTemplate();
			template.setName(name);
			template.setStartTime(start);
			template.setEndTime(end);
			template.setBreakMin(Math.max(toInt(breakText), 0));
			templates.add(template);

			if (errorMessage.isEmpty()) {
				if (name.isEmpty() || start.isEmpty() || end.isEmpty()) {
					errorMessage = "必須項目の記入漏れ";
				} else if (name.length() > 20) {
					errorMessage = "テンプレート名は20文字以内で入力してください";
				} else if (!isTime(start) || !isTime(end)) {
					errorMessage = "開始時刻・終了時刻は「時:分」で入力してください";
				} else if (!breakText.isEmpty() && !breakText.matches("\\d{1,3}")) {
					errorMessage = "休憩時間は0以上の数字（分）で入力してください";
				}
			}
		}
		return errorMessage;
	}

	/** 使っていない色を割り当てる（候補にない値・空のときだけ） */
	private String decideColor(String color, List<Workplace> workplaces, int editingId) {
		for (String[] c : COLORS) {
			if (c[0].equals(color)) {
				return color;
			}
		}
		List<String> used = new ArrayList<>();
		for (Workplace w : workplaces) {
			if (w.getId() != editingId) {
				used.add(w.getColor());
			}
		}
		for (String[] c : COLORS) {
			if (!used.contains(c[0])) {
				return c[0];
			}
		}
		return COLORS[used.size() % COLORS.length][0];
	}

	private boolean isTime(String text) {
		try {
			LocalTime.parse(text);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private int toInt(String text) {
		try {
			return Integer.parseInt(text == null ? "" : text.trim());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private String trim(String text) {
		return text == null ? "" : text.trim();
	}
}
