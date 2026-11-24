<%-- 共通テンプレート（各画面は c:import でこれを使う） --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<!-- Bootstrap CSS -->
<link
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css"
	rel="stylesheet"
	integrity="sha384-rbsA2VBKQhggwzxH7pPCaAqO46MgnOM80zW1RWuH61DGLwZJEdK2Kadq2F9CUG65"
	crossorigin="anonymous">
<!-- アイコン（サイドバー・ダッシュボード用）と日本語フォント -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Noto+Sans+JP:wght@400;500;700&display=swap" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Poppins:wght@600;700&display=swap" rel="stylesheet">
<link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
<link href="${pageContext.request.contextPath}/css/theme.css" rel="stylesheet">
<title>${param.title}</title>
<script src="https://code.jquery.com/jquery-3.2.1.min.js"></script>
${param.scripts}
</head>
<body class="${ user.isAuthenticated() ? 'app-auth' : 'app-guest' }">
	<div id="wrapper" class="container">

		<%-- 左：サイドバー（ログイン後のみ） --%>
		<c:if test="${ user.isAuthenticated() }">
			<aside class="app-side">
				<img src="${pageContext.request.contextPath}/img/logo.png" alt="BaitoLog" class="app-logo">
				<nav class="app-nav">
					<c:import url="/common/navigation.jsp"/>
				</nav>
			</aside>
		</c:if>

		<%-- 右：ヘッダー・中身・フッター --%>
		<div class="app-main">
			<header class="app-header d-flex justify-content-between align-items-center">
					<c:import url="/common/header.jsp"/>
			</header>

			<div class="row justify-content-center">
				<c:choose>
					<%-- ログイン済みの場合 --%>
					<c:when test="${user.isAuthenticated()}">
						<main class="col-12"> ${param.content} </main>
					</c:when>
					<%-- 未ログインの場合（main の変更：画面が狭いときも見やすい幅にする） --%>
					<c:otherwise>
						<main class="col-12 col-md-8 col-lg-6"> ${param.content} </main>
					</c:otherwise>
				</c:choose>
			</div>
			<footer class="py-2 mt-auto mb-0 bg-dark bg-opacity-10 border-top border-3 align-bottom">
				<c:import url="/common/footer.jsp"/>
			</footer>
		</div>

	</div>
</body>
</html>