<%-- 提出済みの報告書（学生） --%>
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
			<h2 class="h3 mb-3 fw-normal bg-secondary bg-opacity-10 py-2 px-4">${monthLabel}の報告書</h2>

			<div class="px-4">
				<div class="alert alert-success">提出済み（${submittedAtText}）</div>

				<table class="table table-bordered w-auto mb-4">
					<tr>
						<th class="bg-light">月の合計</th>
						<td><fmt:formatNumber value="${report.totalHours}" pattern="0.##" /> 時間</td>
					</tr>
					<tr>
						<th class="bg-light">7日間の最大</th>
						<td>
							<fmt:formatNumber value="${report.max7dayHours}" pattern="0.##" /> 時間
							<c:if test="${not empty report.max7dayStart}">
								（${report.max7dayStart.monthValue}/${report.max7dayStart.dayOfMonth} から7日間）
							</c:if>
						</td>
					</tr>
					<tr>
						<th class="bg-light">勤務先の数</th>
						<td>${report.workplaceCount}</td>
					</tr>
				</table>

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
					</tbody>
				</table>

				<a class="btn btn-outline-secondary" href="StudentTop.action">ホームへ戻る</a>
			</div>
		</section>
	</c:param>
</c:import>
