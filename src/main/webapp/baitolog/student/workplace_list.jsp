<%-- 勤務先一覧・登録画面（学生） --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:import url="/common/base.jsp">
	<c:param name="title">
		BaitoLog　勤務先
	</c:param>

	<c:param name="scripts"></c:param>

	<c:param name="content">
		<section class="me-4">
			<h2 class="h3 mb-3 fw-norma bg-secondary bg-opacity-10 py-2 px-4">勤務先</h2>

			<c:if test="${isSaved}">
				<div class="alert alert-success mx-4" role="alert">保存しました</div>
			</c:if>
			<c:if test="${isDeleted}">
				<div class="alert alert-success mx-4" role="alert">削除しました</div>
			</c:if>
			<c:if test="${not empty errorMessage}">
				<div class="alert alert-danger mx-4" role="alert">${errorMessage}</div>
			</c:if>

			<div class="row px-4">
				<%-- 左：登録済みの勤務先の一覧（行を押すと編集欄が開く） --%>
				<div class="col-5">
					<div class="d-flex justify-content-between align-items-center mb-2">
						<span class="fw-bold">登録済みの勤務先</span>
						<a class="btn btn-sm btn-outline-primary" href="WorkplaceList.action?new=1">＋ 追加</a>
					</div>
					<div class="list-group">
						<c:forEach var="w" items="${workplaces}">
							<a class="list-group-item list-group-item-action ${w.id == selected.id ? 'active' : ''}"
								href="WorkplaceList.action?id=${w.id}">
								<span style="display:inline-block;width:0.9em;height:0.9em;background:<c:out value="${w.color}"/>;"></span>
								<c:out value="${w.displayName}" />
							</a>
						</c:forEach>
						<c:if test="${empty workplaces}">
							<div class="list-group-item text-muted">まだ登録されていません</div>
						</c:if>
					</div>
				</div>

				<%-- 右：編集欄 --%>
				<div class="col-7">
					<c:choose>
						<c:when test="${not empty selected}">
							<form action="WorkplaceList.action" method="post">
								<input type="hidden" name="id" value="${selected.id}">
								<h3 class="h5 mb-3">
									<c:choose>
										<c:when test="${isNew or selected.id == 0}">勤務先の登録</c:when>
										<c:otherwise>勤務先の編集</c:otherwise>
									</c:choose>
								</h3>
								<div class="mb-3">
									<label class="form-label" for="workplace_name">勤務先名</label>
									<input type="text" class="form-control" id="workplace_name" name="workplace_name"
										maxlength="50" value="<c:out value="${selected.name}"/>" required>
								</div>
								<div class="mb-3">
									<label class="form-label" for="branch_name">店舗名</label>
									<input type="text" class="form-control" id="branch_name" name="branch_name"
										maxlength="50" value="<c:out value="${selected.branchName}"/>">
								</div>
								<div class="mb-3">
									<label class="form-label" for="color">表示色</label>
									<select class="form-select" id="color" name="color">
										<option value="">自動割当</option>
										<c:forEach var="c" items="${colors}">
											<option value="${c[0]}" ${c[0] == selected.color ? 'selected' : ''}>${c[1]}</option>
										</c:forEach>
									</select>
								</div>

								<div class="mb-2 fw-bold">シフトテンプレート</div>
								<table class="table table-sm align-middle" id="template-table">
									<thead>
										<tr><th>名前</th><th>開始</th><th>終了</th><th>休憩（分）</th></tr>
									</thead>
									<tbody>
										<c:forEach var="t" items="${templates}">
											<tr>
												<td><input type="text" class="form-control form-control-sm" name="template_name" maxlength="20" value="<c:out value="${t.name}"/>"></td>
												<td><input type="time" class="form-control form-control-sm" name="tpl_start" step="900" value="<c:out value="${t.startTime}"/>"></td>
												<td><input type="time" class="form-control form-control-sm" name="tpl_end" step="900" value="<c:out value="${t.endTime}"/>"></td>
												<td><input type="number" class="form-control form-control-sm" name="tpl_break" min="0" max="999" value="${t.breakMin}"></td>
											</tr>
										</c:forEach>
										<%-- 新しいテンプレートを入力する空の行 --%>
										<tr>
											<td><input type="text" class="form-control form-control-sm" name="template_name" maxlength="20" placeholder="例：平日夕方"></td>
											<td><input type="time" class="form-control form-control-sm" name="tpl_start" step="900"></td>
											<td><input type="time" class="form-control form-control-sm" name="tpl_end" step="900"></td>
											<td><input type="number" class="form-control form-control-sm" name="tpl_break" min="0" max="999" value="0"></td>
										</tr>
									</tbody>
								</table>
								<button type="button" class="btn btn-sm btn-outline-secondary mb-3" id="add-template">＋ テンプレートを追加</button>

								<div class="d-flex gap-2">
									<button type="submit" class="btn btn-primary" name="action" value="save">保存</button>
									<c:if test="${selected.id != 0}">
										<button type="submit" class="btn btn-outline-danger" name="action" value="delete"
											onclick="return confirm('この勤務先を削除しますか？');" formnovalidate>削除</button>
									</c:if>
								</div>
							</form>
							<script>
							// 「＋ テンプレートを追加」：空の行を1行ふやす
							document.getElementById("add-template").addEventListener("click", function () {
								var body = document.querySelector("#template-table tbody");
								var row = body.lastElementChild.cloneNode(true);
								row.querySelectorAll("input").forEach(function (input) {
									input.value = (input.name === "tpl_break") ? "0" : "";
								});
								body.appendChild(row);
							});
							</script>
						</c:when>
						<c:otherwise>
							<p class="text-muted">左の一覧から勤務先を選ぶか、「＋ 追加」を押してください。</p>
						</c:otherwise>
					</c:choose>
				</div>
			</div>
		</section>
	</c:param>
</c:import>
