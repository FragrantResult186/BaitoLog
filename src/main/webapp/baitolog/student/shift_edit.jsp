<%-- シフト登録・変更画面（学生） --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions"%>

<c:import url="/common/base.jsp">
<c:param name="title">シフト登録 | BaitoLog</c:param>
<c:param name="scripts">
<script>
// テンプレートデータ（勤務先ID → テンプレート配列）
const templatesByWorkplace = {
<c:forEach var="entry" items="${templatesByWorkplace}" varStatus="st">
	"${entry.key}": [
		<c:forEach var="t" items="${entry.value}" varStatus="tst">
		{ id: ${t.id}, name: "${fn:escapeXml(t.name)}", start: "${t.startTime}", end: "${t.endTime}", breakMin: ${t.breakMin} }<c:if test="${!tst.last}">,</c:if>
		</c:forEach>
	]<c:if test="${!st.last}">,</c:if>
</c:forEach>
};

// シフトデータ（日付 → シフト配列）。登録済みの日をクリックしたとき、フォームに読み込む
const shiftsByDate = {};
<c:forEach var="s" items="${shifts}">
(shiftsByDate["${s.workDate}"] = shiftsByDate["${s.workDate}"] || []).push({
	id: ${s.id}, workplaceId: ${s.workplaceId},
	start: "${s.startTime}", end: "${s.endTime}",
	breakStart: "${s.breakStart != null ? s.breakStart : ''}", breakEnd: "${s.breakEnd != null ? s.breakEnd : ''}"
});
</c:forEach>

function hhmm(v) { return v ? v.substring(0, 5) : ""; }

// フォームに1件のシフトを読み込む（null なら新規入力用に空にする）
function fillForm(shift) {
	document.getElementById("shift_id").value = shift ? shift.id : 0;
	if (shift) {
		document.getElementById("workplace_id").value = shift.workplaceId;
	}
	document.getElementById("start_time").value = shift ? hhmm(shift.start) : "";
	document.getElementById("end_time").value = shift ? hhmm(shift.end) : "";
	document.getElementById("break_start").value = shift ? hhmm(shift.breakStart) : "";
	document.getElementById("break_end").value = shift ? hhmm(shift.breakEnd) : "";
	const del = document.getElementById("delete-btn");
	if (del) del.style.display = shift ? "" : "none";
	onWorkplaceChange();
	calcHours();
	checkLimit();
}

// その日に登録済みシフトがあれば、選択ボタンを表示する（同じ日に追加もできる）
function renderPicker(dateStr) {
	const picker = document.getElementById("shift-picker");
	picker.innerHTML = "";
	const list = shiftsByDate[dateStr] || [];
	if (list.length === 0) { picker.style.display = "none"; return; }
	picker.style.display = "";
	const current = document.getElementById("shift_id").value;
	list.forEach(sh => {
		const btn = document.createElement("button");
		btn.type = "button";
		btn.className = "btn btn-sm me-1 mb-1 " + (String(sh.id) === String(current) ? "btn-primary" : "btn-outline-primary");
		btn.textContent = hhmm(sh.start) + "～" + hhmm(sh.end);
		btn.onclick = () => { fillForm(sh); renderPicker(dateStr); };
		picker.appendChild(btn);
	});
	const add = document.createElement("button");
	add.type = "button";
	add.className = "btn btn-sm btn-outline-secondary mb-1";
	add.textContent = "＋ この日に追加";
	add.onclick = () => { fillForm(null); renderPicker(dateStr); };
	picker.appendChild(add);
}

// カレンダーの日付クリックでフォームにセット（登録済みなら内容を読み込む）
function selectDate(dateStr) {
	document.getElementById("work_date").value = dateStr;
	document.getElementById("form-date-label").textContent = formatDateLabel(dateStr);
	// 選択ハイライトを更新
	document.querySelectorAll(".calendar-day").forEach(td => {
		td.classList.toggle("selected-day", td.dataset.date === dateStr);
	});
	const list = shiftsByDate[dateStr] || [];
	fillForm(list.length > 0 ? list[0] : null);
	renderPicker(dateStr);
}
function formatDateLabel(dateStr) {
	const d = new Date(dateStr + "T00:00:00");
	const week = ["日","月","火","水","木","金","土"];
	return (d.getMonth()+1) + "月" + d.getDate() + "日（" + week[d.getDay()] + "）のシフト";
}

