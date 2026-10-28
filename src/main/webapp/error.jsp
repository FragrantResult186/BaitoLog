<%-- エラーページ（FrontController で例外が起きたときに表示） --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:import url="/common/base.jsp">
	<c:param name="title">
		BaitoLog
	</c:param>

	<c:param name="scripts"></c:param>

	<c:param name="content">
		<section>
			<p>エラーが発生しました</p>
			<p class="text-muted small">原因は Eclipse のコンソールに表示されています。</p>
		</section>
	</c:param>
</c:import>
