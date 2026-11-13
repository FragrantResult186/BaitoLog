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
		<section class="me-lg-4">
			<h2 class="h3 mb-3 fw-normal bg-secondary bg-opacity-10 py-2 px-4">勤務先</h2>

			<c:if test="${isSaved}">
				<div class="alert alert-success mx-3 mx-md-4" role="alert">保存しました</div>
			</c:if>
			<c:if test="${isDeleted}">
				<div class="alert alert-success mx-3 mx-md-4" role="alert">削除しました</div>
			</c:if>
			<c:if test="${not empty errorMessage}">
				<div class="alert alert-danger mx-3 mx-md-4" role="alert">${errorMessage}</div>
			</c:if>

			<div class="row g-4 px-3 px-md-4">
				<%-- 左：登録済みの勤務先カード --%>
				<div class="col-12 col-lg-5">
					<div class="d-flex justify-content-between align-items-center mb-2">
						<span class="fw-bold">登録済みの勤務先</span>
						<a class="btn btn-sm btn-outline-primary" href="WorkplaceList.action?new=1">＋ 追加</a>
					</div>

					<div class="d-grid gap-2">
						<c:forEach var="w" items="${workplaces}">
							<a class="card text-decoration-none border-2 ${w.id == selected.id ? 'border-primary bg-primary bg-opacity-10 shadow' : 'border-light shadow-sm'}"
								href="WorkplaceList.action?id=${w.id}"
								style="border-left: 0.4rem solid <c:out value='${w.color}' />;"
								aria-current="${w.id == selected.id ? 'true' : 'false'}">
								<div class="card-body py-3 px-3">
									<div class="d-flex align-items-center gap-2">
										<span class="rounded-circle flex-shrink-0"
											style="width: 0.85rem; height: 0.85rem; background-color: <c:out value='${w.color}' />;"
											aria-hidden="true"></span>
										<span class="fw-semibold text-body"><c:out value="${w.displayName}" /></span>
										<c:if test="${w.id == selected.id}">
											<span class="badge text-bg-primary ms-auto">選択中</span>
										</c:if>
									</div>
								</div>
							</a>
						</c:forEach>
						<c:if test="${empty workplaces}">
							<div class="card border-light shadow-sm">
								<div class="card-body text-muted">まだ登録されている勤務先はありません</div>
							</div>
						</c:if>
					</div>
				</div>

				<%-- 右：勤務先編集パネル --%>
				<div class="col-12 col-lg-7">
					<c:choose>
						<c:when test="${not empty selected}">
							<div class="card border-0 shadow-sm">
								<div class="card-body p-3 p-md-4">
									<div class="d-flex justify-content-between align-items-center mb-3">
										<h3 class="h5 mb-0">
											<c:choose>
												<c:when test="${isNew or selected.id == 0}">勤務先の登録</c:when>
												<c:otherwise>勤務先の編集</c:otherwise>
											</c:choose>
										</h3>
										<c:if test="${not isNew and selected.id != 0}">
											<span class="badge text-bg-light">勤務先情報</span>
										</c:if>
									</div>

									<form action="WorkplaceList.action" method="post">
										<input type="hidden" name="id" value="${selected.id}">

										<div class="row g-3 mb-3">
											<div class="col-12 col-md-6">
												<label class="form-label" for="workplace_name">勤務先名</label>
												<input type="text" class="form-control" id="workplace_name" name="workplace_name"
													maxlength="50" value="<c:out value='${selected.name}' />" required>
											</div>
											<div class="col-12 col-md-6">
												<label class="form-label" for="branch_name">店舗名</label>
												<input type="text" class="form-control" id="branch_name" name="branch_name"
													maxlength="50" value="<c:out value='${selected.branchName}' />">
											</div>
										</div>

										<fieldset class="mb-4">
											<input type="hidden" id="color" name="color" value="<c:out value='${selected.color}' />">
											<legend class="form-label mb-2">表示色</legend>
											<div id="color-swatches" class="d-flex flex-wrap gap-2" role="radiogroup" aria-label="表示色">
												<button type="button" class="color-swatch color-swatch-auto btn btn-sm border border-2 border-light rounded-2 px-2 flex-shrink-0 text-nowrap"
													data-color="" aria-label="自動で決める" aria-pressed="false">自動</button>
												<c:forEach var="c" items="${colors}">
													<button type="button" class="color-swatch border border-2 border-light rounded-circle p-0 flex-shrink-0"
														data-color="<c:out value='${c[0]}' />" title="<c:out value='${c[1]}' />"
														aria-label="<c:out value='${c[1]}' />" aria-pressed="false"
														style="width: 2.25rem; height: 2.25rem; background-color: <c:out value='${c[0]}' />;"></button>
												</c:forEach>
											</div>
										</fieldset>

										<div class="d-flex justify-content-between align-items-center mb-1">
											<h4 class="h6 mb-0">シフトテンプレート</h4>
											<span class="small text-muted">勤務時間の定型</span>
										</div>
										<p class="small text-muted mb-2">削除したテンプレートは、保存ボタンを押すまで反映されません。</p>

										<div class="table-responsive">
											<table class="table table-sm align-middle mb-2" id="template-table">
												<thead class="table-light">
													<tr>
														<th scope="col">名前</th>
														<th scope="col">開始</th>
														<th scope="col">終了</th>
														<th scope="col">休憩（分）</th>
														<th scope="col" class="text-end">操作</th>
													</tr>
												</thead>
												<tbody>
													<c:forEach var="t" items="${templates}">
														<tr class="template-row">
															<td>
																<input type="text" class="form-control form-control-sm" name="template_name"
																	maxlength="20" value="<c:out value='${t.name}' />" aria-label="テンプレート名">
															</td>
															<td>
																<div class="time-select d-flex align-items-center gap-1" data-time-select>
																	<input type="hidden" class="time-value" name="tpl_start" value="<c:out value='${t.startTime}' />">
																	<select class="form-select form-select-sm time-hour w-auto" aria-label="開始時刻（時）"></select>
																	<span class="small text-muted">時</span>
																	<select class="form-select form-select-sm time-minute w-auto" aria-label="開始時刻（分）"></select>
																	<span class="small text-muted">分</span>
																</div>
															</td>
															<td>
																<div class="time-select d-flex align-items-center gap-1" data-time-select>
																	<input type="hidden" class="time-value" name="tpl_end" value="<c:out value='${t.endTime}' />">
																	<select class="form-select form-select-sm time-hour w-auto" aria-label="終了時刻（時）"></select>
																	<span class="small text-muted">時</span>
																	<select class="form-select form-select-sm time-minute w-auto" aria-label="終了時刻（分）"></select>
																	<span class="small text-muted">分</span>
																</div>
															</td>
															<td>
																<input type="number" class="form-control form-control-sm" name="tpl_break"
																	min="0" max="999" value="${t.breakMin}" aria-label="休憩時間（分）">
															</td>
															<td class="text-end">
																<button type="button" class="delete-template btn btn-sm btn-outline-danger">削除</button>
															</td>
														</tr>
													</c:forEach>
													<%-- 新しいテンプレートを入力する空の行 --%>
													<tr class="template-row">
														<td>
															<input type="text" class="form-control form-control-sm" name="template_name"
																maxlength="20" placeholder="例：平日夕方" aria-label="テンプレート名">
														</td>
														<td>
																<div class="time-select d-flex align-items-center gap-1" data-time-select>
																	<input type="hidden" class="time-value" name="tpl_start" value="">
																	<select class="form-select form-select-sm time-hour w-auto" aria-label="開始時刻（時）"></select>
																	<span class="small text-muted">時</span>
																	<select class="form-select form-select-sm time-minute w-auto" aria-label="開始時刻（分）"></select>
																	<span class="small text-muted">分</span>
																</div>
														</td>
														<td>
																<div class="time-select d-flex align-items-center gap-1" data-time-select>
																	<input type="hidden" class="time-value" name="tpl_end" value="">
																	<select class="form-select form-select-sm time-hour w-auto" aria-label="終了時刻（時）"></select>
																	<span class="small text-muted">時</span>
																	<select class="form-select form-select-sm time-minute w-auto" aria-label="終了時刻（分）"></select>
																	<span class="small text-muted">分</span>
																</div>
														</td>
														<td>
															<input type="number" class="form-control form-control-sm" name="tpl_break"
																min="0" max="999" value="0" aria-label="休憩時間（分）">
														</td>
														<td class="text-end">
															<button type="button" class="delete-template btn btn-sm btn-outline-danger">削除</button>
														</td>
													</tr>
												</tbody>
											</table>
										</div>

										<template id="empty-template-row">
											<tr class="template-row">
												<td>
													<input type="text" class="form-control form-control-sm" name="template_name"
														maxlength="20" placeholder="例：平日夕方" aria-label="テンプレート名">
												</td>
												<td>
															<div class="time-select d-flex align-items-center gap-1" data-time-select>
																<input type="hidden" class="time-value" name="tpl_start" value="">
																<select class="form-select form-select-sm time-hour w-auto" aria-label="開始時刻（時）"></select>
																<span class="small text-muted">時</span>
																<select class="form-select form-select-sm time-minute w-auto" aria-label="開始時刻（分）"></select>
																<span class="small text-muted">分</span>
																</div>
												</td>
												<td>
															<div class="time-select d-flex align-items-center gap-1" data-time-select>
																<input type="hidden" class="time-value" name="tpl_end" value="">
																<select class="form-select form-select-sm time-hour w-auto" aria-label="終了時刻（時）"></select>
																<span class="small text-muted">時</span>
																<select class="form-select form-select-sm time-minute w-auto" aria-label="終了時刻（分）"></select>
																<span class="small text-muted">分</span>
																</div>
												</td>
												<td>
													<input type="number" class="form-control form-control-sm" name="tpl_break"
														min="0" max="999" value="0" aria-label="休憩時間（分）">
												</td>
												<td class="text-end">
													<button type="button" class="delete-template btn btn-sm btn-outline-danger">削除</button>
												</td>
											</tr>
										</template>

										<button type="button" class="btn btn-sm btn-outline-secondary mb-3" id="add-template">＋ テンプレートを追加</button>

										<div class="d-flex flex-wrap gap-2">
											<button type="submit" class="btn btn-primary" name="action" value="save">保存</button>
											<c:if test="${selected.id != 0}">
												<button type="submit" class="btn btn-outline-danger" name="action" value="delete"
													onclick="return confirm('この勤務先を削除しますか？');" formnovalidate>削除</button>
											</c:if>
										</div>
									</form>
								</div>
							</div>

							<script>
							(function () {
								var colorInput = document.getElementById("color");
								var swatches = document.querySelectorAll("#color-swatches .color-swatch");

								function updateSelectedSwatch() {
									var selectedColor = colorInput.value || "";
									swatches.forEach(function (swatch) {
										var selected = swatch.getAttribute("data-color") === selectedColor;
										swatch.setAttribute("aria-pressed", selected ? "true" : "false");
										swatch.classList.toggle("border-dark", selected);
										swatch.classList.toggle("border-light", !selected);
									});
								}

								swatches.forEach(function (swatch) {
									swatch.addEventListener("click", function () {
										colorInput.value = swatch.getAttribute("data-color");
										updateSelectedSwatch();
									});
								});
								updateSelectedSwatch();

								var tableBody = document.querySelector("#template-table tbody");
								var emptyRowTemplate = document.getElementById("empty-template-row");

														function twoDigits(value) {
															return value < 10 ? "0" + value : String(value);
														}

														function populateTimeSelect(select, max) {
															select.innerHTML = "";
															var emptyOption = document.createElement("option");
															emptyOption.value = "";
															emptyOption.textContent = "--";
															select.appendChild(emptyOption);
															for (var value = 0; value <= max; value++) {
																var option = document.createElement("option");
																option.value = twoDigits(value);
																option.textContent = twoDigits(value);
																select.appendChild(option);
															}
														}

														function initializeTimeSelect(container) {
															var hidden = container.querySelector(".time-value");
															var hourSelect = container.querySelector(".time-hour");
															var minuteSelect = container.querySelector(".time-minute");
															populateTimeSelect(hourSelect, 23);
															populateTimeSelect(minuteSelect, 59);

															var match = /^(\d{1,2}):(\d{2})/.exec(hidden.value || "");
															if (match && Number(match[1]) <= 23 && Number(match[2]) <= 59) {
																hourSelect.value = twoDigits(Number(match[1]));
																minuteSelect.value = twoDigits(Number(match[2]));
															}

															function syncTimeValue() {
																if (!hourSelect.value || !minuteSelect.value) {
																	hidden.value = "";
																	return;
																}
																hidden.value = hourSelect.value + ":" + minuteSelect.value;
															}

															hourSelect.addEventListener("change", syncTimeValue);
															minuteSelect.addEventListener("change", syncTimeValue);
														}

														document.querySelectorAll("[data-time-select]").forEach(initializeTimeSelect);

								function bindDeleteButton(row) {
									var button = row.querySelector(".delete-template");
									button.addEventListener("click", function () {
										if (!confirm("このシフトテンプレートを削除しますか？\n保存するまで反映されません。")) {
											return;
										}
										row.remove();
									});
								}

								tableBody.querySelectorAll(".template-row").forEach(bindDeleteButton);

								document.getElementById("add-template").addEventListener("click", function () {
									var row = emptyRowTemplate.content.firstElementChild.cloneNode(true);
									tableBody.appendChild(row);
															row.querySelectorAll("[data-time-select]").forEach(initializeTimeSelect);
									bindDeleteButton(row);
									row.querySelector('input[name="template_name"]').focus();
								});
							})();
							</script>
						</c:when>
						<c:otherwise>
							<div class="card border-0 shadow-sm">
								<div class="card-body text-muted">左の勤務先カードを選ぶか、「＋ 追加」を押してください。</div>
							</div>
						</c:otherwise>
					</c:choose>
				</div>
			</div>
		</section>
	</c:param>
</c:import>
