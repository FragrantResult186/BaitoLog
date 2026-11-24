<%-- ダッシュボード（学生）通常／提出後を兼ねる --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions"%>

<c:import url="/common/base.jsp">
	<c:param name="title">
		ダッシュボード | BaitoLog
	</c:param>

	<c:param name="scripts">
		<%-- 土・日・祝日の色分け（シフト登録の画面と同じ判定） --%>
		<script src="${pageContext.request.contextPath}/js/holidays.js"></script>
	</c:param>

	<c:param name="content">
		<section class="dash">

			<%-- 進捗バーの色：上限の2時間前（上限28時間なら26時間）以上、または超過なら赤。
			     表示のたびに、登録済みのシフトから計算し直す --%>
			<c:set var="warnHours" value="${result.limitHours - 2}" />
			<c:choose>
				<c:when test="${result.status == 'OVER' or result.totalHours >= warnHours}"><c:set var="barClass" value="bar-red" /></c:when>
				<c:when test="${result.status == 'CAUTION'}"><c:set var="barClass" value="bg-warning" /></c:when>
				<c:otherwise><c:set var="barClass" value="bg-success" /></c:otherwise>
			</c:choose>

			<div class="dash-layout">

				<%-- ===== 左：1枚の白いボード（直近7日間のまとめ・報告書） ===== --%>
				<div class="board">

					<%-- 提出直後のmessage --%>
					<c:if test="${ not empty message }">
						<div class="alert alert-success">✓ ${ message }</div>
					</c:if>

					<c:if test="${ not empty errorMessage }">
						<div class="alert alert-danger">${ errorMessage }</div>
					</c:if>

					<%-- 対象期間 --%>
					<p class="board-period">
						直近7日間　${result.startDate.monthValue}/${result.startDate.dayOfMonth}
						〜 ${result.endDate.monthValue}/${result.endDate.dayOfMonth}
					</p>

					<%-- 合計と上限 ＋ 進捗バー --%>
					<p class="board-total">
						<fmt:formatNumber value="${result.totalHours}" pattern="0.##" /> /
						<fmt:formatNumber value="${result.limitHours}" pattern="0.##" /> 時間
					</p>
					<div class="progress">
						<div class="progress-bar ${barClass}" role="progressbar"
							style="width: ${result.percent > 100 ? 100 : result.percent}%"></div>
					</div>

					<%-- 残り時間メッセージ --%>
					<p class="board-message">
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
					<div class="board-section">
						<h3 class="board-title">勤務先別（7日間）</h3>
						<c:choose>
							<c:when test="${ empty result.workplaceHours }">
								<p class="text-muted mb-0">この7日間の勤務はありません</p>
							</c:when>
							<c:otherwise>
								<ul class="workplace-list">
									<c:forEach var="w" items="${ result.workplaceHours }">
										<li><c:out value="${ w.key }" />：<fmt:formatNumber value="${ w.value }" pattern="0.##" /> 時間</li>
									</c:forEach>
								</ul>
							</c:otherwise>
						</c:choose>
					</div>

					<%-- 報告書の提出状況 --%>
					<div class="board-section">
						<h3 class="board-title">報告書の提出状況</h3>
						<c:choose>
							<c:when test="${ submitted }">
								<div class="alert alert-success d-flex flex-wrap justify-content-between align-items-center gap-2 mb-0">
									<span>${ monthLabel }の報告書：提出済み (${ submittedAtText })</span>
									<a href="ReportConfirm.action?month=${ month }" class="btn btn-warning btn-sm">提出する</a>
								</div>
							</c:when>
							<c:otherwise>
								<div class="alert alert-warning d-flex flex-wrap justify-content-between align-items-center gap-2 mb-0">
									<span>${ monthLabel }の報告書：未提出</span>
									<a href="ReportConfirm.action?month=${ month }" class="btn btn-warning btn-sm">提出する</a>
								</div>
							</c:otherwise>
						</c:choose>
					</div>
				</div>

				<%-- ===== 右：カレンダーと今月のシフト予定（1枚のパネル） ===== --%>
				<aside class="side-panel">

					<%-- カレンダー（中身はJavaScriptで描く。印は下の一覧のdata-dateから作る） --%>
					<div id="miniCal">
						<div class="cal-head">
							<button type="button" class="cal-nav" id="calPrev" aria-label="前の月"><i class="bi bi-chevron-left" aria-hidden="true"></i></button>
							<h3 class="cal-title" id="calTitle"></h3>
							<button type="button" class="cal-nav" id="calNext" aria-label="次の月"><i class="bi bi-chevron-right" aria-hidden="true"></i></button>
						</div>
						<div class="cal-grid" id="calGrid"></div>
						<p class="cal-legend">
							<span><i class="lg-today"></i>今日</span>
							<span><i class="lg-shift"></i>シフトあり</span>
						</p>
					</div>

					<%-- 今月のシフト予定（これからのシフトを、表示中の月で絞り込む） --%>
					<div class="side-section">
						<h3 class="section-title">今月のシフト予定</h3>
						<c:if test="${ empty shifts }">
							<p class="text-muted mt-3 mb-0">これからのシフトはありません</p>
						</c:if>
						<c:if test="${ not empty shifts }">
							<ul class="shift-list" id="shiftList">
								<c:forEach var="s" items="${ shifts }">
									<li>
										<a class="shift-item" data-date="${ s.workDate }" href="ShiftEdit.action?id=${ s.id }">
											<span class="shift-date">
												${ s.workDate.monthValue }/${ s.workDate.dayOfMonth }
												<small>${ fn:substring('月火水木金土日', s.workDate.dayOfWeek.value -1, s.workDate.dayOfWeek.value) }</small>
											</span>
											<span class="shift-info">
												<span class="shift-place"><c:out value="${workplaceMap[s.workplaceId].displayName}" /></span>
												<span class="shift-time">${ s.startTime } ～ ${ s.endTime }</span>
											</span>
										</a>
									</li>
								</c:forEach>
							</ul>
							<p class="text-muted mt-3 mb-0" id="shiftEmpty" style="display: none;">この月のシフトはありません</p>
						</c:if>
					</div>
				</aside>
			</div>

			<script>
			(function () {
				var grid = document.getElementById('calGrid');
				var title = document.getElementById('calTitle');
				if (!grid) { return; }

				var items = document.querySelectorAll('#shiftList .shift-item');
				var emptyMsg = document.getElementById('shiftEmpty');
				var dows = ['日', '月', '火', '水', '木', '金', '土'];
				var now = new Date();
				var today = ymd(now.getFullYear(), now.getMonth(), now.getDate());
				var year = now.getFullYear();
				var month = now.getMonth();

				// 「2026-11-02」の形にそろえる
				function pad(n) { return (n < 10 ? '0' : '') + n; }
				function ymd(y, m, d) { return y + '-' + pad(m + 1) + '-' + pad(d); }

				// シフトがある日の一覧（一覧のdata-dateから作る）
				var shiftDays = {};
				for (var i = 0; i < items.length; i++) {
					shiftDays[items[i].getAttribute('data-date')] = true;
				}

				function render() {
					title.textContent = year + '年' + (month + 1) + '月';
					var html = '';
					for (var i = 0; i < 7; i++) {
						html += '<div class="cal-dow' + (i === 0 ? ' is-sun' : (i === 6 ? ' is-sat' : '')) + '">' + dows[i] + '</div>';
					}

					var first = new Date(year, month, 1).getDay();
					var last = new Date(year, month + 1, 0).getDate();
					for (var b = 0; b < first; b++) { html += '<div class="cal-day is-empty"></div>'; }
					for (var d = 1; d <= last; d++) {
						var key = ymd(year, month, d);
						var cls = 'cal-day';
						if (shiftDays[key]) { cls += ' has-shift'; }
						if (key === today) { cls += ' is-today'; }
						// 土・日・祝日の色（祝日は、名前をマウスを乗せたときに表示）
						var holiday = '';
						if (window.BaitoCalendar) {
							var dayCls = BaitoCalendar.dayClass(key);
							if (dayCls) { cls += ' ' + dayCls; }
							holiday = BaitoCalendar.holidayName(key);
						}
						html += '<div class="' + cls + '"' + (holiday ? ' title="' + holiday + '"' : '') + '>' + d + '</div>';
					}
					grid.innerHTML = html;

					// 表示中の月のシフトだけを一覧に出す
					var prefix = year + '-' + pad(month + 1) + '-';
					var shown = 0;
					for (var j = 0; j < items.length; j++) {
						var date = items[j].getAttribute('data-date');
						var li = items[j].parentNode;
						var inMonth = date.indexOf(prefix) === 0;
						li.style.display = inMonth ? '' : 'none';
						if (inMonth) { shown++; }
						if (date === today && !items[j].querySelector('.shift-badge')) {
							items[j].className += ' is-today';
							var badge = document.createElement('span');
							badge.className = 'shift-badge';
							badge.textContent = '今日';
							items[j].appendChild(badge);
						}
					}
					if (emptyMsg) { emptyMsg.style.display = shown === 0 ? '' : 'none'; }
				}

				document.getElementById('calPrev').onclick = function () {
					month--;
					if (month < 0) { month = 11; year--; }
					render();
				};
				document.getElementById('calNext').onclick = function () {
					month++;
					if (month > 11) { month = 0; year++; }
					render();
				};
				render();
			})();
			</script>

		</section>
	</c:param>
</c:import>