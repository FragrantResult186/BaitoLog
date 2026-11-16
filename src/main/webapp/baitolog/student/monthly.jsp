<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions"%>

<c:import url="/common/base.jsp">
	<c:param name="title">
		月次記録 | Baitolog
	</c:param>
	
	<c:param name="scripts"></c:param>
	
	<c:param name="content">
		<section>
		
			<%-- 対象年月 --%>
			<select class="form-select w-auto mb-4" name="target_month"
				onchange="location.href = 'Monthly.action?month=' + this.value;">
				<c:forEach var="i" begin="0" end="12">
					<c:set var="m" value="${ summary.month.plusMonths(i - 6) }"></c:set>
					<option value="${m.year}-${m.monthValue lt 10 ? '0' : ''}${m.monthValue}" ${i == 6 ? 'selected' : ''}>${m.year}年${m.monthValue}月</option>
				</c:forEach>
			</select>
			
			<%-- 勤務記録一覧 --%>
			<table class="table align-middle mb-4">
				<thead class="small text-secondry">
					<tr>
						<th>日付</th>
						<th>勤務先</th>
						<th>勤務時間</th>
						<th>休憩</th>
						<th class="text-end">労働時間</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="s" items="${ monthlyShifts }">
						<tr>
							<td>
								${ s.workDate.monthValue }/${ s.workDate.dayOfMonth }
								${ fn:substring('月火水木金土日', s.workDate.dayOfWeek.value - 1, s.workDate.dayOfWeek.value) }
							</td>
							<td>${empty workplaceNames[s.workplaceId] ? '勤務先不明' : workplaceNames[s.workplaceId]}</td>
							<td class="text-secondary">${ s.startTime }～${ s.endTime }</td>
							<td class="text-secondary">
								<c:choose>
									<c:when test="${ not empty s.breakStart }">${ s.breakStart }～${ s.breakEnd }</c:when>
									<c:otherwise>ー</c:otherwise>
								</c:choose>
							</td>
							<td class="text-end"><fmt:formatNumber value="${ s.workHours }" pattern="0.##"></fmt:formatNumber>
					</c:forEach>
				</tbody>
			</table>
		
		
		<c:if test="${ not empty monthlyShifts }">
			<div class="text-end small mb-4">
				<div>月合計労働時間：<strong><fmt:formatNumber  value="${ summary.totalHours }" pattern="0.##"></fmt:formatNumber></strong> 時間</div>
				<div>
					7日間の最大（${summary.max7dayStart.monthValue}/${summary.max7dayStart.dayOfMonth}
					〜 ${summary.max7dayEnd.monthValue}/${summary.max7dayEnd.dayOfMonth}）：
					<strong><fmt:formatNumber value="${summary.max7dayHours}" pattern="0.##" /></strong> 時間
				</div>
			</div>
		</c:if>
		
		<%-- ボタン --%>
		<div class="d-flex gap-3 mb-5">
			<c:choose>
				<%-- データがあるとき：それぞれの画面へ移動する --%>
				<c:when test="${not empty monthlyShifts}">
					<a href="Report.action?month=${month}" class="btn text-white fw-bold text-start"
						style="background: #1e3a5f; width: 18rem;">報告書を出力</a>
				</c:when>
				<%-- データがないとき：押すとメッセージを出す --%>
				<c:otherwise>
					<button type="button" class="btn text-white fw-bold text-start"
						style="background: #1e3a5f; width: 18rem;" onclick="showNoData();">報告書を出力</button>
				</c:otherwise>
			</c:choose>
		</div>

		<%-- データがないときのメッセージ（ボタンを押したときだけ、3秒間表示する） --%>
		<c:if test="${empty monthlyShifts}">
			<div id="noDataMessage" class="d-none text-white rounded-3 px-4 py-3 shadow"
				style="position: fixed; top: 6rem; left: 50%; transform: translateX(-50%); background: #404040; z-index: 1080;">
				出力できるデータがありません
			</div>
			<script>
				function showNoData() {
					var box = document.getElementById('noDataMessage');
					box.classList.remove('d-none');
					clearTimeout(window.noDataTimer);
					window.noDataTimer = setTimeout(function () {
						box.classList.add('d-none');
					}, 3000);
				}
			</script>
		</c:if>
		</section>
	</c:param>
	
</c:import>