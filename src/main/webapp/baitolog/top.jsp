<%-- 起動確認JSP（ログイン画面ができたら削除する） --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:import url="/common/base.jsp">
	<c:param name="title">
		BaitoLog
	</c:param>

	<c:param name="scripts"></c:param>

	<c:param name="content">
		<section class="me-4">
			<h2 class="h3 mb-3 fw-norma bg-secondary bg-opacity-10 py-2 px-4">起動確認</h2>
			<p class="px-4">起動できました。この画面が出れば開発環境の準備は完了です。</p>
			<p class="px-4">データベース接続：OK（H2 ${dbVersion}）</p>
		</section>
	</c:param>
</c:import>
