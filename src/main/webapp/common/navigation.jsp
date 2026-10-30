<%-- サイドバー（ログイン後に表示。学生と教職員でメニューを分ける） --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:set var="root" value="${pageContext.request.contextPath}/baitolog" />

<ul class="nav nav-pills flex-column mb-auto px-4">
	<c:choose>
		<c:when test="${user.teacher}">
			<%-- 教職員用メニュー --%>
			<li class="nav-item my-3"><a href="${root}/teacher/StudentList.action">学生一覧</a></li>
		</c:when>
		<c:otherwise>
			<%-- 学生用メニュー（各画面ができたら href を有効にする） --%>
			<li class="nav-item my-3"><a href="${root}/student/StudentTop.action">ホーム</a></li>
			<li class="nav-item mb-3"><a href="${root}/student/ShiftEdit.action">シフト</a></li>
			<li class="nav-item mb-3"><a href="${root}/student/WorkplaceList.action">勤務先</a></li>
			<li class="nav-item mb-3"><a href="${root}/student/Monthly.action">月次記録</a></li>
		</c:otherwise>
	</c:choose>
</ul>
