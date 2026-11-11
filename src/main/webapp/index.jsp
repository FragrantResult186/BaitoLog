<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
	// ログイン済みの学生はダッシュボードへ、それ以外はログイン画面へ
	Object user = session.getAttribute("user");
	if (user instanceof bean.Student) {
		response.sendRedirect("baitolog/StudentTop.action");
	} else {
		response.sendRedirect("baitolog/Login.action");
	}
%>