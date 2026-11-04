<%-- 新パスワード登録画面（初回ログイン時のみ） --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:import url="/common/base.jsp">
	<c:param name="title">
		BaitoLog　新しいパスワードの登録
	</c:param>

	<c:param name="scripts"></c:param>

	<c:param name="content">
		<section class="me-4">
			<h2 class="h3 mb-3 fw-norma bg-secondary bg-opacity-10 py-2 px-4">新しいパスワードの登録</h2>
			<p class="px-4">初回ログインです。これから使う新しいパスワードを、2回入力してください。</p>

			<%-- 入力に問題があるときだけ表示 --%>
			<c:if test="${not empty errorMessage}">
				<div class="alert alert-danger mx-4" role="alert">${errorMessage}</div>
			</c:if>

			<form action="SetPassword.action" method="post" class="px-4">
				<div class="mb-3">
					<label class="form-label" for="new_password">新しいパスワード</label>
					<input type="password" class="form-control" id="new_password" name="new_password"
						maxlength="40" required autofocus>
				</div>
				<div class="mb-3">
					<label class="form-label" for="new_password2">新しいパスワード（確認）</label>
					<input type="password" class="form-control" id="new_password2" name="new_password2"
						maxlength="40" required>
				</div>
				<div class="form-check mb-3">
					<input type="checkbox" class="form-check-input" id="show_password">
					<label class="form-check-label" for="show_password">パスワードを表示する（2つが同じか確認できます）</label>
				</div>
				<div class="d-grid gap-2">
					<button type="submit" class="btn btn-primary">登録</button>
				</div>
			</form>
			<script>
			// 「パスワードを表示する」：チェックを入れると、2つの入力欄の文字が見える
			document.getElementById("show_password").addEventListener("change", function () {
				var type = this.checked ? "text" : "password";
				document.getElementById("new_password").type = type;
				document.getElementById("new_password2").type = type;
			});
			</script>
		</section>
	</c:param>
</c:import>
