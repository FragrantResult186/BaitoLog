<%-- アカウント作成画面（教職員）：学生のCSVファイルを選んで取り込む --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:import url="/common/base.jsp">
	<c:param name="title">
		BaitoLog　アカウント作成
	</c:param>

	<c:param name="scripts"></c:param>

	<c:param name="content">
		<section class="me-4">
			<h2 class="h3 mb-3 fw-norma bg-secondary bg-opacity-10 py-2 px-4">アカウント作成（CSVインポート）</h2>

			<%-- CSVが読み込めなかったときだけ表示 --%>
			<c:if test="${not empty errors}">
				<div class="alert alert-danger mx-4" role="alert">
					<p class="mb-1"><c:out value="${errorMessage}" /></p>
					<ul class="mb-0">
						<c:forEach var="message" items="${errors}">
							<li><c:out value="${message}" /></li>
						</c:forEach>
					</ul>
				</div>
			</c:if>

			<p class="px-4">学生の名前・学生番号・クラスが書かれたCSVファイルを選んでください。</p>
			<ul class="px-5 small text-muted">
				<li>列の順番は「名前,学生番号,クラス」の3つです。</li>
				<li>1行目に「学生番号」と書かれた見出し行があれば、読み飛ばします。</li>
				<li>文字コードは UTF-8 または Shift_JIS です。</li>
				<li>クラスは、先に登録されているものだけ書けます：
					<c:forEach var="className" items="${classNames}" varStatus="status">
						<c:out value="${className}" /><c:if test="${not status.last}">、</c:if>
					</c:forEach>
					<c:if test="${empty classNames}">（まだ登録されていません）</c:if>
				</li>
			</ul>

			<form action="AccountImportExecute.action" method="post" enctype="multipart/form-data" class="px-4">
				<div class="mb-3">
					<label class="form-label" for="csvFile">CSVファイル</label>
					<input type="file" class="form-control" id="csvFile" name="csvFile"
						accept=".csv,text/csv" required>
				</div>
				<div class="d-grid gap-2">
					<button type="submit" class="btn btn-primary">インポートする</button>
				</div>
			</form>
		</section>
	</c:param>
</c:import>
