<%-- 学生別記録画面（教職員） --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<c:set var="wd" value="${['月','火','水','木','金','土','日']}" />
<c:set var="statusClass" value="${window.status == 'OVER' ? 'text-danger' : (window.status == 'CAUTION' ? 'text-warning' : 'text-success')}" />
<c:set var="baseQuery" value="id=${student.id}&target_month=${targetMonth}&class_id=${classId}" />
<c:import url="/common/base.jsp">
	<c:param name="title">
		BaitoLog　学生別記録
	</c:param>

	<c:param name="scripts">
		<style>
			.day-chart { display: flex; align-items: flex-end; gap: 2px; height: 150px; }
			.day-col { flex: 1; display: flex; flex-direction: column; justify-content: flex-end; height: 100%;
				text-decoration: none; color: inherit; border-radius: 3px; }
			.day-col:hover { background: #e9ecef; }
			.day-col.in-window { background: #eef2f7; }
			.day-bar { width: 100%; min-height: 1px; background: #8fa3bd; border-radius: 2px 2px 0 0; }
			.day-col.in-window .day-bar { background: #0d6efd; }
			.day-col.in-window.chart-over .day-bar { background: #dc3545; }
			.day-col.in-window.chart-caution .day-bar { background: #f0ad00; }
			.day-label { font-size: 0.65rem; text-align: center; line-height: 1.6; }
			.day-label.sat { color: #0d6efd; }
			.day-label.sun { color: #dc3545; }
		</style>
		<script>
			// 「リセットする」を押したら、二重に押せないようにする
			document.addEventListener("DOMContentLoaded", function() {
				var form = document.getElementById("resetExecuteForm");
				if (form) {
					form.addEventListener("submit", function() {
						form.querySelector("button[type=submit]").disabled = true;
					});
				}
			});
		</script>
	</c:param>

	<c:param name="content">
		<section class="me-4">
			<h2 class="h4 mb-3 py-3 px-4 text-white" style="background:#0f2540;">
				<c:out value="${student.name}" /> ｜ <c:out value="${student.schoolClass.name}" default="（未所属）" /> ｜ 勤務先 ${workplaceCount} 社
			</h2>

			<c:if test="${not empty resetMessage}">
				<div class="alert alert-success mx-4" role="alert"><c:out value="${resetMessage}" /></div>
			</c:if>
			<c:if test="${not empty errorMessage}">
				<div class="alert alert-danger mx-4" role="alert"><c:out value="${errorMessage}" /></div>
			</c:if>

			<%-- 対象期間（選ぶとすぐに作り直す） --%>
			<form method="get" action="StudentDetail.action" class="d-flex align-items-center gap-3 px-4 mb-3">
				<input type="hidden" name="id" value="${student.id}">
				<input type="hidden" name="class_id" value="<c:out value='${classId}' />">
				<label for="target_month" class="mb-0">対象期間</label>
				<select class="form-select w-auto" id="target_month" name="target_month" onchange="this.form.submit()">
					<c:forEach var="m" items="${months}">
						<option value="${m}" ${targetMonth == m.toString() ? 'selected' : ''}>${m.year} 年 ${m.monthValue} 月</option>
					</c:forEach>
				</select>
				<noscript><button type="submit" class="btn btn-primary">表示</button></noscript>
			</form>

			<%-- 7日間の合計と判定 --%>
			<div class="d-flex flex-wrap align-items-baseline gap-4 px-4 mb-3">
				<span class="small">${windowStart.monthValue}/${windowStart.dayOfMonth} ～ ${windowEnd.monthValue}/${windowEnd.dayOfMonth}</span>
				<span class="${statusClass}"><span class="display-6"><fmt:formatNumber value="${window.totalHours}" minFractionDigits="1" maxFractionDigits="2" /></span> <span class="fs-4 text-dark">時間</span></span>
				<span class="${statusClass} fw-bold">
					<c:choose>
						<c:when test="${window.status == 'OVER'}">✖ 上限を <fmt:formatNumber value="${window.overHours}" minFractionDigits="1" maxFractionDigits="2" /> 時間超えています</c:when>
						<c:when test="${window.status == 'CAUTION'}">▲ 上限まで <fmt:formatNumber value="${window.remainingHours}" minFractionDigits="1" maxFractionDigits="2" /> 時間です</c:when>
						<c:otherwise>✔ 上限内です</c:otherwise>
					</c:choose>
				</span>
				<span class="small text-muted">（上限 <fmt:formatNumber value="${window.limitHours}" maxFractionDigits="1" /> 時間）</span>
			</div>

			<%-- 日別グラフ（日付を押すと、その日を真ん中にした7日間へ移る） --%>
			<div class="px-4 mb-4">
				<div class="small text-muted mb-1">日別の労働時間（日付を押すと、その日を真ん中にした7日間を見られます）</div>
				<div class="day-chart">
					<c:forEach var="d" items="${monthDays}">
						<c:set var="inWindow" value="${!d.date.isBefore(windowStart) && !d.date.isAfter(windowEnd)}" />
						<a class="day-col ${inWindow ? 'in-window' : ''} ${inWindow && window.status == 'OVER' ? 'chart-over' : ''} ${inWindow && window.status == 'CAUTION' ? 'chart-caution' : ''}"
							href="StudentDetail.action?${baseQuery}&center=${d.date}"
							title="${d.date.monthValue}/${d.date.dayOfMonth}（${wd[d.date.dayOfWeek.value - 1]}）<fmt:formatNumber value='${d.hours}' minFractionDigits='1' maxFractionDigits='2' />時間">
							<div class="day-bar" style="height:${d.hours / chartMax * 100}%;"></div>
							<div class="day-label ${d.date.dayOfWeek.value == 6 ? 'sat' : (d.date.dayOfWeek.value == 7 ? 'sun' : '')}">${d.date.dayOfMonth}</div>
						</a>
					</c:forEach>
				</div>
			</div>

			<%-- 日別勤務記録（見ている7日間） --%>
			<div class="px-4">
				<table class="table table-sm align-middle">
					<thead>
						<tr>
							<th>日付</th>
							<th>勤務先</th>
							<th>勤務時間</th>
							<th>休憩</th>
							<th class="text-end">労働時間</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="d" items="${windowDays}">
							<c:choose>
								<c:when test="${empty d.shifts}">
									<tr class="text-muted">
										<td>${d.date.monthValue}/${d.date.dayOfMonth} ${wd[d.date.dayOfWeek.value - 1]}</td>
										<td>ー</td><td>ー</td><td>ー</td><td class="text-end">ー</td>
									</tr>
								</c:when>
								<c:otherwise>
									<c:forEach var="shift" items="${d.shifts}" varStatus="st">
										<tr>
											<td>
												<c:if test="${st.first}">${d.date.monthValue}/${d.date.dayOfMonth} ${wd[d.date.dayOfWeek.value - 1]}</c:if>
											</td>
											<td><c:out value="${workplaceMap[shift.workplaceId].displayName}" /></td>
											<td>${shift.startTime} ～ ${shift.endTime}</td>
											<td>
												<c:choose>
													<c:when test="${shift.hasBreak()}">${shift.breakStart} ～ ${shift.breakEnd}</c:when>
													<c:otherwise>ー</c:otherwise>
												</c:choose>
											</td>
											<td class="text-end"><fmt:formatNumber value="${shift.workHours}" minFractionDigits="2" maxFractionDigits="2" /></td>
										</tr>
									</c:forEach>
								</c:otherwise>
							</c:choose>
						</c:forEach>
					</tbody>
					<tfoot>
						<tr class="fw-bold">
							<td colspan="4" class="text-end">合計</td>
							<td class="text-end"><fmt:formatNumber value="${window.totalHours}" minFractionDigits="2" maxFractionDigits="2" /></td>
						</tr>
					</tfoot>
				</table>
			</div>

			<div class="d-flex gap-2 px-4 mb-4">
				<a href="StudentDetail.action?${baseQuery}&center=${center}&csv=1" class="btn btn-outline-secondary">CSV出力</a>
			</div>

			<%-- パスワードのリセット（学生がパスワードを忘れたとき。一番下に置く） --%>
			<hr class="mx-4">
			<div id="reset" class="px-4 mb-5">
				<h3 class="h5">パスワードのリセット</h3>
				<p class="small text-muted mb-2">
					パスワードを<strong>生年月日（例：20061004）</strong>に戻します。
					勤務先・シフトテンプレート・シフトなどのデータは消えません。学生は、生年月日でログインしたあと、新しいパスワードを登録し直します。
				</p>
				<c:choose>
					<c:when test="${!canReset}">
						<p class="text-danger mb-0">この学生は生年月日が登録されていないため、リセットできません。</p>
					</c:when>
					<c:when test="${resetStep == 'confirm'}">
						<div class="alert alert-warning" role="alert">
							<strong><c:out value="${student.name}" />（<c:out value="${student.loginId}" />）</strong>のパスワードを、生年月日に戻します。よろしいですか？
						</div>
						<div class="d-flex gap-2">
							<form method="post" action="StudentDetail.action#reset">
								<input type="hidden" name="id" value="${student.id}">
								<input type="hidden" name="target_month" value="${targetMonth}">
								<input type="hidden" name="class_id" value="<c:out value='${classId}' />">
								<input type="hidden" name="center" value="${center}">
								<button type="submit" class="btn btn-outline-secondary">やめる</button>
							</form>
							<form id="resetExecuteForm" method="post" action="StudentDetail.action">
								<input type="hidden" name="id" value="${student.id}">
								<input type="hidden" name="target_month" value="${targetMonth}">
								<input type="hidden" name="class_id" value="<c:out value='${classId}' />">
								<input type="hidden" name="center" value="${center}">
								<input type="hidden" name="reset_step" value="execute">
								<input type="hidden" name="token" value="<c:out value='${token}' />">
								<button type="submit" class="btn btn-danger">リセットする</button>
							</form>
						</div>
					</c:when>
					<c:otherwise>
						<form method="post" action="StudentDetail.action#reset">
							<input type="hidden" name="id" value="${student.id}">
							<input type="hidden" name="target_month" value="${targetMonth}">
							<input type="hidden" name="class_id" value="<c:out value='${classId}' />">
							<input type="hidden" name="center" value="${center}">
							<input type="hidden" name="reset_step" value="confirm">
							<button type="submit" class="btn btn-outline-danger">パスワードをリセット</button>
						</form>
					</c:otherwise>
				</c:choose>
			</div>
		</section>
	</c:param>
</c:import>
