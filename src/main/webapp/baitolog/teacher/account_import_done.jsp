<%-- 作成完了画面（教職員）：作った人数と、全員共通の初期パスワードを1回だけ表示する --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:import url="/common/base.jsp">
	<c:param name="title">
		BaitoLog　アカウント作成完了
	</c:param>

	<c:param name="scripts"></c:param>

	<c:param name="content">
		<section class="me-4">
			<h2 class="h3 mb-3 fw-norma bg-secondary bg-opacity-10 py-2 px-4">アカウント作成完了</h2>

			<div class="alert alert-success mx-4" role="alert">
				<c:out value="${createdCount}" />人のアカウントを作成しました
			</div>

			<div class="px-4">
				<p class="mb-1">初期パスワード（全員共通）</p>
				<p><code class="fs-3 user-select-all"><c:out value="${initialPassword}" /></code></p>
				<p class="text-danger">
					この画面を閉じると、初期パスワードは二度と表示できません。今のうちに控えて、学生に伝えてください。
				</p>
				<p class="small text-muted">
					学生は、学生番号と、この初期パスワードで最初にログインします。そのあと、各自で新しいパスワードを登録します。
				</p>
				<a class="btn btn-primary" href="TeacherTop.action">学生一覧へ</a>
				<a class="btn btn-outline-secondary" href="AccountImport.action">続けて取り込む</a>
			</div>
		</section>
	</c:param>
</c:import>
