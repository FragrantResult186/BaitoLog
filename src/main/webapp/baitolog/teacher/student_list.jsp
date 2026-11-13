<%-- 学生一覧画面（教職員のトップ画面） --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<c:import url="/common/base.jsp">
	<c:param name="title">
		BaitoLog　学生一覧
	</c:param>

	<c:param name="scripts">
		<style>
			.status-over { color: #dc3545; font-weight: bold; }
			.status-caution { color: #b26a00; font-weight: bold; }
			.status-ok { color: #198754; }
			.status-none { color: #6c757d; }
		</style>
	</c:param>

	<c:param name="content">
		<section class="me-4">
			<h2 class="h3 mb-3 fw-norma bg-secondary bg-opacity-10 py-2 px-4">学生一覧</h2>

			<c:if test="${not empty errorMessage}">
				<div class="alert alert-danger mx-4" role="alert"><c:out value="${errorMessage}" /></div>
			</c:if>

			<%-- 対象期間・クラス（選ぶとすぐに一覧を作り直す） --%>
			<form method="get" action="StudentList.action" class="d-flex flex-wrap gap-3 px-4 mb-3 align-items-center">
				<label for="target_month" class="mb-0">対象期間</label>
				<select class="form-select w-auto" id="target_month" name="target_month" onchange="this.form.submit()">
					<c:forEach var="m" items="${months}">
						<option value="${m}" ${targetMonth == m.toString() ? 'selected' : ''}>${m.year} 年 ${m.monthValue} 月</option>
					</c:forEach>
				</select>
				<label for="class_id" class="mb-0">クラス</label>
				<select class="form-select w-auto" id="class_id" name="class_id" onchange="this.form.submit()">
					<option value="">すべて</option>
					<c:forEach var="schoolClass" items="${classes}">
						<option value="${schoolClass.id}" ${selectedClassId == schoolClass.id ? 'selected' : ''}>
							<c:out value="${schoolClass.name}" />
						</option>
					</c:forEach>
				</select>
				<noscript><button type="submit" class="btn btn-primary">表示</button></noscript>
			</form>

			<%-- 件数 --%>
			<div class="row g-3 px-4 mb-3">
				<div class="col-4">
					<div class="rounded p-3 h-100" style="background:#fff3cd;">
						<div class="small">上限 間近</div>
						<div class="fs-2 text-end">${cautionCount}</div>
					</div>
				</div>
				<div class="col-4">
					<div class="rounded p-3 h-100 bg-light">
						<div class="small">未入力</div>
						<div class="fs-2 text-end">${noneCount}</div>
					</div>
				</div>
				<div class="col-4">
					<div class="rounded p-3 h-100 bg-light">
						<div class="small">対象学生</div>
						<div class="fs-2 text-end">${studentCount}</div>
					</div>
				</div>
			</div>

			<p class="px-4 mb-2">期間内に上限を超えた記録のある学生：${overCount} 名</p>

			<div class="px-4">
				<table class="table table-sm align-middle">
					<thead>
						<tr>
							<th>氏名</th>
							<th>クラス</th>
							<th>勤務先</th>
							<th>7日間の最大</th>
							<th>該当期間</th>
							<th>状態</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="s" items="${summaries}">
							<tr>
								<td>
									<a href="StudentDetail.action?id=${s.student.id}&target_month=${targetMonth}&class_id=${selectedClassId}"><c:out value="${s.student.name}" /></a>
								</td>
								<td><c:out value="${s.student.schoolClass.name}" default="（未所属）" /></td>
								<td>${s.workplaceCount} 社</td>
								<c:choose>
									<c:when test="${s.entered}">
										<td>
											<span class="${s.status == 'OVER' ? 'status-over' : (s.status == 'CAUTION' ? 'status-caution' : '')}"><fmt:formatNumber value="${s.maxWindow.totalHours}" minFractionDigits="1" maxFractionDigits="1" /></span> 時間
										</td>
										<td>${s.maxWindow.startDate.monthValue}/${s.maxWindow.startDate.dayOfMonth} ～ ${s.maxWindow.endDate.monthValue}/${s.maxWindow.endDate.dayOfMonth}</td>
									</c:when>
									<c:otherwise>
										<td class="status-none">未入力</td>
										<td class="status-none">ー</td>
									</c:otherwise>
								</c:choose>
								<td>
									<%-- 色とアイコンの両方で状態を示す --%>
									<c:choose>
										<c:when test="${s.status == 'OVER'}"><span class="status-over">✖ 超過</span></c:when>
										<c:when test="${s.status == 'CAUTION'}"><span class="status-caution">▲ 間近</span></c:when>
										<c:when test="${s.status == 'OK'}"><span class="status-ok">✔ 上限内</span></c:when>
										<c:otherwise><span class="status-none">ー</span></c:otherwise>
									</c:choose>
								</td>
							</tr>
						</c:forEach>
						<c:if test="${empty summaries}">
							<tr><td colspan="6" class="text-center text-muted py-4">該当する生徒が見つかりません</td></tr>
						</c:if>
					</tbody>
				</table>
			</div>

			<div class="px-4 mb-4">
				<a href="StudentList.action?target_month=${targetMonth}&class_id=${selectedClassId}&csv=1"
					class="btn btn-outline-secondary ${empty summaries ? 'disabled' : ''}">CSV出力</a>
			</div>
		</section>
	</c:param>
</c:import>
