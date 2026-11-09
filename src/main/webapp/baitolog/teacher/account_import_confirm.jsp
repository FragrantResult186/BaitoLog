<%-- 確認画面（教職員）：読み込んだ内容を見せて、「完了」で登録する --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:import url="/common/base.jsp">
	<c:param name="title">
		BaitoLog　アカウント作成の確認
	</c:param>

	<c:param name="scripts"></c:param>

	<c:param name="content">
		<section class="me-4">
			<h2 class="h3 mb-3 fw-norma bg-secondary bg-opacity-10 py-2 px-4">アカウント作成の確認</h2>

			<%-- 登録に失敗したときだけ表示 --%>
			<c:if test="${not empty errorMessage}">
				<div class="alert alert-danger mx-4" role="alert"><c:out value="${errorMessage}" /></div>
			</c:if>

			<p class="px-4">
				<strong><c:out value="${okCount}" /></strong>人が追加されます。
				<c:if test="${errorCount > 0}">
					（エラーの<c:out value="${errorCount}" />行はスキップされます）
				</c:if>
			</p>

			<div class="px-4">
				<table class="table table-sm align-middle">
					<thead>
						<tr>
							<th>行</th>
							<th>名前</th>
							<th>学生番号</th>
							<th>クラス</th>
							<th>状態</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="row" items="${rows}">
							<tr class="${empty row.error ? '' : 'table-danger'}">
								<td><c:out value="${row.lineNo}" /></td>
								<td><c:out value="${row.name}" /></td>
								<td><c:out value="${row.studentNo}" /></td>
								<td><c:out value="${row.className}" /></td>
								<td>
									<c:choose>
										<c:when test="${empty row.error}">追加されます</c:when>
										<c:otherwise><c:out value="${row.error}" /></c:otherwise>
									</c:choose>
								</td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
			</div>

			<form id="confirmForm" action="AccountImportConfirmExecute.action" method="post" class="px-4">
				<a class="btn btn-outline-secondary" href="AccountImport.action">戻る</a>
				<button type="submit" id="doneButton" class="btn btn-primary"
					${okCount == 0 ? 'disabled' : ''}>完了</button>
			</form>
			<script>
			// 「完了」：確認してから送信し、送信中はボタンを押せなくする（二重に作らないため）
			document.getElementById("confirmForm").addEventListener("submit", function (event) {
				if (!window.confirm("この情報を登録しますか？")) {
					event.preventDefault();
					return;
				}
				document.getElementById("doneButton").disabled = true;
			});
			</script>
		</section>
	</c:param>
</c:import>