// 勤務先変更 → テンプレート一覧を更新
function onWorkplaceChange() {
	const wpId = document.getElementById("workplace_id").value;
	const box = document.getElementById("template-buttons");
	box.innerHTML = "";
	const list = templatesByWorkplace[wpId] || [];
	list.forEach(t => {
		const btn = document.createElement("button");
		btn.type = "button";
		btn.className = "btn btn-outline-secondary btn-sm me-1 mb-1";
		btn.textContent = t.name;
		btn.onclick = () => applyTemplate(t);
		box.appendChild(btn);
	});
}

// テンプレート適用
function applyTemplate(t) {
	document.getElementById("start_time").value = t.start.substring(0,5);
	document.getElementById("end_time").value = t.end.substring(0,5);
	if (t.breakMin > 0) {
		// 開始から休憩開始までのオフセットは簡易に中央寄りにする
		const start = toMinutes(t.start);
		const end = toMinutes(t.end);
		const mid = start + Math.floor((end - start - t.breakMin) / 2);
		document.getElementById("break_start").value = toTimeStr(mid);
		document.getElementById("break_end").value = toTimeStr(mid + t.breakMin);
	} else {
		document.getElementById("break_start").value = "";
		document.getElementById("break_end").value = "";
	}
	calcHours();
	checkLimit();
}

function toMinutes(hhmm) {
	const p = hhmm.split(":");
	return parseInt(p[0]) * 60 + parseInt(p[1]);
}
function toTimeStr(min) {
	const h = Math.floor(min / 60) % 24;
	const m = min % 60;
	return String(h).padStart(2,"0") + ":" + String(m).padStart(2,"0");
}

// 労働時間計算
function calcHours() {
	const s = document.getElementById("start_time").value;
	const e = document.getElementById("end_time").value;
	const bs = document.getElementById("break_start").value;
	const be = document.getElementById("break_end").value;
	if (!s || !e) {
		document.getElementById("work-hours").textContent = "-";
		return;
	}
	let mins = toMinutes(e) - toMinutes(s);
	if (mins < 0) mins += 24*60;
	if (bs && be) {
		let bm = toMinutes(be) - toMinutes(bs);
		if (bm < 0) bm += 24*60;
		mins -= bm;
	}
	const hours = Math.max(0, mins) / 60;
	document.getElementById("work-hours").textContent = hours.toFixed(2) + "時間";
}

// 7日間判定（ShiftCheck.action へ fetch）
function checkLimit() {
	const date = document.getElementById("work_date").value;
	const start = document.getElementById("start_time").value;
	const end = document.getElementById("end_time").value;
	const breakStart = document.getElementById("break_start").value;
	const breakEnd = document.getElementById("break_end").value;
	const id = document.getElementById("shift_id").value || "0";
	const msg = document.getElementById("limit-message");
	const saveBtn = document.getElementById("save-btn");

	if (!date || !start || !end) {
		msg.textContent = "";
		msg.className = "";
		saveBtn.disabled = false;
		return;
	}

	const params = new URLSearchParams({
		date: date, start: start, end: end,
		breakStart: breakStart, breakEnd: breakEnd, id: id
	});

	fetch("${pageContext.request.contextPath}/baitolog/student/ShiftCheck.action?" + params)
		.then(r => r.json())
		.then(data => {
			if (!data.ok) {
				msg.textContent = data.message || "判定できませんでした";
				msg.className = "text-danger";
				saveBtn.disabled = true;
				return;
			}
			msg.textContent = data.message;
			if (data.over) {
				msg.className = "text-danger fw-bold";
				saveBtn.disabled = true;
			} else {
				msg.className = "text-success";
				saveBtn.disabled = false;
			}
		})
		.catch(() => {
			msg.textContent = "判定通信に失敗しました";
			msg.className = "text-warning";
		});
}

