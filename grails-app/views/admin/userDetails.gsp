<!doctype html>
<html>
<head>
</head>
<body>

	<table width="100%" border="0" cellspacing="0" cellpadding="0" class="popuptable">
		<tr>
			<td><g:message code="name.label" default="Name" /></td>
			<td>:</td><td>${selectedUser?.name}</td>
			<td><g:message code="nickname.label" default="Nickname" /></td>
			<td>:</td><td>${selectedUser?.nickName}</td>
		</tr>
		<tr>
			<td><g:message code="first.name.label" default="First Name" /></td>
			<td>:</td><td>${selectedUser?.firstName}</td>
			<td><g:message code="last.name.label" default="Last Name" /></td>
			<td>:</td><td>${selectedUser?.lastName}</td>
		</tr>
		<tr>
			<td><g:message code="gender.label" default="Gender" /></td>
			<td>:</td><td>${selectedUser?.gender}</td>
			<td><g:message code="email.label" default="Email" /></td>
			<td>:</td><td>${selectedUser?.email}</td>
		</tr>
		<tr>
			<td><g:message code="date.of.birth.label" default="Date of Birth" /></td>
			<td>:</td><td>${selectedUser?.dateOfBirth}</td>
			<td><g:message code="postcode.label" default="Zip/Post Code" /></td>
			<td>:</td><td>${selectedUser?.zipCode}</td>
		</tr>
		<tr>
			<td><g:message code="home.city.label" default="Home City" /></td>
			<td>:</td><td>${selectedUser?.homeCity}</td>
			<td><g:message code="state.label" default="State" /></td>
			<td>:</td><td>${selectedUser?.state}</td>
		</tr>
		<tr>
			<td><g:message code="orientation.label" default="Orientation" /></td>
			<td>:</td><td>${selectedUser?.orientation}</td>
			<td><g:message code="ethnicity.label" default="Ethnicity" /></td>
			<td>:</td><td>${selectedUser?.ethnicity}</td>
		</tr>
		<tr>
			<td><g:message code="description.label" default="Description" /></td>
			<td>:</td><td>${selectedUser?.description}</td>
			<td><g:message code="status.label" default="Status" /></td>
			<td>:</td><td>${selectedUser?.status}</td>
		</tr>
		<tr>
			<td><g:message code="bartsy.id.label" default="Bartsy Id" /></td>
			<td>:</td><td>${selectedUser?.bartsyId}</td>
			<td><g:message code="bartsy.password.label" default="Bartsy Password" /></td>
			<td>:</td><td>${selectedUser?.bartsyPassword}</td>
		</tr>
		<tr>
			<td><g:message code="bartsy.login.label" default="Bartsy Login" /></td>
			<td>:</td><td>${selectedUser?.bartsyLogin}</td>
		</tr>
		<tr>
			<td><g:message code="google.id.label" default="Google Id" /></td>
			<td>:</td><td>${selectedUser?.googleId}</td>
			<td><g:message code="google.username.label" default="Google Username" /></td>
			<td>:</td><td>${selectedUser?.googleUserName}</td>
		</tr>
		<tr>
			<td><g:message code="facebook.id.label" default="Facebook Id" /></td>
			<td>:</td><td>${selectedUser?.facebookId}</td>
			<td><g:message code="facebook.username.label" default="Facebook Username" /></td>
			<td>:</td><td>${selectedUser?.facebookUserName}</td>
		</tr>
		<tr>
			<td><g:message code="email.verified.label" default="Email Verified" /></td>
			<td>:</td><td>${selectedUser?.emailVerified}</td>
			<td><g:message code="session.code.label" default="Session Code" /></td>
			<td>:</td><td>${selectedUser?.sessionCode}</td>
		</tr>
		<tr>
			<td><g:message code="device.type.label" default="Device Type" /></td>
			<td>:</td><td>${selectedUser?.deviceType}</td>
			<!--  <td><g:message code="device.token.label" default="Device Token" /></td>
			<td>:</td><td>${selectedUser?.deviceToken}</td>-->
		</tr>
		<tr>
			<td><g:message code="date.created.label" default="Created Date" /></td>
			<td>:</td><td>${selectedUser?.dateCreated}</td>
			<td><g:message code="date.last.updated.label" default="Last Updated Date" /></td>
			<td>:</td><td>${selectedUser?.lastUpdated}</td>
		</tr>
		<!--<tr>
			  <td><g:message code="credit.card.label" default="Credit Card Number" /></td>
			<td>:</td><td>${selectedUser?.creditCardNumber}</td>
		</tr>
		<tr>
			<td><g:message code="cc.exp.month.label" default="Expiry Month" /></td>
			<td>:</td><td>${selectedUser?.expMonth}</td>
			<td><g:message code="cc.exp.year.label" default="Expiry Year" /></td>
			<td>:</td><td>${selectedUser?.expYear}</td>
		</tr>-->
	</table>

</body>
</html>