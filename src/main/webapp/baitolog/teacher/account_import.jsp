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
			<h2 class="h3 mb-3 fw-normal bg-secondary bg-opacity-10 py-2 px-4">学生アカウント登録</h2>

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
						<li>すでに登録されている学生番号は、氏名・クラス・生年月日が違うときに、その学生の情報を更新します（パスワードや、これまでのシフトなどの記録は、そのままです）。</li>
						<li>この画面では、まだ登録されません。エラーがあれば、エラーの行だけ表示します（直してから、もう一度読み込んでください）。</li>
					</ul>
					<form method="post" enctype="multipart/form-data" action="AccountImport.action" class="px-4">
						<input type="hidden" name="action" value="check">
						<div class="mb-3">
							<input type="file" class="form-control" name="file" accept=".csv,.txt,.xlsx" required>
						</div>
						<button type="submit" class="btn btn-primary">内容を確認する</button>
					</form>
				</c:when>

				<%-- 2. 読み込んだ内容を確認する（エラーがあればエラーの行だけ。エラーがなければ全行） --%>
				<c:when test="${mode == 'preview'}">
					<c:choose>
						<c:when test="${errorCount > 0}">
							<div class="alert alert-danger mx-4" role="alert">
								<c:out value="${fileName}" />　にエラーがあります（全 ${rowCount} 行中、エラー <strong>${errorCount}</strong> 行）。<br>
								ファイルを直して、もう一度読み込んでください。エラーがなくなるまで、次へは進めません。
							</div>
							<div class="px-4">
								<table class="table table-sm align-middle">
									<thead>
										<tr>
											<th>行</th>
											<th>クラス</th>
											<th>学生番号</th>
											<th>氏名</th>
											<th>生年月日</th>
											<th>エラー</th>
										</tr>
									</thead>
									<tbody>
										<c:forEach var="row" items="${rows}">
											<c:if test="${not empty row.error}">
												<tr class="table-danger">
													<td>${row.lineNo}</td>
													<td><c:out value="${row.className}" /></td>
													<td><c:out value="${row.studentNo}" /></td>
													<td><c:out value="${row.name}" /></td>
													<td><c:out value="${row.birthday}" /></td>
													<td><c:out value="${row.error}" /></td>
												</tr>
											</c:if>
										</c:forEach>
									</tbody>
								</table>
							</div>
							<div class="px-4">
								<form method="post" action="AccountImport.action">
									<input type="hidden" name="action" value="back">
									<button type="submit" class="btn btn-primary">ファイルを選び直す</button>
								</form>
							</div>
						</c:when>
						<c:otherwise>
							<p class="px-4">
								<c:out value="${fileName}" />　を読み込みました。エラーはありません（${rowCount} 人）。内容を確認してください。<br>
								新規登録 <strong>${newCount}</strong> 人 ／ 更新 <strong>${updateCount}</strong> 人 ／ 変更なし ${unchangedCount} 人
							</p>
							<div class="px-4">
								<table class="table table-sm align-middle">
									<thead>
										<tr>
											<th>種類</th>
											<th>クラス</th>
											<th>学生番号</th>
											<th>氏名</th>
											<th>生年月日</th>
										</tr>
									</thead>
									<tbody>
										<c:forEach var="row" items="${rows}">
											<tr class="${row.kind == 'UPDATE' ? 'table-warning' : (row.kind == 'UNCHANGED' ? 'text-muted' : '')}">
												<td>
													<c:choose>
														<c:when test="${row.kind == 'UPDATE'}">更新</c:when>
														<c:when test="${row.kind == 'UNCHANGED'}">変更なし</c:when>
														<c:otherwise>新規</c:otherwise>
													</c:choose>
												</td>
												<%-- 更新の行は、変わるところを「変更前 → 変更後」で見せる --%>
												<td>
													<c:choose>
														<c:when test="${row.kind == 'UPDATE' and row.classChanged}">
															<c:out value="${empty row.oldClassName ? '（未所属）' : row.oldClassName}" /> → <strong><c:out value="${row.className}" /></strong>
														</c:when>
														<c:otherwise><c:out value="${row.className}" /></c:otherwise>
													</c:choose>
												</td>
												<td><c:out value="${row.studentNo}" /></td>
												<td>
													<c:choose>
														<c:when test="${row.kind == 'UPDATE' and row.nameChanged}">
															<c:out value="${row.oldName}" /> → <strong><c:out value="${row.name}" /></strong>
														</c:when>
														<c:otherwise><c:out value="${row.name}" /></c:otherwise>
													</c:choose>
												</td>
												<td>
													<c:choose>
														<c:when test="${row.kind == 'UPDATE' and row.birthdayChanged}">
															<c:out value="${empty row.oldBirthday ? '（未登録）' : row.oldBirthday}" /> → <strong><c:out value="${row.birthday}" /></strong>
														</c:when>
														<c:otherwise><c:out value="${row.birthday}" /></c:otherwise>
													</c:choose>
												</td>
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
								<c:choose>
									<c:when test="${newCount + updateCount > 0}">
										<form method="post" action="AccountImport.action">
											<input type="hidden" name="action" value="confirm">
											<input type="hidden" name="token" value="<c:out value='${token}' />">
											<button type="submit" class="btn btn-primary">次へ（登録・更新の最終確認）</button>
										</form>
									</c:when>
									<c:otherwise>
										<span class="text-muted align-self-center">登録・更新する学生がいません（すべて登録済みで、変更がありません）</span>
									</c:otherwise>
								</c:choose>
							</div>
						</c:otherwise>
					</c:choose>
				</c:when>

				<%-- 3. 登録の最終確認 --%>
				<c:when test="${mode == 'confirm'}">
					<div class="alert alert-warning mx-4" role="alert">
						新規 <strong>${newCount} 人</strong>のアカウントを登録し、<strong>${updateCount} 人</strong>の情報（氏名・クラス・生年月日）を更新します。<br>
						新規の学生の最初のパスワードは、生年月日（例：20061004）です。更新する学生のパスワードや、これまでのシフト・勤務先などの記録は変わりません。<br>
						登録・更新したあとは取り消せません。
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
							<button type="submit" class="btn btn-danger">登録・更新する</button>
						</form>
					</div>
				</c:when>

				<%-- 4. 結果 --%>
				<c:when test="${mode == 'result'}">
					<div class="alert alert-success mx-4" role="alert">
						新規 <strong>${createdCount} 人</strong>のアカウントを登録し、<strong>${updatedCount} 人</strong>の情報を更新しました。<br>
						新規の学生は、学生番号と生年月日（例：20061004）でログインし、新しいパスワードを登録します。
					</div>
					<div class="px-4">
						<a href="AccountImport.action" class="btn btn-outline-primary">続けて登録・更新する</a>
					</div>
				</c:when>
			</c:choose>
		</section>
	</c:param>
</c:import>