document.addEventListener("DOMContentLoaded", () => {
	onWorkplaceChange();
	calcHours();
	const dateInput = document.getElementById("work_date");
	if (dateInput.value) {
		document.getElementById("form-date-label").textContent = formatDateLabel(dateInput.value);
		renderPicker(dateInput.value);
	}
	["start_time","end_time","break_start","break_end"].forEach(id => {
		document.getElementById(id).addEventListener("change", () => { calcHours(); checkLimit(); });
	});
	document.getElementById("workplace_id").addEventListener("change", () => { onWorkplaceChange(); checkLimit(); });
	dateInput.addEventListener("change", () => {
		document.getElementById("form-date-label").textContent = formatDateLabel(dateInput.value);
		checkLimit();
	});
});




</script>
	</c:param>

	<c:param name="content">
		<section>
			<%-- メッセージ --%>
			<c:if test="${not empty message}">
				<div class="alert alert-success">${message}</div>
			</c:if>
			<c:if test="${not empty errorMessage}">
				<div class="alert alert-danger">${errorMessage}</div>
			</c:if>

			<div class="d-flex justify-content-between align-items-center mb-3">
				<h4 class="mb-0">${monthLabel}</h4>
				<div>
					<a href="ShiftEdit.action?month=${prevMonth}"
						class="btn btn-outline-secondary btn-sm">← 前月</a> <a
						href="ShiftEdit.action?month=${nextMonth}"
						class="btn btn-outline-secondary btn-sm">翌月 →</a>
				</div>
			</div>

			<div class="row">
				<%-- 左：カレンダー --%>
				<div class="col-md-7 mb-4">
					<table class="table table-bordered text-center calendar-table">
						<thead>
							<tr>
								<th>月</th>
								<th>火</th>
								<th>水</th>
								<th>木</th>
								<th>金</th>
								<th>土</th>
								<th>日</th>
							</tr>
						</thead>
						<tbody>
							<c:set var="firstDay" value="${yearMonth.atDay(1)}" />
							<c:set var="daysInMonth" value="${yearMonth.lengthOfMonth()}" />
							<c:set var="startOffset"
								value="${(firstDay.dayOfWeek.value + 6) % 7}" />
							<c:set var="selectedDate"
								value="${editShift != null ? editShift.workDate : null}" />
							<tr>
								<c:forEach begin="1" end="${startOffset}">
									<td class="text-muted bg-light"></td>
								</c:forEach>

								<c:forEach begin="1" end="${daysInMonth}" var="day">
									<%-- yyyy-MM-dd を確実に作る --%>
									<c:set var="cellDate" value="${yearMonth.atDay(day)}" />
									<c:set var="dateStr" value="${cellDate}" />

									<%-- その日のシフト色を集める --%>
									<c:set var="dayColors" value="" />
									<c:forEach var="s" items="${shifts}">
										<c:if test="${s.workDate.equals(cellDate)}">
											<c:set var="wp" value="${workplaceMap[s.workplaceId]}" />
											<c:set var="c"
												value="${wp != null && not empty wp.color ? wp.color : '#6c757d'}" />
											<c:set var="dayColors"
												value="${dayColors}${empty dayColors ? '' : ','}${c}" />
										</c:if>
									</c:forEach>

									<c:set var="isSelected"
										value="${selectedDate != null && selectedDate.equals(cellDate)}" />

									<td class="calendar-day ${isSelected ? 'selected-day' : ''}"
										data-date="${dateStr}" onclick="selectDate('${dateStr}')"
										style="cursor: pointer; vertical-align: top; height: 4.5rem;">
										<div class="fw-bold day-number">${day}</div> <c:if
											test="${not empty dayColors}">
											<div class="shift-bars">
												<c:forTokens items="${dayColors}" delims="," var="col">
													<div class="shift-bar" style="background-color:${col};"></div>
												</c:forTokens>
											</div>
										</c:if>
									</td>

									<c:if
										test="${(startOffset + day) % 7 == 0 && day < daysInMonth}">
							</tr>
							<tr>
								</c:if>
								</c:forEach>

								<c:set var="remain"
									value="${(7 - (startOffset + daysInMonth) % 7) % 7}" />
								<c:forEach begin="1" end="${remain}">
									<td class="text-muted bg-light"></td>
								</c:forEach>
							</tr>
						</tbody>
					</table>
				</div>

				<%-- 右：入力フォーム --%>
				<div class="col-md-5">
					<div class="card">
						<div class="card-header">
							<span id="form-date-label"> <c:choose>
									<c:when test="${not empty editShift}">
										${editShift.workDate.monthValue}月${editShift.workDate.dayOfMonth}日のシフト
									</c:when>
									<c:otherwise>日付を選んでください</c:otherwise>
								</c:choose>
							</span>
						</div>
						<div class="card-body">
							<div id="shift-picker" class="mb-3" style="display: none;"></div>
							<form method="post" action="ShiftEditExecute.action"
								id="shift-form">
								<input type="hidden" name="id" id="shift_id"
									value="${editShift != null ? editShift.id : 0}" /> <input
									type="hidden" name="month" value="${monthParam}" />

								<div class="mb-3">
									<label class="form-label">勤務先</label> <select
										name="workplace_id" id="workplace_id" class="form-select"
										required>
										<option value="">選択してください</option>
										<c:forEach var="w" items="${workplaces}">
											<option value="${w.id}"
												${editShift != null && editShift.workplaceId == w.id ? 'selected' : ''}>
												${w.name}
												<c:if test="${not empty w.branchName}"> ${w.branchName}</c:if>
											</option>
										</c:forEach>
									</select>
								</div>

								<div class="mb-2">
									<label class="form-label">テンプレート</label>
									<div id="template-buttons"></div>
								</div>

								<div class="mb-3">
									<label class="form-label">勤務日</label> <input type="date"
										name="work_date" id="work_date" class="form-control" required
										value="${editShift != null ? editShift.workDate : ''}" />
								</div>

								<div class="row mb-3">
									<div class="col">
										<label class="form-label">開始</label> <input type="time"
											name="start_time" id="start_time" class="form-control"
											required
											value="${editShift != null ? editShift.startTime : ''}" />
									</div>
									<div class="col">
										<label class="form-label">終了</label> <input type="time"
											name="end_time" id="end_time" class="form-control" required
											value="${editShift != null ? editShift.endTime : ''}" />
									</div>
								</div>

								<div class="row mb-3">

									<div class="col">
										<label class="form-label">休憩開始</label> <input type="time"
											name="break_start" id="break_start" class="form-control"
											value="${editShift != null && editShift.breakStart != null ? editShift.breakStart : ''}" />
									</div>
									<div class="col">
										<label class="form-label">休憩終了</label> <input type="time"
											name="break_end" id="break_end" class="form-control"
											value="${editShift != null && editShift.breakEnd != null ? editShift.breakEnd : ''}" />
									</div>
								</div>

								<p class="mb-2">
									労働時間：<strong id="work-hours">-</strong>
								</p>
								<p id="limit-message" class="mb-3"></p>

								<div class="d-flex gap-2">
									<button type="submit" id="save-btn" class="btn btn-primary">保存</button>
									<button type="submit" id="delete-btn" formaction="ShiftDeleteExecute.action" formnovalidate
										class="btn btn-outline-danger"
										style="${editShift != null && editShift.id > 0 ? '' : 'display:none;'}"
										onclick="return confirm('このシフトを削除しますか？');">削除</button>
								</div>
							</form>
						</div>
					</div>
				</div>
			</div>
		</section>
	</c:param>
</c:import>