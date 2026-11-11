<%-- ダッシュボード（学生）通常／提出後を兼ねる --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions"%>

<%-- ★仮データ：StudentTopAction ができたらこのブロックを消す --%>
<%
	// 7日間の集計（勤務先ごとに足すと、合計も自動で増える）
	bean.WindowResult dummy = new bean.WindowResult();
	dummy.setStartDate(java.time.LocalDate.of(2026, 9, 12));
	dummy.setEndDate(java.time.LocalDate.of(2026, 9, 18));
	dummy.setLimitHours(28);
	dummy.addWorkplaceHours("ローソン 北口店", 14.5);
	dummy.addWorkplaceHours("居酒屋 とり松", 8.0);
	request.setAttribute("result", dummy);

	// 勤務先ID → 表示名
	java.util.Map<Integer, String> names = new java.util.HashMap<>();
	names.put(1, "ローソン 北口店");
	names.put(2, "居酒屋 とり松");
	request.setAttribute("workplaceNames", names);

	// これからのシフト（{日, 勤務先ID, 開始時, 終了時}）
	java.util.List<bean.Shift> shifts = new java.util.ArrayList<>();
	int[][] rows = { {19, 1, 17, 22}, {20, 2, 18, 23}, {22, 1, 17, 22} };
	for (int[] r : rows) {
		bean.Shift s = new bean.Shift();
		s.setId(r[0]);
		s.setWorkplaceId(r[1]);
		s.setWorkDate(java.time.LocalDate.of(2026, 9, r[0]));
		s.setStartTime(java.time.LocalTime.of(r[2], 0));
		s.setEndTime(java.time.LocalTime.of(r[3], 0));
		shifts.add(s);
	}
	request.setAttribute("shifts", shifts);
	
	// 報告書の提出状況（false を true にすると、提出済みの表示を確認できる）
	request.setAttribute("submitted", false);
	request.setAttribute("monthLabel", "9月");
	request.setAttribute("month", "2026-09");
	request.setAttribute("submittedAtText", "10/15 12:23");
	request.setAttribute("message", "9月分の報告書を提出しました");
%>

<c:import url="/common/base.jsp">
	<c:param name="title">
		ダッシュボード | BaitoLog
	</c:param>

	<c:param name="scripts"></c:param>

	<c:param name="content">
		<section>
		
			<%-- 提出直後のmessage --%>
			<c:if test="${ not empty message }">
				<div class="alert alert-success">✓ ${ message }</div>
			</c:if>
			
			<c:if test="${ not empty errorMessage }">
				<div class="alert alert-danger">${ errorMessage }</div>
			</c:if>
			

			<%-- 対象期間 --%>
			<h5 class="mb-3">
				直近7日間　${result.startDate.monthValue}/${result.startDate.dayOfMonth}
				〜 ${result.endDate.monthValue}/${result.endDate.dayOfMonth}
			</h5>

			<%-- 合計と上限 --%>
			<p class="fs-3 mb-1">
				<fmt:formatNumber value="${result.totalHours}" pattern="0.##" /> /
				<fmt:formatNumber value="${result.limitHours}" pattern="0.##" /> 時間
			</p>

			<%-- 進捗バー：状態で色を決める --%>
			<c:choose>
				<c:when test="${result.status == 'OVER'}"><c:set var="barClass" value="bg-danger" /></c:when>
				<c:when test="${result.status == 'CAUTION'}"><c:set var="barClass" value="bg-warning" /></c:when>
				<c:otherwise><c:set var="barClass" value="bg-success" /></c:otherwise>
			</c:choose>
			<div class="progress mb-2" style="height: 1.5rem;">
				<div class="progress-bar ${barClass}" role="progressbar"
					style="width: ${result.percent > 100 ? 100 : result.percent}%"></div>
			</div>

			<%-- 残り時間メッセージ --%>
			<p class="mb-4 text-center border rounded p-2">
				<c:choose> 
					<c:when test="${result.over}">
						上限を <fmt:formatNumber value="${result.overHours}" pattern="0.##" /> 時間 超えています
					</c:when>
					<c:otherwise>
						あと <fmt:formatNumber value="${result.remainingHours}" pattern="0.##" /> 時間 はたらけます
					</c:otherwise>
				</c:choose>
			</p>
			
			<%-- 勤務先別合計（7日間） --%>
			<div>
				<c:choose>
					<c:when test="${ empty result.workplaceHours }">
						<p class="text-muted">この7日間の勤務はありません</p>
					</c:when>
					<c:otherwise>
						<ul　class="list-unstyled mb-0">
							<c:forEach var="w" items="${ resultworkplaceHours }">
								<li>${ w.key }:<fmt:formatNumber value="${ w.value }" pattern="0.##" />時間</li>
							</c:forEach>
						</ul>
					</c:otherwise>
				</c:choose>
			</div>
			
			<%-- これからのシフト --%>
			<h5>これからのシフト</h5>
			<c:choose>
				<c:when test="${ empty shifts }">
					<p class="text-muted">これからのシフトはありません</p>
				</c:when>
				<c:otherwise>
					<table class="table table-hover mb-4">
						<c:forEach var="s" items="${ shifts }">
							<tr onclick="location.href='ShiftEdit.action?id=${ s.id }'" style="cursor: pointer;">
								<td>
									${ s.workDate.monthValue }/${ s.workDate.dayOfMonth }
									${ fn:substring('月火水木金土日', s.workDate.dayOfWeek.value -1, s.workDate.dayOfWeek.value) }
								</td>
								<td>${ workplaceNames[ s.workplaceId ] }</td>
								<td>${ s.startTime } ～ ${ s.endTime }</td>
							</tr>
						</c:forEach>
					</table>
				</c:otherwise>
			</c:choose>
			
			<%-- 報告書の提出状況 --%>
			<c:choose>
				<c:when test="${ submitted }">
					<div class="alert alert-success d-flex justfy-content-between align-items-center">
						<span>${ monthLabel }の報告書：提出済み (${ submittedAtText })</span>
						<a href="Report.action?month=${ month }" class="btn btn-warning btn-sm">提出する</a>
					</div>
				</c:when>
				<c:otherwise>
					<div class="alert alert-warning d-flex justfy-content-between align-items-center">
						<span>${ monthLabel }の報告書：未提出 </span>
						<a href="ReportConfirm.action?month=${ month }" class="btn btn-warning btn-sm">提出する</a>
					</div>
				</c:otherwise>
			</c:choose>

		</section>
	</c:param>
</c:import>