<%-- 学生アカウント一括登録画面（教職員） --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:import url="/common/base.jsp">
	<c:param name="title">
		BaitoLog　アカウント登録
	</c:param>

	<c:param name="scripts">
		<script>
			// 登録ボタンを押したら、二重に押せないようにする
			document.addEventListener("DOMContentLoaded", function() {
				document.querySelectorAll("form[data-once]").forEach(function(form) {
					form.addEventListener("submit", function() {
						form.querySelectorAll("button[type=submit]").forEach(function(b) {
							b.disabled = true;
						});
					});
				});
			});
		</script>
	</c:param>

	<c:param name="content">
		<section class="me-4">
			<h2 class="h3 mb-3 fw-norma bg-secondary bg-opacity-10 py-2 px-4">学生アカウント登録</h2>

			<c:if test="${not empty errorMessage}">
				<div class="alert alert-danger mx-4" role="alert"><c:out value="${errorMessage}" /></div>
			</c:if>

			<c:choose>
				<%-- 1. ファイルを選ぶ --%>
				<c:when test="${mode == 'upload'}">
					<p class="px-4">学校の学生ファイル（CSV または xlsx）を選んでください。</p>
					<ul class="px-5 small text-muted">
						<li>1行目は見出しにしてください。「氏名」「学生番号」「クラス」「生年月日」の列が必要です（列の順番は問いません）。</li>
						<li>生年月日が最初のパスワードになります（例：2006年10月4日 → 20061004）。</li>
						<li>この画面では、まだ登録されません。次の画面で内容を確認します。</li>
					</ul>
					<form method="post" enctype="multipart/form-data" action="AccountImport.action" class="px-4">
						<input type="hidden" name="action" value="check">
						<div class="mb-3">
							<input type="file" class="form-control" name="file" accept=".csv,.txt,.xlsx" required>
						</div>
						<button type="submit" class="btn btn-primary">内容を確認する</button>
					</form>
				</c:when>

				<%-- 2. 読み込んだ内容を確認する --%>
				<c:when test="${mode == 'preview'}">
					<p class="px-4">
						<c:out value="${fileName}" />　を読み込みました。内容を確認してください。<br>
						全 ${rowCount} 行　登録できる：<strong>${okCount}</strong> 行　エラー：
						<strong class="${errorCount > 0 ? 'text-danger' : ''}">${errorCount}</strong> 行
					</p>
					<c:if test="${errorCount > 0}">
						<p class="px-4 text-danger small">エラーのある行は登録されません。ファイルを直してもう一度読み込むか、そのまま進んでエラー以外の行だけ登録できます。</p>
					</c:if>
					<div class="px-4">
						<table class="table table-sm align-middle">
							<thead>
								<tr>
									<th>行</th>
									<th>クラス</th>
									<th>学生番号</th>
									<th>氏名</th>
									<th>生年月日</th>
									<th>状態</th>
								</tr>
							</thead>
							<tbody>
								<c:forEach var="row" items="${rows}">
									<tr class="${empty row.error ? '' : 'table-danger'}">
										<td>${row.lineNo}</td>
										<td><c:out value="${row.className}" /></td>
										<td><c:out value="${row.studentNo}" /></td>
										<td><c:out value="${row.name}" /></td>
										<td><c:out value="${row.birthday}" /></td>
										<td><c:choose>
											<c:when test="${empty row.error}">OK</c:when>
											<c:otherwise><c:out value="${row.error}" /></c:otherwise>
										</c:choose></td>
									</tr>
								</c:forEach>
							</tbody>
						</table>
					</div>
					<div class="d-flex gap-2 px-4">
						<form method="post" action="AccountImport.action">
							<input type="hidden" name="action" value="back">
							<button type="submit" class="btn btn-outline-secondary">やり直す</button>
						</form>
						<form method="post" action="AccountImport.action">
							<input type="hidden" name="action" value="confirm">
							<input type="hidden" name="token" value="<c:out value='${token}' />">
							<button type="submit" class="btn btn-primary" ${okCount == 0 ? 'disabled' : ''}>次へ（登録の最終確認）</button>
						</form>
					</div>
				</c:when>

				<%-- 3. 登録の最終確認 --%>
				<c:when test="${mode == 'confirm'}">
					<div class="alert alert-warning mx-4" role="alert">
						<strong>${okCount} 人</strong>のアカウントを登録します。<br>
						<c:if test="${errorCount > 0}">エラーのある ${errorCount} 行は登録されません。<br></c:if>
						最初のパスワードは、各学生の生年月日（例：20061004）です。登録したあとは取り消せません。
					</div>
					<div class="d-flex gap-2 px-4">
						<form method="post" action="AccountImport.action">
							<input type="hidden" name="action" value="preview">
							<input type="hidden" name="token" value="<c:out value='${token}' />">
							<button type="submit" class="btn btn-outline-secondary">戻る</button>
						</form>
						<form method="post" action="AccountImport.action" data-once>
							<input type="hidden" name="action" value="register">
							<input type="hidden" name="token" value="<c:out value='${token}' />">
							<button type="submit" class="btn btn-danger">登録する</button>
						</form>
					</div>
				</c:when>

				<%-- 4. 結果 --%>
				<c:when test="${mode == 'result'}">
					<div class="alert alert-success mx-4" role="alert">
						<strong>${createdCount} 人</strong>のアカウントを登録しました。<br>
						学生は、学生番号と生年月日（例：20061004）でログインし、新しいパスワードを登録します。
					</div>
					<c:if test="${not empty skippedRows}">
						<p class="px-4 text-danger">登録しなかった行：</p>
						<div class="px-4">
							<table class="table table-sm">
								<thead>
									<tr><th>行</th><th>学生番号</th><th>氏名</th><th>理由</th></tr>
								</thead>
								<tbody>
									<c:forEach var="row" items="${skippedRows}">
										<tr>
											<td>${row.lineNo}</td>
											<td><c:out value="${row.studentNo}" /></td>
											<td><c:out value="${row.name}" /></td>
											<td><c:out value="${row.error}" /></td>
										</tr>
									</c:forEach>
								</tbody>
							</table>
						</div>
					</c:if>
					<div class="px-4">
						<a href="AccountImport.action" class="btn btn-outline-primary">続けて登録する</a>
					</div>
				</c:when>
			</c:choose>
		</section>
	</c:param>
</c:import>
