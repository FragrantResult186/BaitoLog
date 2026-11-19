
<%-- 新規登録画面 --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:import url="/common/base.jsp">
	<c:param name="title">
		BaitoLog　新規登録
	</c:param>

	<c:param name="scripts"></c:param>

	<c:param name="content">
		<section>
			<h2 class="h3 mb-3 fw-normal bg-secondary bg-opacity-10 py-2 px-4">
				新規登録
			</h2>

			<%-- エラーメッセージ --%>
			<c:if test="${not empty errorMessage}">
				<div class="alert alert-danger mx-4" role="alert">
					<c:out value="${errorMessage}" />
				</div>
			</c:if>

			<p class="px-4">
				必要な情報を入力して、学生アカウントを登録してください。
			</p>

			<form action="Signup.action" method="post" class="px-4">

				<%-- 名前 --%>
				<div class="mb-3">
					<label class="form-label" for="name">
						名前
					</label>
					<input type="text"
						class="form-control"
						id="name"
						name="name"
						maxlength="30"
						value="<c:out value='${name}' />"
						required>
				</div>

				<%-- 学生番号 --%>
				<div class="mb-3">
					<label class="form-label" for="username">
						学生番号
					</label>
					<input type="text"
						class="form-control"
						id="username"
						name="username"
						maxlength="20"
						value="<c:out value='${username}' />"
						required>
				</div>

				<%-- パスワード --%>
				<div class="mb-3">
					<label class="form-label" for="password">
						パスワード
					</label>
					<input type="password"
						class="form-control"
						id="password"
						name="password"
						maxlength="20"
						required>
				</div>

				<%-- パスワード確認 --%>
				<div class="mb-3">
					<label class="form-label" for="password2">
						パスワード（確認）
					</label>
					<input type="password"
						class="form-control"
						id="password2"
						name="password2"
						maxlength="20"
						required>
				</div>

				<%-- 生年月日 --%>
				<div class="mb-3">
					<label class="form-label" for="birthday">
						生年月日
					</label>
					<input type="date"
						class="form-control"
						id="birthday"
						name="birthday"
						value="<c:out value='${birthday}' />"
						required>
				</div>

				<%-- 学校コード --%>
				<div class="mb-3">
					<label class="form-label" for="school_code">
						学校コード
					</label>
					<input type="text"
						class="form-control"
						id="school_code"
						name="school_code"
						maxlength="10"
						value="<c:out value='${school_code}' />"
						required>
				</div>

				<%-- 登録ボタン --%>
				<div class="d-grid gap-2">
					<button type="submit" class="btn btn-primary">
						登録する
					</button>

					<a class="btn btn-outline-secondary"
						href="Login.action">
						ログイン画面へ戻る
					</a>
				</div>

			</form>
		</section>
	</c:param>
</c:import>
