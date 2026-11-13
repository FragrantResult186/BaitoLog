<%-- 提出済みの報告書（学生・読み取り専用） --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions"%>

<c:set var="maxStart" value="${report.max7dayStart}" />
<c:if test="${not empty maxStart}">
	<c:set var="maxEnd" value="${maxStart.plusDays(6)}" />
</c:if>

<c:import url="/common/base.jsp">
	<c:param name="title">
		アルバイト時間管理報告シート | BaitoLog
	</c:param>

	<c:param name="scripts">
		<style>
			.report-page { width: 100%; margin: 0; color: #111; }
			.report-sheet { width: 100%; margin: 0; padding: 0.35rem 0 1.25rem; background: #fff; }
			.report-print-control { display: flex; align-items: center; gap: 0.4rem; margin-bottom: 0.45rem; font-size: 0.72rem; }
			.report-print-button { border: 1px solid #0b5ed7; border-radius: 0.15rem; padding: 0.05rem 0.4rem; background: #0d6efd; color: #fff; font-size: 0.72rem; }
			.report-title { margin: 0 0 0.35rem; padding-bottom: 0.15rem; border-bottom: 2px solid #111; font-size: 1.2rem; font-weight: 700; }
			.report-meta { display: grid; grid-template-columns: 1.2fr 1fr 1.35fr; gap: 0.6rem; margin-bottom: 0.35rem; font-size: 0.75rem; }
			.report-table { width: 100%; border-collapse: collapse; table-layout: fixed; font-size: 0.7rem; }
			.report-table th, .report-table td { border: 1px solid #777; padding: 0.12rem 0.25rem; line-height: 1.25; vertical-align: middle; }
			.report-table thead th { background: #eeeae2; font-weight: 700; white-space: nowrap; }
			.report-table th:nth-child(1) { width: 12%; }
			.report-table th:nth-child(2) { width: 8%; }
			.report-table th:nth-child(3) { width: 23%; }
			.report-table th:nth-child(4) { width: 21%; }
			.report-table th:nth-child(5) { width: 22%; }
			.report-table th:nth-child(6) { width: 14%; }
			.report-table td:nth-child(1), .report-table td:nth-child(2), .report-table td:nth-child(4), .report-table td:nth-child(5), .report-table td:nth-child(6) { white-space: nowrap; }
			.report-table td:nth-child(6), .report-summary-value { text-align: right; }
			.report-table tfoot th, .report-table tfoot td { background: #eeeae2; font-weight: 700; }
			.report-table tfoot th { text-align: right; }
			.report-empty { padding: 0.8rem !important; color: #666; text-align: center; }
			.report-toggle { margin: 0.3rem 0 0.7rem; padding: 0; border: 0; background: transparent; color: #111; font-size: 0.72rem; text-decoration: underline; cursor: pointer; }
			.report-notes-label { margin: 0.5rem 0 0.2rem; font-size: 0.75rem; }
			.report-declaration-box { min-height: 5.3rem; padding: 0.75rem 0.45rem 0.45rem; border: 1px solid #555; font-size: 0.7rem; }
			.report-declaration { margin: 0 0 0.9rem; }
			.report-signature { display: flex; align-items: end; gap: 1rem; }
			.report-signature-line { flex: 1; display: block; min-width: 7rem; border-bottom: 1px solid #111; height: 1rem; }
			.report-readonly { margin-top: 0.55rem; text-align: center; font-size: 0.78rem; font-weight: 700; }
			@media (max-width: 700px) {
				.report-sheet { padding-right: 0; padding-left: 0; }
				.report-meta { grid-template-columns: 1fr; gap: 0.15rem; }
				.report-table { font-size: 0.62rem; }
			}
			@media print {
				@page { margin: 10mm; }
				body { background: #fff; }
				#wrapper { width: auto; max-width: none; }
				#wrapper > header, #wrapper > nav, #wrapper > footer, .screen-only { display: none !important; }
				.report-page, .report-sheet { max-width: none; margin: 0; padding: 0; }
				.report-table { font-size: 9pt; }
			}
		</style>
		<script>
			document.addEventListener("DOMContentLoaded", function () {
				var toggle = document.getElementById("reportTableToggle");
				var rows = Array.prototype.slice.call(document.querySelectorAll("#report-shift-table tbody tr.report-data-row"));
				var collapsed = false;
				if (toggle && rows.length > 0) {
					toggle.addEventListener("click", function () {
						collapsed = !collapsed;
						rows.forEach(function (row, index) { row.hidden = collapsed && index >= 7; });
						toggle.textContent = collapsed ? "▼ すべて表示" : "▲ 一部を表示";
					});
				}
			});
		</script>
	</c:param>

	<c:param name="content">
		<section class="report-page">
			<div class="report-sheet">
				<div class="report-print-control screen-only">
					<button type="button" class="report-print-button" onclick="window.print();">印刷</button>
					<span>このボタンは印刷時には表示されません</span>
				</div>

				<div class="report-readonly screen-only">提出済み（<c:out value="${submittedAtText}" />）</div>
				<h2 class="report-title">アルバイト時間管理報告シート</h2>
				<div class="report-meta">
					<div>対象年月：<c:out value="${monthLabel}" /></div>
					<div>クラス：<c:out value="${report.student.schoolClass.name}" default="（未所属）" /></div>
					<div>氏名：<c:out value="${report.student.name}" /></div>
				</div>

				<table id="report-shift-table" class="report-table">
					<thead>
						<tr><th>日付</th><th>曜日</th><th>アルバイト先</th><th>勤務時間</th><th>休憩時間</th><th>労働時間</th></tr>
					</thead>
					<tbody>
						<c:forEach var="i" items="${report.items}">
							<tr class="report-data-row">
								<td>${i.workDate.monthValue}月${i.workDate.dayOfMonth}日</td>
								<td>${fn:substring('月火水木金土日', i.workDate.dayOfWeek.value - 1, i.workDate.dayOfWeek.value)}</td>
								<td><c:out value="${i.workplaceName}" /></td>
								<td>${i.startTime} ～ ${i.endTime}</td>
								<td><c:choose><c:when test="${not empty i.breakStart and not empty i.breakEnd}">${i.breakStart} ～ ${i.breakEnd}</c:when><c:otherwise>ー</c:otherwise></c:choose></td>
								<td><fmt:formatNumber value="${i.workingHours}" pattern="0.00" /></td>
							</tr>
						</c:forEach>
						<c:if test="${empty report.items}"><tr><td colspan="6" class="report-empty">提出する情報がありません。</td></tr></c:if>
					</tbody>
					<tfoot>
						<tr><th colspan="5">月の合計労働時間</th><td class="report-summary-value"><fmt:formatNumber value="${report.totalHours}" pattern="0.00" /></td></tr>
						<tr>
							<th colspan="5">連続する7日間の最大<c:if test="${not empty maxStart and not empty maxEnd}">（${maxStart.monthValue}/${maxStart.dayOfMonth}～${maxEnd.monthValue}/${maxEnd.dayOfMonth}）</c:if></th>
							<td class="report-summary-value"><fmt:formatNumber value="${report.max7dayHours}" pattern="0.00" /></td>
						</tr>
					</tfoot>
				</table>

				<c:if test="${fn:length(report.items) > 7}"><button type="button" id="reportTableToggle" class="report-toggle screen-only">▲ 一部を表示</button></c:if>
				<div class="report-notes-label">備考：</div>
				<div class="report-declaration-box">
					<p class="report-declaration">私は学校に報告している<c:out value="${report.workplaceCount}" />社以外にはアルバイトをしていません。</p>
					<div class="report-signature"><span>サイン（自署）</span><span class="report-signature-line" aria-hidden="true"></span></div>
				</div>
			</div>
		</section>
	</c:param>
</c:import>
