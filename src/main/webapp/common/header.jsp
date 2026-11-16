<%-- ヘッダー --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<span class="app-brand">BaitoLog</span>
<c:if test="${user.isAuthenticated()}">
	<div class="app-user">
		<span>${user.getName()}</span>
		<a href="${pageContext.request.contextPath}/baitolog/Logout.action"
			onclick="return confirm('ログアウトしますか？');">ログアウト</a>
	</div>
</c:if>