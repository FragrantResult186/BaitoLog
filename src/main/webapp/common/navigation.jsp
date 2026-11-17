<%-- ナビゲーション（ログイン後にヘッダーの下へ横一列で表示。学生と教職員でメニューを分ける） --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions"%>
<c:set var="root" value="${pageContext.request.contextPath}/baitolog" />
<%-- いま開いている画面：Actionの名前と、JSPのファイル名の両方で調べる --%>
<c:set var="uri" value="${requestScope['jakarta.servlet.forward.request_uri']}" />
<c:set var="jsp" value="${pageContext.request.requestURI}" />

<ul class="nav nav-tabs nav-justified app-tabs">
	<c:choose>
		<c:when test="${user.teacher}">
			<%-- 教職員用メニュー --%>
			<li class="nav-item"><a class="nav-link ${(fn:contains(uri, 'StudentList') or fn:contains(jsp, 'student_list')) ? 'active' : ''}" href="${root}/teacher/StudentList.action">学生一覧</a></li>
			<li class="nav-item"><a class="nav-link ${(fn:contains(uri, 'AccountImport') or fn:contains(jsp, 'account')) ? 'active' : ''}" href="${root}/teacher/AccountImport.action">アカウント登録</a></li>
		</c:when>
		<c:otherwise>
			<%-- 学生用メニュー --%>
			<li class="nav-item"><a class="nav-link ${(fn:contains(uri, 'StudentTop') or fn:contains(jsp, 'student_top')) ? 'active' : ''}" href="${root}/student/StudentTop.action">ホーム</a></li>
			<li class="nav-item"><a class="nav-link ${(fn:contains(uri, 'ShiftEdit') or fn:contains(jsp, 'shift')) ? 'active' : ''}" href="${root}/student/ShiftEdit.action">シフト</a></li>
			<li class="nav-item"><a class="nav-link ${(fn:contains(uri, 'WorkplaceList') or fn:contains(jsp, 'workplace')) ? 'active' : ''}" href="${root}/student/WorkplaceList.action">勤務先</a></li>
			<li class="nav-item"><a class="nav-link ${(fn:contains(uri, 'Monthly') or fn:contains(jsp, 'monthly')) ? 'active' : ''}" href="${root}/student/Monthly.action">月次記録</a></li>
		</c:otherwise>
	</c:choose>
</ul>