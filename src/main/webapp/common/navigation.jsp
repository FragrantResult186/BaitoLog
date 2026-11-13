<%-- サイドバー（ログイン後に表示。学生と教職員でメニューを分ける） --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="root" value="${pageContext.request.contextPath}/baitolog" />
<%--  --%>
<c:set var="uri" value="${ requestScope[ 'jakarta.servlet.forward.request_yri']}"></c:set>

<ul class="nav nav-tabs nav-justified app-tabs">
	<c:choose>
		<c:when test="${user.teacher}">
			<%-- 教職員用メニュー --%>
			<li class="nav-item"><a class="nav-link ${fn:contains(uri, 'StudentList') ? 'active' : ''}" href="${root}/teacher/StudentList.action">学生一覧</a></li>
			<li class="nav-item"><a class="nav-link ${fn:contains(uri, 'AccountImport') ? 'active' : ''}" href="${root}/teacher/AccountImport.action">アカウント登録</a></li>
		</c:when>
		<c:otherwise>
			<%-- 学生用メニュー（各画面ができたら href を有効にする） --%>
			<li class="nav-item"><a class="nav-link ${fn:contains(uri, 'StudentTop') ? 'active' : ''}" href="${root}/student/StudentTop.action">ホーム</a></li>
			<li class="nav-item"><a class="nav-link ${fn:contains(uri, 'ShiftEdit') ? 'active' : ''}" href="${root}/student/ShiftEdit.action">シフト</a></li>
			<li class="nav-item"><a class="nav-link ${fn:contains(uri, 'WorkplaceList') ? 'active' : ''}" href="${root}/student/WorkplaceList.action">勤務先</a></li>
			<li class="nav-item"><a class="nav-link ${fn:contains(uri, 'Monthly') ? 'active' : ''}" href="${root}/student/Monthly.action">月次記録</a></li>
		</c:otherwise>
	</c:choose>
</ul>
