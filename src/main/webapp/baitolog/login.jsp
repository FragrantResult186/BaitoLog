<%-- ログイン画面 --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:import url="/common/base.jsp">
	<c:param name="title">
		BaitoLog　ログイン
	</c:param>

	<c:param name="scripts"></c:param>

	<c:param name="content">
		<section class="me-4">
			<h2 class="h3 mb-3 fw-norma bg-secondary bg-opacity-10 py-2 px-4">ログイン</h2>

			<%-- 認証に失敗したときだけ表示 --%>
			<c:if test="${not empty errorMessage}">
				<div class="alert alert-danger mx-4" role="alert">${errorMessage}</div>
			</c:if>

			<form action="Login.action" method="post" class="px-4">
				<div class="mb-3">
					<label class="form-label" for="username">ユーザーID</label>
					<input type="text" class="form-control" id="username" name="username"
						maxlength="20" value="<c:out value="${username}"/>" required autofocus>
				</div>
				<div class="mb-3">
					<label class="form-label" for="password">パスワード</label>
					<input type="password" class="form-control" id="password" name="password"
						maxlength="40" required>
				</div>
				<div class="d-grid gap-2">
					<button type="submit" class="btn btn-primary">ログイン</button>
					<%-- 新規登録画面ができたら有効になる --%>
					<a class="btn btn-outline-primary" href="Signup.action">新規登録</a>
				</div>
			</form>
		</section>
	</c:param>
</c:import>
