<%@ page contentType="text/html;charset=UTF-8"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title>Bartsy</title>
</head>
<body>
	<div class="mailbody">
		<p>
			<label>Welcome to Bartsy, the easiest and most social way to
				get your drinks.</label>
		</p>
		<p>
			<label>Please click on the link bellow to verify your Bartsy
				account so you're able to start collecting rewards!</label>
		</p>
		<g:link controller="User" absolute="true" base="${url}"
			action="verifyEmailId" id="${userId}">Bartsy Account Verification</g:link>
		<p align=left>(This Email was automatically generated. Please do
			not reply)</p>

		<p>Using Bartsy is subject to the <a href="#">Terms of Service</a>. Your
			privacy is guaranteed, please read our <a href="#">Privacy Policy</a></p>

	</div>
</body>
</html>