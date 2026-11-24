<%-- 学生の削除の確認画面（教職員） --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:import url="/common/base.jsp">
	<c:param name="title">
		BaitoLog　学生の削除
	</c:param>

	<c:param name="scripts">
		<script>
			document.addEventListener("DOMContentLoaded", function() {
				var form = document.getElementById("deleteForm");
				if (form) {
					form.addEventListener("submit", function(e) {
						// 最後の確認（やめるときは何も起きない）
						if (!window.confirm("本当にこの学生を削除しますか？\nこの操作は取り消せません。")) {
							e.preventDefault();
							return;
						}
						// 二重に押せないようにする
						form.querySelector("button[type=submit]").disabled = true;
					});
				}
			});
		</script>
	</c:param>

	<c:param name="content">
		<section class="me-4">
			<h2 class="h3 mb-3 fw-normal bg-secondary bg-opacity-10 py-2 px-4">学生の削除</h2>

			<c:if test="${not empty errorMessage}">
				<div class="alert alert-danger mx-4" role="alert"><c:out value="${errorMessage}" /></div>
			</c:if>

			<div class="px-4">
				<table class="table table-sm w-auto">
					<tr><th class="pe-4">学生番号</th><td><c:out value="${student.loginId}" /></td></tr>
					<tr><th class="pe-4">氏名</th><td><c:out value="${student.name}" /></td></tr>
					<tr><th class="pe-4">クラス</th><td><c:out value="${student.schoolClass.name}" default="（未所属）" /></td></tr>
					<tr><th class="pe-4">生年月日</th><td><c:out value="${student.birthday}" default="未登録" /></td></tr>
				</table>
			</div>

			<p class="px-4" style="color:#dc3545; font-weight:400;">
				この学生のアカウントと、勤務先・シフトテンプレート・シフト・報告書などのデータをすべて削除します。<br>
				この操作は取り消せません。削除したあと、この学生はログインできなくなります。
			</p>

			<form id="deleteForm" method="post" action="StudentDelete.action" class="px-4">
				<input type="hidden" name="id" value="${student.id}">
				<input type="hidden" name="token" value="<c:out value='${token}' />">

				<div class="mb-3">
					<label class="form-label" for="reason">削除の理由（必須）</label>
					<textarea class="form-control" id="reason" name="reason" rows="3" maxlength="200"
						placeholder="例：誤って登録されたため／卒業のため／退学のため" required><c:out value="${reason}" /></textarea>
					<div class="form-text">200文字以内。記録として残ります。</div>
				</div>

				<div class="d-flex gap-2 mb-5">
					<a href="StudentDetail.action?id=${student.id}" class="btn btn-outline-secondary">やめる</a>
					<button type="submit" class="btn btn-danger">削除する</button>
				</div>
			</form>
		</section>
	</c:param>
</c:import>