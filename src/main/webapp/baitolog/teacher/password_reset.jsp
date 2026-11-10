<%-- パスワードリセットの確認画面（教職員） --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:import url="/common/base.jsp">
	<c:param name="title">
		BaitoLog　パスワードリセット
	</c:param>

	<c:param name="scripts">
		<script>
			// 「リセットする」を押したら、二重に押せないようにする
			document.addEventListener("DOMContentLoaded", function() {
				var form = document.getElementById("resetForm");
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
			<h2 class="h3 mb-3 fw-norma bg-secondary bg-opacity-10 py-2 px-4">パスワードリセット</h2>

			<c:if test="${not empty errorMessage}">
				<div class="alert alert-danger mx-4" role="alert"><c:out value="${errorMessage}" /></div>
			</c:if>

			<div class="px-4">
				<table class="table table-sm w-auto">
					<tr><th class="pe-4">学生番号</th><td><c:out value="${student.loginId}" /></td></tr>
					<tr><th class="pe-4">氏名</th><td><c:out value="${student.name}" /></td></tr>
					<tr><th class="pe-4">クラス</th><td><c:out value="${student.schoolClass.name}" default="（未所属）" /></td></tr>
					<tr><th class="pe-4">生年月日</th><td><c:out value="${student.birthday}" default="未登録" /></td></tr>
				</table>
			</div>

			<c:if test="${canReset}">
				<div class="alert alert-warning mx-4" role="alert">
					この学生のパスワードを、<strong>生年月日（例：20061004）</strong>に戻します。<br>
					勤務先・シフトテンプレート・シフトなどのデータは消えません。<br>
					学生は、生年月日でログインしたあと、新しいパスワードを登録し直します。
				</div>
			</c:if>

			<div class="d-flex gap-2 px-4">
				<a href="StudentList.action" class="btn btn-outline-secondary">戻る</a>
				<c:if test="${canReset}">
					<form id="resetForm" method="post" action="PasswordReset.action">
						<input type="hidden" name="id" value="${student.id}">
						<input type="hidden" name="token" value="<c:out value='${token}' />">
						<button type="submit" class="btn btn-danger">リセットする</button>
					</form>
				</c:if>
			</div>
		</section>
	</c:param>
</c:import>
