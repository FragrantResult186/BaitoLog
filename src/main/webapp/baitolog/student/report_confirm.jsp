<%-- 報告書の提出確認（学生） --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions"%>

<c:import url="/common/base.jsp">
	<c:param name="title">
		${monthLabel}の報告書 | BaitoLog
	</c:param>

	<c:param name="scripts"></c:param>

	<c:param name="content">
		<section class="me-4">
			<h2 class="h3 mb-3 fw-normal bg-secondary bg-opacity-10 py-2 px-4">${monthLabel}の報告書（提出前の確認）</h2>

			<c:if test="${not empty errorMessage}">
				<div class="alert alert-danger mx-4" role="alert">${errorMessage}</div>
			</c:if>

			<div class="px-4">
				<%-- 月の切り替え --%>
				<div class="d-flex gap-2 mb-3">
					<a class="btn btn-sm btn-outline-secondary" href="ReportConfirm.action?month=${prevMonth}">← 前の月</a>
					<a class="btn btn-sm btn-outline-secondary" href="ReportConfirm.action?month=${nextMonth}">次の月 →</a>
				</div>

				<%-- 集計 --%>
				<table class="table table-bordered w-auto mb-4">
					<tr>
						<th class="bg-light">月の合計</th>
						<td><fmt:formatNumber value="${report.totalHours}" pattern="0.##" /> 時間</td>
					</tr>
					<tr>
						<th class="bg-light">7日間の最大</th>
						<td>
							<fmt:formatNumber value="${report.max7dayHours}" pattern="0.##" /> 時間
							<c:if test="${not empty maxWindow}">
								（${maxWindow.startDate.monthValue}/${maxWindow.startDate.dayOfMonth}
								〜 ${maxWindow.endDate.monthValue}/${maxWindow.endDate.dayOfMonth}
								／ 上限 <fmt:formatNumber value="${maxWindow.limitHours}" pattern="0.##" /> 時間）
							</c:if>
						</td>
					</tr>
					<tr>
						<th class="bg-light">勤務先の数</th>
						<td>${report.workplaceCount}</td>
					</tr>
				</table>

				<c:if test="${maxWindow.over}">
					<div class="alert alert-danger">
						7日間の上限を <fmt:formatNumber value="${maxWindow.overHours}" pattern="0.##" /> 時間
						超えている期間があります。内容を確認してください。
					</div>
				</c:if>

				<%-- 勤務先別の合計 --%>
				<h5>勤務先別の合計</h5>
				<ul class="list-unstyled mb-4">
					<c:forEach var="w" items="${workplaceHours}">
						<li><c:out value="${w.key}" />：<fmt:formatNumber value="${w.value}" pattern="0.##" /> 時間</li>
					</c:forEach>
					<c:if test="${empty workplaceHours}">
						<li class="text-muted">この月の勤務はありません</li>
					</c:if>
				</ul>

				<%-- 明細 --%>
				<h5>勤務の明細</h5>
				<table class="table table-sm table-striped mb-4">
					<thead>
						<tr><th>日付</th><th>勤務先</th><th>時間帯</th><th>休憩</th><th class="text-end">労働時間</th></tr>
					</thead>
					<tbody>
						<c:forEach var="i" items="${report.items}">
							<tr>
								<td>
									${i.workDate.monthValue}/${i.workDate.dayOfMonth}
									${fn:substring('月火水木金土日', i.workDate.dayOfWeek.value - 1, i.workDate.dayOfWeek.value)}
								</td>
								<td><c:out value="${i.workplaceName}" /></td>
								<td>${i.startTime} ～ ${i.endTime}</td>
								<td>
									<c:choose>
										<c:when test="${not empty i.breakStart and not empty i.breakEnd}">${i.breakStart} ～ ${i.breakEnd}</c:when>
										<c:otherwise>なし</c:otherwise>
									</c:choose>
								</td>
								<td class="text-end"><fmt:formatNumber value="${i.workingHours}" pattern="0.##" /></td>
							</tr>
						</c:forEach>
						<c:if test="${empty report.items}">
							<tr><td colspan="5" class="text-muted">この月の勤務はありません</td></tr>
						</c:if>
					</tbody>
				</table>

				<%-- 提出 --%>
				<form action="ReportConfirm.action" method="post" class="d-flex gap-2"
					onsubmit="return confirm('${monthLabel}分の報告書を提出しますか？\n提出後は内容を変更できません。');">
					<input type="hidden" name="month" value="${month}">
					<button type="submit" class="btn btn-primary" ${empty report.items ? 'disabled' : ''}>提出する</button>
					<a class="btn btn-outline-secondary" href="StudentTop.action">戻る</a>
				</form>
			</div>
		</section>
	</c:param>
</c:import>
