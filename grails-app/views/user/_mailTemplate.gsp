<%@ page contentType="text/html;charset=UTF-8"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title>Bartsy</title>
</head>
<body>
	<div class="body">
		<g:link controller="User" absolute="true"  base="${url}"  action="verifyEmailId" id="${userId}">Bartsy Account Verification</g:link>
		<p align=left>(This Email was automatically generated. Please do not reply)</p>
	</div>
</body>
</html>