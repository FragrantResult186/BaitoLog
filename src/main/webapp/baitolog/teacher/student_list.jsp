<%-- 学生一覧画面（教職員） --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:import url="/common/base.jsp">
	<c:param name="title">
		BaitoLog　学生一覧
	</c:param>

	<c:param name="scripts"></c:param>

	<c:param name="content">
		<section class="me-4">
			<h2 class="h3 mb-3 fw-norma bg-secondary bg-opacity-10 py-2 px-4">学生一覧</h2>

			<c:if test="${not empty resetMessage}">
				<div class="alert alert-success mx-4" role="alert"><c:out value="${resetMessage}" /></div>
			</c:if>
			<c:if test="${not empty errorMessage}">
				<div class="alert alert-danger mx-4" role="alert"><c:out value="${errorMessage}" /></div>
			</c:if>

			<%-- 絞り込み --%>
			<form method="get" action="StudentList.action" class="row g-2 px-4 mb-3 align-items-end">
				<div class="col-auto">
					<label class="form-label mb-0 small" for="classId">クラス</label>
					<select class="form-select" id="classId" name="classId">
						<option value="">すべて</option>
						<c:forEach var="schoolClass" items="${classes}">
							<option value="${schoolClass.id}" ${selectedClassId == schoolClass.id ? 'selected' : ''}>
								<c:out value="${schoolClass.name}" />
							</option>
						</c:forEach>
					</select>
				</div>
				<div class="col-auto">
					<label class="form-label mb-0 small" for="q">氏名・学生番号</label>
					<input type="text" class="form-control" id="q" name="q" maxlength="30"
						value="<c:out value='${keyword}' />">
				</div>
				<div class="col-auto">
					<button type="submit" class="btn btn-primary">絞り込む</button>
					<a href="StudentList.action" class="btn btn-outline-secondary">解除</a>
				</div>
				<div class="col text-end">
					<a href="AccountImport.action" class="btn btn-outline-primary">アカウント登録</a>
				</div>
			</form>

			<p class="px-4 mb-2">${students.size()} 人</p>

			<div class="px-4">
				<table class="table table-sm align-middle">
					<thead>
						<tr>
							<th>クラス</th>
							<th>学生番号</th>
							<th>氏名</th>
							<th>生年月日</th>
							<th>パスワード</th>
							<th></th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="student" items="${students}">
							<tr>
								<td><c:out value="${student.schoolClass.name}" default="（未所属）" /></td>
								<td><c:out value="${student.loginId}" /></td>
								<td><c:out value="${student.name}" /></td>
								<td><c:out value="${student.birthday}" default="未登録" /></td>
								<td>
									<c:choose>
										<c:when test="${student.passwordInitialized}">登録済み</c:when>
										<c:otherwise><span class="text-danger">初期パスワードのまま</span></c:otherwise>
									</c:choose>
								</td>
								<td class="text-end">
									<c:choose>
										<c:when test="${empty student.birthday}">
											<button type="button" class="btn btn-sm btn-outline-secondary" disabled>リセット</button>
										</c:when>
										<c:otherwise>
											<a href="PasswordReset.action?id=${student.id}" class="btn btn-sm btn-outline-danger">パスワードをリセット</a>
										</c:otherwise>
									</c:choose>
								</td>
							</tr>
						</c:forEach>
						<c:if test="${empty students}">
							<tr><td colspan="6" class="text-center text-muted py-4">学生がいません</td></tr>
						</c:if>
					</tbody>
				</table>
			</div>
		</section>
	</c:param>
</c:import>
